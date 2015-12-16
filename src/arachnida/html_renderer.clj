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
        ;(page/include-css "http://torment.usersys.redhat.com/openjdk/style.css")]
        (page/include-css "http://10.34.3.139/bootstrap.min.css")
        (page/include-js  "http://10.34.3.139/bootstrap.min.js")
        [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/lib/prototype-1.6.0.2.js"}]
        [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/lib/canvas2image.js"}]
        [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/lib/canvastext.js"}]
        [:script {:type "text/javascript" :src "http://10.34.3.139/flotr/flotr.debug-0.2.0-alpha.js"}]
    ] ; head
)

(defn render-html-footer
    "Renders part of HTML page - the footer."
    []
    [:div (str "<br /><br /><br /><br />Author: Pavel Tisnovsky &lt;<a href='mailto:ptisnovs@redhat.com'>ptisnovs@redhat.com</a>&gt;&nbsp;&nbsp;&nbsp;"
          "<br />")])

