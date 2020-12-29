(defproject zero.one/fxl "0.0.5"
  :description "fxl is a Clojure spreadsheet library."
  :url "https://gitlab.com/zero-one-open-source/fxl"
  :license {:name "Apache License"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[expound "0.8.7"]
                 [failjure "2.0.0"]
                 [org.apache.poi/poi-ooxml "4.1.2"]
                 [org.clojure/math.combinatorics "0.1.6"]
                 [com.google.api-client/google-api-client           "1.30.4" ]
                 [com.google.apis/google-api-services-drive "v3-rev197-1.25.0"]
                 [com.google.oauth-client/google-oauth-client-jetty "1.30.6" ]
                 [com.google.apis/google-api-services-sheets "v4-rev581-1.25.0"]]
  :profiles {:dev      {:dependencies [[midje "1.9.9"]]
                        :plugins      [[lein-ancient "0.6.15"]
                                       [lein-midje "3.2.2"]
                                       [lein-cloverage "1.2.1"]]}
             :provided {:dependencies [[org.clojure/clojure "1.10.1"]]}})
