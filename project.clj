(defproject zero.one/fxl "0.0.4"
  :description "fxl is a Clojure spreadsheet library."
  :url "https://gitlab.com/zero-one-open-source/fxl"
  :license {:name "Apache License"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/math.combinatorics "0.1.6"]
                 [org.apache.poi/poi-ooxml "5.0.0"]
                 [failjure "2.1.1"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [expound "0.8.7"]]
                   :plugins [[lein-ancient "0.7.0"]
                             [lein-midje "3.2.2"]
                             [lein-cloverage "1.2.2"]]}
             :provided {:dependencies [[org.clojure/clojure "1.10.1"]]}})
