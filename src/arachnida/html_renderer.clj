(ns arachnida.html-renderer)

(require '[hiccup.core                   :as hiccup])
(require '[hiccup.page                   :as page])
(require '[hiccup.form                   :as form])

(defn render-html-header
    "Renders part of HTML page - the header."
    []
    [:head
        [:title "Arachnida"]
        [:meta {:name "Author"    :content "Pavel Tisnovsky"}]
        [:meta {:name "Generator" :content "Clojure"}]
        [:meta {:http-equiv "Content-type" :content "text/html; charset=utf-8"}]
        (page/include-css "http://10.34.3.139/bootstrap.min.css")
        (page/include-css "http://10.34.3.139/bootstrap.min.css")
        (page/include-css "http://10.34.3.139/arachnida.css")
        (page/include-js  "http://10.34.3.139/bootstrap.min.js")
        (page/include-js  "http://10.34.3.139/flotr/lib/prototype-1.6.0.2.js")
        (page/include-js  "http://10.34.3.139/flotr/lib/canvas2image.js")
        (page/include-js  "http://10.34.3.139/flotr/lib/canvastext.js")
        (page/include-js  "http://10.34.3.139/flotr/flotr.debug-0.2.0-alpha.js")
    ] ; head
)

(defn render-html-footer
    "Renders part of HTML page - the footer."
    []
    [:div (str "<br /><br /><br /><br />Author: Pavel Tisnovsky &lt;<a href='mailto:ptisnovs@redhat.com'>ptisnovs@redhat.com</a>&gt;&nbsp;&nbsp;&nbsp;"
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
    []
    (page/xhtml
        (render-html-header)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section)
                [:h1 "Products"]
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                ]
                [:h1 "Writers"]
                [:table {:class "table table-condensed table-hover table-bordered" :rules "all"}
                ]
                [:br][:br][:br][:br]
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

