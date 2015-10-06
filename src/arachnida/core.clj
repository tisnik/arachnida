(ns arachnida.core)

(require '[clojure.java.jdbc :as jdbc])

(require '[clojure.pprint     :as pprint])

(require '[hozumi.rm-rf       :as rm-rf])

(require '[ring.adapter.jetty      :as jetty])
(require '[ring.middleware.params  :as http-params])
(require '[ring.util.response      :as http-response])
(require '[ring.middleware.cookies :as cookies])

(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[arachnida.db-spec       :as db-spec])
(require '[arachnida.db-interface  :as db-interface])
(require '[arachnida.git-data-fetcher  :as git-data-fetcher])

(defn -main
    [& args]
    (git-data-fetcher/process))

