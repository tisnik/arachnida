;
;  (C) Copyright 2015, 2016  Pavel Tisnovsky
;
;  All rights reserved. This program and the accompanying materials
;  are made available under the terms of the Eclipse Public License v1.0
;  which accompanies this distribution, and is available at
;  http://www.eclipse.org/legal/epl-v10.html
;
;  Contributors:
;      Pavel Tisnovsky
;

(ns arachnida.core  (:gen-class))

(require '[clojure.tools.cli      :as cli])

(require '[arachnida.db-spec          :as db-spec])
(require '[arachnida.db-interface     :as db-interface])
(require '[arachnida.git-data-fetcher :as git-data-fetcher])
(require '[arachnida.server           :as server])

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

(defn -main
    [& args]
    (let [all-options    (cli/parse-opts args cli-options)
          options        (all-options :options)
          show-help?     (options :help)
          git?           (options :git)
          server?        (options :server)]
        (cond show-help? (show-help)
              git?       (git-data-fetcher/process)
              server?    (server/start-server)
              :else      (show-error))))

