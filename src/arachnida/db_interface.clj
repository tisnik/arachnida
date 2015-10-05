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

