(ns arachnida.db-interface)

(require '[clojure.java.jdbc :as jdbc])
(require '[arachnida.db-spec :as db-spec])

(defn read-product-list
    []
    (jdbc/query db-spec/data-db  "select id, name
                                  from   products
                                  order  by id;"))

(defn read-repo-list
    [product-id]
    (jdbc/query db-spec/data-db ["select id, name, url
                                  from   repos
                                  where  product=? order by id;" product-id]))

(defn read-commit-id
    [db product repo branch sha]
    (:id
    (first
        (jdbc/query db ["select id from commits where product=? and repo=? and branch=? and sha=?"
                             product repo branch sha]))))

(defn insert-changed-file
    [db commit-id changed-file]
    (jdbc/insert! db "changed_files" {:commit_id commit-id
                                           :file_name (first changed-file)
                                           :operation (second changed-file)}))

(defn insert-commit
    "Zapis dat do tabulky."
    [db product repo branch sha message author date files-changed insertions deletions]
    (jdbc/insert! db "commits" {:product product :repo repo :branch branch
                                             :sha     sha     :message message
                                             :author author   :date date
                                             :files_changed files-changed
                                             :insertions insertions
                                             :deletions deletions}))

