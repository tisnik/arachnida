(ns arachnida.html-renderer)

(require '[hiccup.core         :as hiccup])
(require '[hiccup.page         :as page])
(require '[hiccup.form         :as form])
(require '[clj-flotr.generator :as flotr])

(require '[arachnida.config    :as config])

(defn render-html-header
    "Renders part of HTML page - the header."
    []
    [:head
        [:title "Arachnida"]
        [:meta {:name "Author"    :content "Pavel Tisnovsky"}]
        [:meta {:name "Generator" :content "Clojure"}]
        [:meta {:http-equiv "Content-type" :content "text/html; charset=utf-8"}]
        (page/include-css (str @config/url-to-common-files "bootstrap.min.css"))
        (page/include-css (str @config/url-to-common-files "arachnida.css"))
        (page/include-js  (str @config/url-to-common-files "bootstrap.min.js"))
        (page/include-js  (str @config/url-to-common-files "flotr/lib/prototype-1.6.0.2.js"))
        (page/include-js  (str @config/url-to-common-files "flotr/lib/canvas2image.js"))
        (page/include-js  (str @config/url-to-common-files "flotr/lib/canvastext.js"))
        (page/include-js  (str @config/url-to-common-files "flotr/flotr.debug-0.2.0-alpha.js"))
    ] ; head
)

(defn render-html-footer
    "Renders part of HTML page - the footer."
    []
    [:div (str "<br /><br /><br /><br />Author: Pavel Tisnovsky &lt;&gt;&nbsp;&nbsp;&nbsp;"
          "<br />")])

(defn render-navigation-bar-section
    "Renders whole navigation bar."
    []
    [:nav {:class "navbar navbar-inverse navbar-fixed-top" :role "navigation"}
        [:div {:class "container-fluid"}
            [:div {:class "row"}
                [:div {:class "col-md-4"}
                    [:div {:class "navbar-header"}
                        [:a {:href "/" :class "navbar-brand"} "Arachnida"]
                    ] ; ./navbar-header
                ] ; col ends
                [:div {:class "col-md-4"}
                    [:div {:class "navbar-header"}
                        [:div {:class "navbar-brand"}
                            ""
                        ]
                    ]
                ] ; col ends
                [:div {:class "col-md-4"}
                    ""
                ] ; col ends
            ] ; row ends
        ] ; /.container-fluid
]); </nav>

(defn render-index-page
    [products writers]
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 "Products"]
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                    (for [product products]
                        [:tr [:td [:a {:href (str "/product?name=" (:name product)) } (:name product)]]])
                ]
                [:h1 "Writers"]
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                    (for [writer writers]
                        [:tr [:td [:a {:href (str "/author?name=" (:author writer)) } (:author writer)]]])
                ]
                [:br][:br][:br][:br]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-product-page
    [product-name repositories]
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 (str "Product: " product-name)]
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                    (for [repository repositories]
                        [:tr
                            [:td (:name repository)]
                            [:td (:url repository)]])
                ]
                [:br][:br][:br][:br]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn week-td
    [selected-author week-stat data]
    [:td [:a {:href (str "/author-week?name=" selected-author "&week=" (:week week-stat))} data]])

(defn render-author-page
    [author-name statistic weeks-stat selected-week week-graph-data cummulative-graph-data]
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 (str "Author: " author-name)]
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                    [:tr [:th {:colspan 2} "Summary for 2015"]]
                    [:tr [:td "Commits:"]
                         [:td (:commits_count statistic)]]
                    [:tr [:td "Insertions:"]
                         [:td (:insertions statistic)]]
                    [:tr [:td "Deletions:"]
                         [:td (:deletions statistic)]]
                    [:tr [:td "Changed files:"]
                         [:td (:files_changed statistic)]]
                ]
                [:h3 "Week stat"]
                (flotr/line-chart "weeks statistic" "800px" "300px" week-graph-data
                                  :horizontal-lines true
                                  :vertical-lines true
                                  :show-legend true
                                  :legend-positon "sw")
                [:h3 "Cummulative"]
                (flotr/line-chart "cummulative statistic" "800px" "300px" cummulative-graph-data
                                  :horizontal-lines true
                                  :vertical-lines true
                                  :show-legend true
                                  :legend-positon "sw")
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                    [:tr [:th {:colspan 3} "Week statistic"]]
                    [:tr [:th "Week"] [:th "From"] [:th "To"] [:th {:colspan "4"} "Statistic"]]
                    [:tr [:th "&nbsp;"] [:th "&nbsp;"] [:th "&nbsp;"]
                         [:th "Commits"]
                         [:th "Files"]
                         [:th "Insertions"]
                         [:th "Deletions"]]
                (for [week-stat weeks-stat]
                    [:tr
                         (if (= (str (:week week-stat)) selected-week) {:style "background-color:yellow"})
                         (week-td author-name week-stat (:week week-stat))
                         (week-td author-name week-stat (:first-day week-stat))
                         (week-td author-name week-stat (:last-day week-stat))
                         [:td {:style "text-align:right"} (:commits-count (:stat-for-author week-stat))]
                         [:td {:style "text-align:right"} (:files-changed (:stat-for-author week-stat))]
                         [:td {:style "text-align:right"} (:insertions (:stat-for-author week-stat))]
                         [:td {:style "text-align:right"} (:deletions (:stat-for-author week-stat))]
                     ])
                ]
                [:br][:br][:br][:br]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

