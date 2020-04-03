(defproject fxl "0.0.1-SNAPSHOT"
  :description "Cool new project to do things and stuff"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.apache.poi/poi-ooxml "4.1.2"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [expound "0.8.4"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-cloverage "1.1.2"]
                             [lein-kibit "0.1.8"]]}

             ;; You can add dependencies that apply to `lein midje` below.
             ;; An example would be changing the logging destination for test runs.
             :midje {}})
             ;; Note that Midje itself is in the `dev` profile to support
             ;; running autotest in the repl.
