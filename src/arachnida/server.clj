(ns arachnida.server)

(require '[ring.adapter.jetty      :as jetty])
(require '[ring.middleware.params  :as http-params])
(require '[ring.util.response      :as http-response])
(require '[ring.middleware.cookies :as cookies])

(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[arachnida.config        :as config])
(require '[arachnida.db-interface  :as db-interface])
(require '[arachnida.calendar      :as calendar])
(require '[arachnida.html-renderer :as html-renderer])

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
    [week author]
    (if (and week author)
        (let [calendar  (calendar/get-calendar-for-week 2015 (Integer. week))
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

(defn create-html-page
    [authors selected-author weeks-stat selected-week stat-for-week commit-id commit-info]
    (page/xhtml
        [:head
            [:title "Stats"]
            [:meta {:name "Generator" :content "Clojure"}]
            [:meta {:http-equiv "Content-type" :content "text/html; charset=utf-8"}]
            [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/lib/prototype-1.6.0.2.js"}]
            [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/lib/canvas2image.js"}]
            [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/lib/canvastext.js"}]
            [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/flotr.debug-0.2.0-alpha.js"}]
        ]
        [:body
            [:h1 "Authors"]
                [:table {:style "border:2px solid brown"}
                    [:tr [:td "Name"] [:td "Commits"] [:td "Files changed"] [:td "Insertions"] [:td "Deletions"]]
                (for [author authors]
                    [:tr (if (= (:author author) selected-author) {:style "background-color:yellow"})
                        [:td [:a {:href (str "?author=" (:author author) "&week=" selected-week)} (:author author)]]
                        [:td (:commits_count author)]
                        [:td (:files_changed author)]
                        [:td (:insertions author)]
                        [:td (:deletions author)]
                    ])
                ]
                [:div {:id "authors-graph" :style "width:500px;height:500px"}
                    [:script {:type "text/javascript"}
                    "var f = Flotr.draw($('authors-graph'), ["
                          (graph-for-authors-data authors selected-author)
                          "], {
                          HtmlText: false, 
                          grid: {
                              verticalLines:   true, 
                              horizontalLines: true
                          },
                          xaxis: {showLabels: false},
                          yaxis: {showLabels: false}, 
                          pie: {show: true, explode: 5},
                          legend:{
                              show: true,
                              position: 'ne',
                              backgroundColor: '#D2E8FF',
                          }});"
                    ]
                ]
                [:div {:id "weeks-stat" :style "width:900px;height:400px"}
                    [:script {:type "text/javascript"}
                    "var f = Flotr.draw($('weeks-stat'), ["
                          (graph-for-weeks-stat weeks-stat :stat-for-all :commits-count "Commits (all)") ","
                          (graph-for-weeks-stat weeks-stat :stat-for-author :commits-count "Commits (author)") ","
                          (graph-for-weeks-stat weeks-stat :stat-for-all :files-changed "Files changed (all)") ","
                          (graph-for-weeks-stat weeks-stat :stat-for-author :files-changed "Files changed (author)")
                          "], {
                          HtmlText: false, 
                          grid: {
                              verticalLines:   true, 
                              horizontalLines: true
                          },
                          xaxis: {showLabels: true},
                          yaxis: {showLabels: true}, 
                          mouse: {
	track: true,
	position: 'se',
	margin: 3,
	color: '#ff3f19',
	trackDecimals: 1,
	radius: 3,
	sensibility: 2
},
                          //bars: {show:true},
                          legend:{
                              show: true,
                              position: 'ne',
                              backgroundColor: '#D2E8FF',
                          }});"
                    ]
                ]
            [:h1 "Week stat"]
                [:table {:style "border:2px solid brown"}
                    [:tr [:th "Week"] [:th "From"] [:th "To"]
                         [:th {:colspan "4"} "Stat for all writers"]
                         [:th {:colspan "4"} "Stat for selected writer"]
                    ]
                    [:tr [:th "&nbsp;"] [:th "&nbsp;"] [:th "&nbsp;"]
                         [:th "Commits"]
                         [:th "Files"]
                         [:th "Insertions"]
                         [:th "Deletions"]
                         [:th "Commits"]
                         [:th "Files"]
                         [:th "Insertions"]
                         [:th "Deletions"]]
                (for [week-stat weeks-stat]
                    [:tr
                         (if (= (str (:week week-stat)) selected-week) {:style "background-color:yellow"})
                         (week-td selected-author week-stat (:week week-stat))
                         (week-td selected-author week-stat (:first-day week-stat))
                         (week-td selected-author week-stat (:last-day week-stat))
                         [:td {:style "text-align:right"} (:commits-count (:stat-for-all week-stat))]
                         [:td {:style "text-align:right"} (:files-changed (:stat-for-all week-stat))]
                         [:td {:style "text-align:right"} (:insertions (:stat-for-all week-stat))]
                         [:td {:style "text-align:right"} (:deletions (:stat-for-all week-stat))]
                         [:td {:style "text-align:right"} (:commits-count (:stat-for-author week-stat))]
                         [:td {:style "text-align:right"} (:files-changed (:stat-for-author week-stat))]
                         [:td {:style "text-align:right"} (:insertions (:stat-for-author week-stat))]
                         [:td {:style "text-align:right"} (:deletions (:stat-for-author week-stat))]
                     ])
                ]
            [:h1 "Stat for selected week"]
                [:table
                    [:tr [:th "Repository"]
                         [:th "Commits"]
                         [:th "Files"]
                         [:th "Insertions"]
                         [:th "Deletions"]]
                (for [s (:statistic-for-week stat-for-week)]
                    [:tr [:td (:repo s)]
                         [:td (:commits s)]
                         [:td (:files s)]
                         [:td (:insertions s)]
                         [:td (:deletions s)]
                    ])]
            [:h1 "Commits for selected week"]
                [:table {:border 1}
                (for [s (:commits-for-week stat-for-week)]
                         
                    [:tr (if (= (str (:id s)) commit-id) {:style "background-color:yellow"})
                         [:td (:product s)]
                         [:td (:repo s)]
                         [:td (for [branch (get (:branches-per-commit stat-for-week) (:id s))]
                                  (str (:branch branch) "<br/>"))]
                         [:td (:date s)]
                         [:td [:a {:href (str "?author=" selected-author "&week=" selected-week "&commit-id=" (:id s))} (:sha s)]]
                         [:td (:message s)]
                    ]
                )]
            [:h1 "Changes made in selected commit"]
                [:table
                (for [c commit-info]
                    [:tr
                         [:td (file-operation (:operation c))]
                         [:td (:file_name c)]
                    ]
                )]
        ]))

(defn generate-response
    [authors selected-author weeks-stat selected-week stat-for-week commit-id commit-info]
    (let [html-output (create-html-page authors selected-author weeks-stat selected-week stat-for-week commit-id commit-info)]
          (-> (http-response/response html-output)
              (http-response/content-type "text/html; charset=utf-8"))))

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

(defn handler-
    [request]
    (let [params          (:params  request)
          cookies         (:cookies request)
          selected-author (get params "author")
          selected-week   (or (get params "week") "1")
          commit-id       (get params "commit-id")
          authors         (db-interface/read-authors)
          last-week       (calendar/get-week (calendar/get-calendar))
          weeks-stat      (get-weeks-stat last-week selected-author)
          stat-for-week   (read-stat-for-week selected-week selected-author)
          commit-info     (db-interface/read-commit-info commit-id)
          ]
        ;  (println "Author:" selected-author)
        ;  (println "Authors:" authors)
        ;  (println "Weeks: " weeks-stat)
          (println "Stat4week " stat-for-week)
        ;  (println "Commit ID: " commit-id)
         ; (println "Commit info " commit-info)
        (let [response (generate-response authors selected-author weeks-stat selected-week stat-for-week commit-id commit-info)]
              (println "Outgoing cookies: " (get response :cookies))
              response)))

(defn continue-processing
    [html-output]
    (-> (http-response/response html-output)
        (http-response/content-type "text/html; charset=utf-8")))

(defn perform-index-page
    [request]
    (let [products (db-interface/read-product-names)
          authors  (db-interface/read-author-names)]
         (-> (html-renderer/render-index-page products authors)
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
          author-name (get params "name")
          statistic   (db-interface/read-statistic-for-author author-name)
          last-week       (calendar/get-week (calendar/get-calendar))
          weeks-stat      (get-weeks-stat last-week author-name)
          stat-for-week   (read-stat-for-week last-week author-name)
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
        (-> (html-renderer/render-author-page author-name statistic weeks-stat last-week week-graph-data cummulative-graph-data)
            continue-processing)))

(defn perform-product-page
    [request]
    (let [params (:params request)
          product-name (get params "name")
          product-id   (db-interface/read-product-id product-name)
          repositories (db-interface/read-repo-list product-id)]
        (-> (html-renderer/render-product-page product-name repositories)
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
    (println "request URI: " (request :uri))
    (let [uri (request :uri)]
        (condp = uri
            "/"                                 (perform-index-page request)
            "/author"                           (perform-author-page request)
            "/product"                          (perform-product-page request)
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
    (jetty/run-jetty app {:port 8080}))

