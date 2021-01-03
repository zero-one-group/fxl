(ns zero-one.fxl.google-sheets-test
  (:require [clojure.java.io]
            [clojure.spec.alpha :as s]
            [clojure.string]
            [midje.sweet :refer [facts fact => throws]]
            [zero-one.fxl.google-sheets :as gs]
            [zero-one.fxl.specs :as fs]))

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

(defn valid-credentials? []
  (try
    (let [credentials-path "resources/credentials.json"]
      (and (.exists (clojure.java.io/file credentials-path))
           (seq (clojure.string/trim (slurp credentials-path)))))
    (catch Exception e (str "caught exception: " (.getMessage e)))))

;; (when valid-credentials?
;;   (defonce service (gs/sheets-service google-props))

;;   (facts "On gs/read-google-sheets!"
;;          (let [spreadsheet-id "1_8g_ItFMIgpCMFIQ1L1CTRhF4oKsjTs4zYe0UMRSd-w"
;;                sheet-names    (gs/sheet-names! service spreadsheet-id)
;;                cells          (gs/read-google-sheets! service spreadsheet-id (first sheet-names))
;;                values         (->> cells (map :value) set)]
;;            (fact "Read cells should all be valid"
;;                  (filter #(not (s/valid? ::fs/cell %)) cells) => ())
;;            (fact "There should be 15 cells"
;;                  (count cells) => 15)
;;            (fact "Values should be extracted"
;;                  (contains? values "1.4142") => true))))
