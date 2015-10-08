(ns arachnida.server)

(require '[ring.adapter.jetty      :as jetty])
(require '[ring.middleware.params  :as http-params])
(require '[ring.util.response      :as http-response])
(require '[ring.middleware.cookies :as cookies])

(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[arachnida.db-interface :as db-interface])
(require '[arachnida.calendar     :as calendar])

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

(defn read-stat-for-week
    [week author]
    (if (and week author)
        (let [calendar  (calendar/get-calendar-for-week 2015 (Integer. week))
              first-day (calendar/get-first-day-of-week-formatted calendar)
              last-day  (calendar/get-last-day-of-week-formatted calendar)]
              {:statistic-for-week (db-interface/read-statistic-for-week-from-db first-day last-day author)
               :commits-for-week   (db-interface/read-commits-for-week first-day last-day author)})))

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

(defn create-html-page
    [authors selected-author weeks-stat selected-week stat-for-week commit-id commit-info]
    (page/xhtml
        [:head
            [:title "Stats"]
            [:meta {:name "Generator" :content "Clojure"}]
            [:meta {:http-equiv "Content-type" :content "text/html; charset=utf-8"}]
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
                         [:td (:branch s)]
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

(defn handler
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
          (println "Author:" selected-author)
          (println "Authors:" authors)
          (println "Weeks: " weeks-stat)
          (println "Stat4week " stat-for-week)
          (println "Commit ID: " commit-id)
          (println "Commit info " commit-info)
        (let [response (generate-response authors selected-author weeks-stat selected-week stat-for-week commit-id commit-info)]
              (println "Outgoing cookies: " (get response :cookies))
              response)))

(def app
    (-> handler
        cookies/wrap-cookies
        http-params/wrap-params))

(defn start-server
    []
    (jetty/run-jetty app {:port 8080}))

