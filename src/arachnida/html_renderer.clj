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
    [mailto]
    [:div (str "<br /><br /><br /><br />Author: Pavel Tisnovsky "
        (if mailto
            (str "&lt;<a href='mailto:" mailto "'>" mailto "</a>&gt;"))
          "&nbsp;&nbsp;&nbsp;"
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
    [products writers mailto]
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
                (render-html-footer mailto)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-value
    [s k]
    [:td {:style "text-align:right"} (or (get (val s) k) "0")])

(defn url-to-repo-page
    [product-name repository]
    [:a {:href (str "/repository?product=" product-name "&repository=" repository)} repository])

(defn url-to-gitlab
    [url]
    (let [url2 (subs url (inc (.indexOf url ":")))]
    [:a {:href (str "http://gitlab.cee.redhat.com/" url2)} url]))

(defn render-product-page
    [product-name repositories statistic mailto year-graph-data-1 year-graph-data-2]
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
                                [:td (url-to-repo-page product-name (:name repository))]
                                [:td (url-to-gitlab (:url  repository))]])
                    ]]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Summary for all repositories"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                        [:tr [:th "Year"]
                             (for [s statistic]
                                  [:th {:style "width:12ex;text-align:right"} (key s)])]
                        [:tr [:th "Commits:"]
                             (for [s statistic]
                                  (render-value s :commits_count))]
                        [:tr [:th "Insertions:"]
                             (for [s statistic]
                                  (render-value s :insertions))]
                        [:tr [:th "Deletions:"]
                             (for [s statistic]
                                  (render-value s :deletions))]
                        [:tr [:th "Changed files:"]
                             (for [s statistic]
                                  (render-value s :files_changed))]
                    ]]
                    [:h2 "Commits"]
                    (flotr/line-chart "year commits" "800px" "300px" year-graph-data-1
                                      :horizontal-lines true
                                      :vertical-lines true
                                      :show-legend true
                                      :legend-positon "sw")
                    [:h2 "Insertions, deletions and file changed"]
                    (flotr/line-chart "year statistic" "800px" "300px" year-graph-data-2
                                      :horizontal-lines true
                                      :vertical-lines true
                                      :show-legend true
                                      :legend-positon "sw")
                [:br][:br][:br][:br]
                (render-html-footer mailto)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-repository-page
    [product-name repository-name statistic mailto year-graph-data-1 year-graph-data-2]
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 (str "Product: " product-name)]
                [:h2 (str "Repository: " repository-name)]
                [:br]
                [:br]
                [:div {:class "panel panel-primary"}
                    [:div {:class "panel-heading"}
                        "Statistic for repository"]
                    [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                        [:tr [:th "Year"]
                        (for [s statistic]
                             [:th {:style "width:12ex;text-align:right"} (key s)])]
                        [:tr [:th "Commits:"]
                             (for [s statistic]
                                  (render-value s :commits_count))]
                        [:tr [:th "Insertions:"]
                             (for [s statistic]
                                  (render-value s :insertions))]
                        [:tr [:th "Deletions:"]
                             (for [s statistic]
                                  (render-value s :deletions))]
                        [:tr [:th "Changed files:"]
                             (for [s statistic]
                                  (render-value s :files_changed))]
                    ]]
                    [:h2 "Commits"]
                    (flotr/line-chart "year commits" "800px" "300px" year-graph-data-1
                                      :horizontal-lines true
                                      :vertical-lines true
                                      :show-legend true
                                      :legend-positon "sw")
                    [:h2 "Insertions, deletions and file changed"]
                    (flotr/line-chart "year statistic" "800px" "300px" year-graph-data-2
                                      :horizontal-lines true
                                      :vertical-lines true
                                      :show-legend true
                                      :legend-positon "sw")
                [:br][:br][:br][:br]
                (render-html-footer mailto)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn week-td
    [selected-author week-stat data]
    [:td [:a {:href (str "/author-week?name=" selected-author "&week=" (:week week-stat))} data]])

(defn render-author-page
    [author-name statistic weeks-stat selected-week week-graph-data cummulative-graph-data mailto]
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
                (render-html-footer mailto)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-author-week-page
    [author-name selected-week stat-for-week mailto]
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
                (render-html-footer mailto)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-author-week-repo-page
    [author-name selected-week stat-for-week product repo mailto]
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
                (render-html-footer mailto)
            ] ; </div class="container">
        ] ; </body>
    ))

