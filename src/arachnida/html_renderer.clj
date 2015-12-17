(ns arachnida.html-renderer)

(require '[hiccup.core         :as hiccup])
(require '[hiccup.page         :as page])
(require '[hiccup.form         :as form])

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

(defn render-author-page
    [author-name statistic]
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
                [:br][:br][:br][:br]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

