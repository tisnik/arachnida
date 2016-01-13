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

(ns arachnida.config)

(require '[clojure-ini.core :as clojure-ini])

(def repositories-ini-file
    "config/repositories.ini")

(def config-ini-file
    "config/config.ini")

(def url-to-common-files
    (atom nil))

(def mailto
    (atom nil))

(def port
    (atom nil))

(def start-year
    (atom nil))

(def end-year
    (atom nil))

(defn load-repositories
    "Load repositories from the provided INI file."
    []
    (clojure-ini/read-ini repositories-ini-file :keywordize? true))

(defn load-configuration
    "Load configuration from the provided INI file."
    []
    (let [cfg (clojure-ini/read-ini config-ini-file :keywordize? true)]
        (reset! url-to-common-files (-> cfg :settings :url-to-common-files))
        (reset! mailto     (-> cfg :settings :mailto))
        (reset! port       (Integer/parseInt (-> cfg :server :port)))
        (reset! start-year (Integer/parseInt (-> cfg :calendar :start_year)))
        (reset! end-year   (Integer/parseInt (-> cfg :calendar :end_year)))))

