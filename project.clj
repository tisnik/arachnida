(defproject arachnida "0.1.0-SNAPSHOT"
    :description "FIXME: write description"
    :url "http://example.com/FIXME"
    :license {:name "Eclipse Public License"
              :url "http://www.eclipse.org/legal/epl-v10.html"}
    :dependencies [[org.clojure/clojure "1.6.0"]
                   [clj-jgit "0.8.0"]
                   [clj-rm-rf "1.0.0-SNAPSHOT"]
                   [org.clojure/java.jdbc "0.3.5"]
                   [org.xerial/sqlite-jdbc "3.7.2"]
                   [org.clojure/tools.cli "0.3.1"]
                   [hiccup "1.0.4"]
                   [ring/ring-core "1.3.2"]
                   [ring/ring-jetty-adapter "1.3.2"]]
    :dev-dependencies [[lein-ring "0.8.10"]]
    :plugins [[lein-ring "0.8.10"]]
    :main ^:skip-aot arachnida.core
    :target-path "target/%s"
    :ring {:handler arachnida.server/app}
    :profiles {:uberjar {:aot :all}})
