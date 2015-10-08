(ns arachnida.server)

(require '[ring.adapter.jetty      :as jetty])
(require '[ring.middleware.params  :as http-params])
(require '[ring.util.response      :as http-response])
(require '[ring.middleware.cookies :as cookies])

(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

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
                [:table
                (for [s (:commits-for-week stat-for-week)]
                         
                    [:tr (if (= (str (:id s)) commit-id) {:style "background-color:yellow"})
                         [:td (:project s)]
                         [:td (:repo s)]
                         [:td (:branch s)]
                         [:td [:a {:href (str "?author=" selected-author "&week=" selected-week "&commit-id=" (:id s))} (:sha s)]]
                         [:td (:message s)]
                         [:td (:date s)]
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

(defn handler
    [request]
    (let [params          (:params  request)
          cookies         (:cookies request)
          selected-author (get params "author")
          selected-week   (get params "week")
          commit-id       (get params "commit-id")
;         authors         (read-authors)
;         last-week       (get-week (get-calendar))
;         weeks-stat      (get-weeks-stat last-week selected-author)
;         stat-for-week   (read-stat-for-week selected-week selected-author)
;         commit-info     (read-commit-info commit-id)
          authors nil
          weeks-stat nil
          last-week nil
          stat-for-week nil
          commit-info nil
          ]
 ;         (println "Author:" selected-author)
 ;         (println "Authors:" authors)
 ;         (println "Weeks: " weeks-stat)
 ;         (println "Stat4week " stat-for-week)
          (println "Commit ID: " commit-id)
 ;         (println "Commit info " commit-info)
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

