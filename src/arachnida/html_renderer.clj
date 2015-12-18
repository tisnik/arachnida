;
;  (C) Copyright 2015  Pavel Tisnovsky
;
;  All rights reserved. This program and the accompanying materials
;  are made available under the terms of the Eclipse Public License v1.0
;  which accompanies this distribution, and is available at
;  http://www.eclipse.org/legal/epl-v10.html
;
;  Contributors:
;      Pavel Tisnovsky
;

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
    [product-name repositories statistic product-repo]
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 (str "Product: " product-name)]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Repositories"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                        (for [repository repositories]
                            [:tr
                                [:td (:name repository)]
                                [:td (:url  repository)]])
                    ]]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Summary for 2015"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                        [:tr [:td "Commits:"]
                             [:td (:commits_count statistic)]]
                        [:tr [:td "Insertions:"]
                             [:td (:insertions statistic)]]
                        [:tr [:td "Deletions:"]
                             [:td (:deletions statistic)]]
                        [:tr [:td "Changed files:"]
                             [:td (:files_changed statistic)]]
                    ]]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Per repository statistic"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                        [:tr [:th "Repository"] [:th {:colspan "4"} "Statistic"]]
                        [:tr [:th "&nbsp;"]
                             [:th "Commits"]
                             [:th "Files"]
                             [:th "Insertions"]
                             [:th "Deletions"]]
                        (for [pr product-repo]
                            [:tr [:td (:reponame pr)]
                                 [:td (:commits_count pr)]
                                 [:td (:files_changed pr)]
                                 [:td (:insertions pr)]
                                 [:td (:deletions pr)]
                            ]
                        )]]
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
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Summary for 2015"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                        [:tr [:td "Commits:"]
                             [:td (:commits_count statistic)]]
                        [:tr [:td "Insertions:"]
                             [:td (:insertions statistic)]]
                        [:tr [:td "Deletions:"]
                             [:td (:deletions statistic)]]
                        [:tr [:td "Changed files:"]
                             [:td (:files_changed statistic)]]
                    ]]
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
                [:br]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Week statistic"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
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
                    ]]
                [:br][:br][:br][:br]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-author-week-page
    [author-name selected-week stat-for-week]
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 (str "Author: " author-name)]
                [:h3 (str "Status for week: " selected-week)]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Week statistic"]
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                    [:tr [:th "Product"] [:th "Repository"] [:th "Commits"] [:th "Files"] [:th "Insertions"] [:th "Deletions"]]
                (for [s (:statistic-for-week stat-for-week)]
                    [:tr
                         [:td (:product s)]
                         [:td [:a {:href (str "/author-week-repo?name=" author-name "&week=" selected-week "&product=" (:product s) "&repo=" (:reponame s)) } (:reponame s)]]
                         [:td [:a {:href (str "/author-week-repo?name=" author-name "&week=" selected-week "&product=" (:product s) "&repo=" (:reponame s)) } (:commits s)]]
                         [:td (:files s)]
                         [:td (:insertions s)]
                         [:td (:deletions s)]
                     ])
                ]]
                [:br][:br][:br][:br]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-author-week-repo-page
    [author-name selected-week stat-for-week product repo]
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 (str "Author: " author-name)]
                [:h3 (str "Status for week: " selected-week)]
                [:h3 (str "Product " product)]
                [:h3 (str "Repository: " repo)]
                [:br]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Commits"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                        [:tr [:th "Date"] [:th "Message"] [:th "sha"]]
                    (for [c (:commits-for-week stat-for-week) :when (and (= repo (:repo c))
                                                                         (= product (:product c)))]
                        [:tr
                             [:td (:date c)]
                             [:td (:message c)]
                             [:td (:sha c)]
                         ])
                ]]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

