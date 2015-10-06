(ns arachnida.core)

(require '[clojure.java.jdbc :as jdbc])

(require '[clojure.pprint     :as pprint])

(require '[clj-jgit.porcelain :as jgit])
(require '[clj-jgit.querying  :as jgit-query])
(require '[clj-jgit.internal  :as jgit-internal])

(require '[hozumi.rm-rf       :as rm-rf])

(require '[ring.adapter.jetty      :as jetty])
(require '[ring.middleware.params  :as http-params])
(require '[ring.util.response      :as http-response])
(require '[ring.middleware.cookies :as cookies])

(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[arachnida.db-spec       :as db-spec])
(require '[arachnida.db-interface  :as db-interface])
(require '[arachnida.git-interface :as git-interface])
(require '[arachnida.commits-stat  :as commits-stat])

(defn insert-commit-info
    [])

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

(defn process-repository
    [product repository]
    (println (str "    Repository '" (:name repository) "' with ID: " (:id repository)))
    (println "        Repo ID:" (:id repository))
    (println "        Repo URL" (:url repository))
    ;(prepare-local-repository repository)
    (let [directory-name (repo-url->directory-name repository)]
        (try (jgit/with-repo directory-name
            (let [commits-stat (commits-stat/get-commits-stat-for-all-branches repo directory-name)]
                 (clojure.pprint/pprint commits-stat)
                 ;(rev-list repo commits-stat))
                 ))
            (catch Exception e
                (println "*** Exception *** " e)
                nil))))

(defn process-product
    [product]
    (println (str "Processing product '" (:name product) "' with ID: " (:id product)))
    (let [repolist (db-interface/read-repo-list (:id product))]
        (doseq [repository repolist]
            (process-repository product repository))))

(defn -main
    [& args]
    (let [products (db-interface/read-product-list)]
        (doseq [product products]
            (process-product product))))
;   (let [repolist (read-repolist)]
;       (doseq [repo repolist]
;           (process-repository repo))))

