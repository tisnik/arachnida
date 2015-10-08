(ns arachnida.core)

(require '[clojure.tools.cli      :as cli])

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

(def cli-options
  [["-h" "--help"    "help"                   :id :help]
   ["-g" "--git"     "fetch GIT statistic"    :id :git]
   ["-s" "--server"  "run as HTTP server"     :id :server]])

(defn show-help
    []
    (println "Usage:")
    (println "-h" "--help      help")
    (println "-g" "--git       fetch GIT statistic")
    (println "-s" "--server    run as HTTP server"))

(defn show-error
    []
    (println "Unknown command line option!")
    (show-help))

(defn start-server
    []
    )

(defn -main
    [& args]
    (let [all-options           (cli/parse-opts args cli-options)
          options               (all-options :options)
          show-help?            (options :help)
          git?                  (options :git)
          server?               (options :server)]
        (cond show-help? (show-help)
              git?       (git-data-fetcher/process)
              server?    (start-server)
              :else      (show-error))))

