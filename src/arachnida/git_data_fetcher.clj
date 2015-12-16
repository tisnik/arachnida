(ns arachnida.git-data-fetcher)

(require '[clojure.java.jdbc :as jdbc])

(require '[clojure.pprint     :as pprint])

(require '[clj-jgit.porcelain :as jgit])
(require '[clj-jgit.querying  :as jgit-query])
(require '[clj-jgit.internal  :as jgit-internal])

(require '[hozumi.rm-rf       :as rm-rf])

(require '[arachnida.db-spec       :as db-spec])
(require '[arachnida.db-interface  :as db-interface])
(require '[arachnida.git-interface :as git-interface])
(require '[arachnida.commits-stat  :as commits-stat])
(require '[arachnida.config        :as config])

(defn repo-url->directory-name
    "Prepares local directory name from repository URL."
    [repository]
    (let [url (:url repository)]
        (str "git/"
            (subs url (inc (.lastIndexOf url "/"))
                      (.lastIndexOf url ".")))))

(defn rm-dir
    [dirname]
    (try
        (rm-rf/rm-r (java.io.File. dirname))
        (catch Exception e)))

(defn prepare-local-repository
    [repository]
    (let [url     (:url repository)
          dirname (repo-url->directory-name repository)]
        (rm-dir dirname)
        (println "        Cloning into directory: " dirname)
        (git-interface/clone-repository url dirname)
        (println "        Fetching all branches: " dirname)
        (git-interface/fetch-all (repo-url->directory-name repository))))

(defn insert-changed-files
    [db commit-id changed-files]
    (doseq [changed-file changed-files]
        (db-interface/insert-changed-file db commit-id changed-file)))

(defn insert-branch-for-commit
    [transaction commit-id branch]
    (let [branch-name (commits-stat/update-branch-name branch)]
         (db-interface/insert-branch-for-commit transaction commit-id branch-name)))

(defn insert-branches-for-commit
    [transaction commit-id branches]
    (doseq [branch branches]
        (insert-branch-for-commit transaction commit-id branch)))

(defn insert-changed-files-and-branches
    [transaction product repository commit-sha changed-files branches]
    (let [commit-id (db-interface/read-commit-id transaction (:id product) (:id repository) commit-sha)]
                    (if (not commit-id)
                        (println "Warning: no ID for commit " commit-sha))
                    (insert-changed-files transaction commit-id changed-files)
                    (insert-branches-for-commit transaction commit-id branches)))

(defn revision-list
    [product repository repo commits-stat]
    (println "        List of all revision objects")
    (let [rev-list (jgit-query/rev-list repo)]
        (jdbc/with-db-transaction [transaction db-spec/data-db]
        (doseq [rev rev-list]
            (let [info (jgit-query/commit-info repo rev)
                  commit-sha (:id info)
                  author     (:author info)
                  message    (:message info)
                  date       (format "%tF" (:time info))
                  branches      (jgit-query/branches-for repo (:raw info))
                  changed-files (jgit-query/changed-files repo (:raw info))
                  stat          (get commits-stat (:id info))
                  files-changed (or (:files-changed stat) 0)
                  insertions    (or (:insertions stat) 0)
                  deletions     (or (:deletions stat) 0)]
                  (if (not stat)
                      (println "Warning: no stat info for commit " commit-sha "(" author message date branches ")"))
                  (db-interface/insert-commit transaction (:id product) (:id repository)
                                              commit-sha message
                                              author date files-changed insertions deletions)
                  (insert-changed-files-and-branches transaction product repository commit-sha changed-files branches)
             )))))

(defn process-repository
    [product repository]
    (println (str "    Repository '" (:name repository) "' with ID: " (:id repository)))
    (println "        Repo ID:" (:id repository))
    (println "        Repo URL" (:url repository))
    (prepare-local-repository repository)
    (let [directory-name (repo-url->directory-name repository)]
        (try (jgit/with-repo directory-name
            (let [commits-stat (commits-stat/get-commits-stat-for-all-branches repo directory-name)]
                 (println "        Commits stat:" (count commits-stat))
                 ;(clojure.pprint/pprint commits-stat)
                 (revision-list product repository repo commits-stat)))
            (catch Exception e
                (println "*** Exception *** " e)
                nil))))

(defn process-product
    [product]
    (println (str "Processing product '" (:name product) "' with ID: " (:id product)))
    (let [repolist (db-interface/read-repo-list (:id product))]
        (doseq [repository repolist]
            (process-repository product repository))))

(defn write-repositories-for-product-into-db
    [product-name repositories]
    (doseq [repository repositories]
        (let [repository-name (name (key repository))
              repository-url  (val repository)]
              (println "        " repository-name)
              (println "            [" repository-url "]")
              (db-interface/insert-repository db-spec/data-db product-name repository-name repository-url))))

(defn write-products-and-repositories-into-db
    [products-and-repositories]
    (println "Writing products and repositories into database")
    (doseq [product products-and-repositories]
        (let [product-name (name (key product))
              repositories (val product)]
              (println "    " product-name)
              (db-interface/insert-product db-spec/data-db product-name)
              (write-repositories-for-product-into-db product-name repositories)))
    (println "Done"))

(defn process
    []
    (let [products-and-repositories (config/load-repositories)]
        (write-products-and-repositories-into-db products-and-repositories))
    (let [products (db-interface/read-product-list)]
        (doseq [product products]
            (process-product product))))

