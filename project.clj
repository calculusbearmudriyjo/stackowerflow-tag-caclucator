(defproject stackex "0.1.0-SNAPSHOT"
  :description "stackowerflow statistic tag's calculator"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.immutant/web "2.1.9"]
                 [compojure "1.6.0"]
                 [org.clojure/core.async "0.4.474"]
                 [com.taoensso/timbre "4.10.0"]
                 [yogthos/config "0.9"]
                 [http-kit "2.2.0"]
                 [cheshire "5.8.0"]
                 [ring/ring-json "0.4.0"]]

  :plugins [[lein-ring "0.9.7"]
            [cider/cider-nrepl "0.16.0"]]
  
  :main ^:skip-aot stackex.core

  :target-path "target/%s"
  :uberjar-name "stackex.jar"
  
  :profiles {:uberjar {:aot :all}
             :resource-path ["resources/"]})
