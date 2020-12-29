(ns zero-one.fxl.google-sheets-test
  (:require   [midje.sweet :refer [facts fact => throws]]
              [zero-one.fxl.specs :as fs]
              [clojure.spec.alpha :as s]
              [clojure.java.io]
              [zero-one.fxl.google-sheets :as gs]))

(def google-props
  {:credentials "resources/credentials.json"})

(defn action-fn [retries-to-success]
  (let [n-tries (atom 0)]
    (fn []
      (swap! n-tries inc)
      (if (<= @n-tries retries-to-success)
        (throw (Exception. "Try again"))
        @n-tries))))

(facts "On exponential-backoff"
       (gs/exponential-backoff {:wait-ms     100
                                :growth-rate 2
                                :max-ms      399
                                :action!     (action-fn 0)}) => 1
       (gs/exponential-backoff {:wait-ms     100
                                :growth-rate 2
                                :max-ms      399
                                :action!     (action-fn 1)}) => 2
       (gs/exponential-backoff {:wait-ms     100
                                :growth-rate 2
                                :max-ms      399
                                :action!     (action-fn 2)}) => 3
       (gs/exponential-backoff {:wait-ms     100
                                :growth-rate 2
                                :max-ms      399
                                :action!     (action-fn 3)}) => (throws Exception))

;; HACK: To remove check after secret is added
(when (.exists (clojure.java.io/file "resources/credentials.json"))
  (defonce service (gs/sheets-service google-props))

  (facts "On gs/read-google-sheets!"
         (let [spreadsheet-id "1_8g_ItFMIgpCMFIQ1L1CTRhF4oKsjTs4zYe0UMRSd-w"
               sheet-names    (gs/sheet-names! service spreadsheet-id)
               cells          (gs/read-google-sheets! service spreadsheet-id (first sheet-names))
               values         (->> cells (map :value) set)]
           (fact "Read cells should all be valid"
                 (filter #(not (s/valid? ::fs/cell %)) cells) => ())
           (fact "There should be 15 cells"
                 (count cells) => 15)
           (fact "Values should be extracted"
                 (contains? values "1.4142") => true))))
