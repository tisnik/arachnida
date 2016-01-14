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

(ns arachnida.server)

(require '[ring.adapter.jetty      :as jetty])
(require '[ring.middleware.params  :as http-params])
(require '[ring.util.response      :as http-response])
(require '[ring.middleware.cookies :as cookies])

(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[clj-calendar.calendar :as calendar])

(require '[arachnida.config        :as config])
(require '[arachnida.db-interface  :as db-interface])
(require '[arachnida.html-renderer :as html-renderer])

(defn start-of-year
    [year]
    (str year "-01-01"))

(defn end-of-year
    [year]
    (str year "-12-31"))

(defn read-stat-for-product
    [product-id start-year end-year]
    (into (sorted-map)
        (for [year (range start-year (inc end-year))]
            [year (db-interface/read-statistic-for-product product-id (start-of-year year) (end-of-year year))])))

(defn read-stat-for-product-repo
    [product-id start-year end-year]
    (into (sorted-map)
        (for [year (range start-year (inc end-year))]
            [year (db-interface/read-statistic-for-product-repo product-id (start-of-year year) (end-of-year year))])))

(defn read-stat-for-product-and-repo
    [product-id repository-name start-year end-year]
    (into (sorted-map)
        (for [year (range start-year (inc end-year))]
            [year (db-interface/read-statistic-for-product-and-repo product-id repository-name (start-of-year year) (end-of-year year))])))

(defn get-year-data
    [stat what]
    (for [s stat]
        [(key s) (or (get (val s) what) 0)]))

(defn read-stat-per-weeks
    [first-day last-day]
    (let [stat (first
        (db-interface/read-stat-per-weeks-from-db first-day last-day))]
        {:deletions     (or (:deletions stat) 0)
         :insertions    (or (:insertions stat) 0)
         :commits-count (or (:commits_count stat) 0)
         :files-changed (or (:files_changed stat) 0)}))

(defn read-stat-per-weeks-for-author
    [first-day last-day author]
    (if (and author (pos? (count author)))
        (let [stat (first
            (db-interface/read-stat-per-weeks-for-author-from-db first-day last-day author))]
            {:deletions     (or (:deletions stat) 0)
             :insertions    (or (:insertions stat) 0)
             :commits-count (or (:commits_count stat) 0)
             :files-changed (or (:files_changed stat) 0)})
            {:deletions     0
             :insertions    0
             :commits-count 0
             :files-changed 0}))

(defn read-branches-per-commit
    [commits]
    (zipmap (for [commit commits] (:id commit))
            (for [commit commits] (db-interface/read-branches-for-commit (:id commit)))))

(defn read-stat-for-week
    [year week author]
    (if (and week author)
        (let [calendar  (calendar/get-calendar-for-week (Integer. year) (Integer. week))
              first-day (calendar/get-first-day-of-week-formatted calendar)
              last-day  (calendar/get-last-day-of-week-formatted calendar)]
              (let [statistic-for-week  (db-interface/read-statistic-for-week-from-db first-day last-day author)
                    commits-for-week    (db-interface/read-commits-for-week first-day last-day author)
                    branches-per-commit (read-branches-per-commit commits-for-week)]
                   {:statistic-for-week  statistic-for-week
                    :commits-for-week    commits-for-week
                    :branches-per-commit branches-per-commit}))))

(defn file-operation
    [operation]
    (case operation
        ":delete" "Deleted file"
        ":add"    "Added file"
        ":edit"   "Modified file"
                operation))

(defn week-td
    [selected-author week-stat data]
    [:td [:a {:href (str "?author=" selected-author "&week=" (:week week-stat))} data]])

(defn graph-for-authors-data
    [authors selected-author]
    (for [author authors]
         (if (= (:author author) selected-author)
             (str "{data:[[1," (:commits_count author) "]], label: '" (:author author) "', pie:{explode:20}},\n")
             (str "{data:[[1," (:commits_count author) "]], label: '" (:author author) "'},\n"))))

(defn graph-for-weeks-stat
    [weeks-stat who what label]
    (str "{data:["
        (apply str (for [week-stat weeks-stat]
            (str "[" (:week week-stat) "," (get (get week-stat who) what) "],"))) "], label: '" label "'}"))

(defn get-weeks-stat
    [last-week author]
    (for [week (range 1 (inc last-week))] 
    (let [calendar  (calendar/get-calendar-for-week 2015 week)
          first-day (calendar/get-first-day-of-week-formatted calendar)
          last-day  (calendar/get-last-day-of-week-formatted calendar)
          stat-for-all (read-stat-per-weeks first-day last-day)
          stat-for-author (read-stat-per-weeks-for-author first-day last-day author)]
          {:week      week
           :first-day first-day
           :last-day  last-day
           :stat-for-all stat-for-all
           :stat-for-author stat-for-author})))

(defn continue-processing
    [html-output]
    (-> (http-response/response html-output)
        (http-response/content-type "text/html; charset=utf-8")))

(defn perform-index-page
    [request]
    (let [products (db-interface/read-product-names)
          authors  (db-interface/read-author-names)]
         (-> (html-renderer/render-index-page products authors @config/mailto)
             continue-processing)))

(defn get-data
    [weeks-stat who what]
    (for [week-stat weeks-stat]
        [(:week week-stat) (get (get week-stat who) what)]))

(defn get-cummulative-data
    [weeks-stat who what]
    (println weeks-stat)
    (for [week-stat weeks-stat]
        [(:week week-stat)
         (apply + (for [week-stat2 weeks-stat :while (<= (:week week-stat2) (:week week-stat))]
             (get (get week-stat2 who) what 0)))]))

(defn perform-author-page
    [request]
    (let [params (:params request)
          author-name     (get params "name")
          statistic       (db-interface/read-statistic-for-author author-name )
          last-week       (calendar/get-week (calendar/get-calendar))
          weeks-stat      (get-weeks-stat last-week author-name)
          week-graph-data [{:values (get-data weeks-stat :stat-for-author :commits-count) :label "Commits"}
                           {:values (get-data weeks-stat :stat-for-author :files-changed) :label "Files changed"}
                           {:values (get-data weeks-stat :stat-for-author :deletions) :label "Deletions"}
                           {:values (get-data weeks-stat :stat-for-author :insertions) :label "Insertions"}]
          cummulative-graph-data
                          [{:values (get-cummulative-data weeks-stat :stat-for-author :commits-count) :label "Commits"}
                           {:values (get-cummulative-data weeks-stat :stat-for-author :files-changed) :label "Files changed"}
                           {:values (get-cummulative-data weeks-stat :stat-for-author :deletions) :label "Deletions"}
                           {:values (get-cummulative-data weeks-stat :stat-for-author :insertions) :label "Insertions"}]
          ]
        (-> (html-renderer/render-author-page author-name statistic weeks-stat last-week week-graph-data cummulative-graph-data @config/mailto)
            continue-processing)))

(defn perform-author-week-page
    [request]
    (let [params          (:params request)
          author-name     (get params "name")
          selected-week   (get params "week")
          selected-year   (get params "year")
          weeks-stat      (get-weeks-stat (Integer/parseInt selected-year) (Integer/parseInt selected-week) author-name)
          stat-for-week   (read-stat-for-week selected-week author-name)
          ]
        (-> (html-renderer/render-author-week-page author-name selected-week stat-for-week @config/mailto)
            continue-processing)))

(defn perform-author-week-repo-page
    [request]
    (let [params          (:params request)
          author-name     (get params "name")
          selected-week   (get params "week")
          selected-year   (get params "year")
          product         (get params "product")
          repo            (get params "repo")
          weeks-stat      (get-weeks-stat (Integer/parseInt selected-year) (Integer/parseInt selected-week) author-name)
          stat-for-week   (read-stat-for-week selected-week author-name)
          ]
        (-> (html-renderer/render-author-week-repo-page author-name selected-week stat-for-week product repo @config/mailto)
            continue-processing)))

(defn perform-product-page
    [request]
    (let [params (:params request)
          product-name (get params "name")
          product-id   (db-interface/read-product-id product-name)
          repositories (db-interface/read-repo-list product-id)
          product-stat (read-stat-for-product      product-id @config/start-year @config/end-year)
          year-graph-data-1 [{:values (get-year-data product-stat :commits_count) :label "Commits"}]
          year-graph-data-2 [{:values (get-year-data product-stat :files_changed) :label "Files changed"}
                             {:values (get-year-data product-stat :insertions)    :label "Insertions"}
                             {:values (get-year-data product-stat :deletions)     :label "Deletions"}]]
          ;product-repo (read-stat-for-product-repo product-id @config/start-year @config/end-year)]
        (-> (html-renderer/render-product-page product-name repositories product-stat @config/mailto year-graph-data-1 year-graph-data-2)
            continue-processing)))

(defn perform-repository-page
    [request]
    (let [params (:params request)
          product-name    (get params "product")
          repository-name (get params "repository")
          product-id      (db-interface/read-product-id product-name)
          repo-stat       (read-stat-for-product-and-repo product-id repository-name @config/start-year @config/end-year)
          year-graph-data-1 [{:values (get-year-data repo-stat :commits_count) :label "Commits"}]
          year-graph-data-2 [{:values (get-year-data repo-stat :files_changed) :label "Files changed"}
                             {:values (get-year-data repo-stat :insertions)    :label "Insertions"}
                             {:values (get-year-data repo-stat :deletions)     :label "Deletions"}]]
        (-> (html-renderer/render-repository-page product-name repository-name repo-stat @config/mailto year-graph-data-1 year-graph-data-2)
            continue-processing)))

(defn update-file-name
    [file-name]
    (if (.startsWith file-name "/")
        (subs file-name 1)
        file-name))

(defn return-file
    [file-name content-type]
    (let [file (new java.io.File "www" (update-file-name file-name))]
        (println "Returning file " (.getAbsolutePath file))
        (if (.exists file)
            (-> (http-response/response file)
                (http-response/content-type content-type))
            (println "return-file(): can not access file: " (.getName file)))))

(defn return-icon
    [uri]
    (return-file uri "image/x-icon"))

(defn return-css
    [uri]
    (return-file uri "text/css"))

(defn return-javascript
    [uri]
    (return-file uri "application/javascript"))

(defn handler
    "Handler that is called by Ring for all requests received from user(s)."
    [request]
    (config/load-configuration)
    (println "request URI: " (request :uri))
    (let [uri (request :uri)]
        (condp = uri
            "/"                                 (perform-index-page request)
            "/author"                           (perform-author-page request)
            "/author-week"                      (perform-author-week-page request)
            "/author-week-repo"                 (perform-author-week-repo-page request)
            "/product"                          (perform-product-page request)
            "/repository"                       (perform-repository-page request)
            "/favicon.ico"                      (return-icon uri)
            "/bootstrap.min.css"                (return-css uri)
            "/arachnida.css"                    (return-css uri)
            "/bootstrap.min.js"                 (return-javascript uri)
            "/flotr/lib/prototype-1.6.0.2.js"   (return-javascript uri)
            "/flotr/lib/canvas2image.js"        (return-javascript uri)
            "/flotr/lib/canvastext.js"          (return-javascript uri)
            "/flotr/flotr-0.2.0-alpha.js"       (return-javascript uri)
            "/flotr/flotr.debug-0.2.0-alpha.js" (return-javascript uri))))

(def app
    (-> handler
        cookies/wrap-cookies
        (http-params/wrap-params :encoding "UTF-8")))

(defn start-server
    []
    (config/load-configuration)
    (config/print-configuration)
    (jetty/run-jetty app {:port @config/port}))

