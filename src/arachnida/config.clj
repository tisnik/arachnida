(ns arachnida.config)

(require '[clojure-ini.core :as clojure-ini])

(def repositories-ini-file
    "config/repositories.ini")

(def config-ini-file
    "config/config.ini")

(def url-to-common-files
    (atom nil))

(defn load-repositories
    "Load repositories from the provided INI file."
    []
    (clojure-ini/read-ini repositories-ini-file :keywordize? true))

(defn load-configuration
    "Load configuration from the provided INI file."
    []
    (let [cfg (clojure-ini/read-ini config-ini-file :keywordize? true)
          settings (:settings cfg)]
        (reset! url-to-common-files (:url-to-common-files settings))))

