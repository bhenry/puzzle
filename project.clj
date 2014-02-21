(defproject puzzle "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [jayq "2.3.0"]
                 [prismatic/dommy "0.1.1"]
                 [yolk "0.9.0"]
                 [yolk-jquery "0.6.0"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :source-path "src/clj"
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:pretty-print true
                                   :output-to "resources/assets/js/main.js"
                                   :optimization :whitespace}}]})

