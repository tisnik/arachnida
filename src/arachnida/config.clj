(ns arachnida.config)

(require '[clojure-ini.core :as clojure-ini])

(def repositories-ini-file
    "config/repositories.ini")

(defn load-repositories
    "Load repositories from the provided INI file."
    []
    (clojure-ini/read-ini repositories-ini-file :keywordize? true))

