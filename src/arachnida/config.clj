(ns arachnida.config)

(require '[clojure-ini.core :as clojure-ini])

(defn load-repositories
    "Load repositories from the provided INI file."
    [ini-file-name]
    (clojure-ini/read-ini ini-file-name :keywordize? true))

