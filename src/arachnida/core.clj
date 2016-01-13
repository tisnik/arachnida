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
(require '[arachnida.config           :as config])

(def cli-options
  [["-h" "--help"    "help"                          :id :help]
   ["-g" "--git"     "fetch GIT statistic"           :id :git]
   ["-s" "--server"  "run as HTTP server"            :id :server]
   ["-c" "--config"  "display current configuration" :id :config]])

(defn show-help
    [summary]
    (println "Usage:")
    (println summary))

(defn show-error
    [summary]
    (println "Unknown command line option!")
    (show-help summary))

(defn display-configuration
    []
    (config/load-configuration)
    (config/print-configuration))

(defn -main
    [& args]
    (let [all-options    (cli/parse-opts args cli-options)
          options        (all-options :options)
          show-help?     (options :help)
          git?           (options :git)
          server?        (options :server)
          config?        (options :config)]
        (cond show-help? (show-help (:summary all-options))
              git?       (git-data-fetcher/process)
              server?    (server/start-server)
              config?    (display-configuration)
              :else      (show-error (:summary all-options)))))

