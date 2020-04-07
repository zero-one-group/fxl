(defproject zero.one/fxl "0.0.1-SNAPSHOT"
  :description "fxl is a Clojure spreadsheet library."
  :url "https://gitlab.com/zero-one-open-source/fxl"
  :license {:name "Apache License"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.apache.poi/poi-ooxml "4.1.2"]
                 [failjure "2.0.0"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [expound "0.8.4"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-cloverage "1.1.2"]
                             [lein-kibit "0.1.8"]]}})
