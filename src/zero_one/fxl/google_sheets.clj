(ns zero-one.fxl.google-sheets
  (:require [clojure.java.io]
            [zero-one.fxl.core :as fxl])
  (:import
   (com.google.api.client.googleapis.auth.oauth2 GoogleCredential)
   (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
   (com.google.api.client.json.jackson2 JacksonFactory)
   (com.google.api.services.sheets.v4 SheetsScopes
                                      Sheets$Builder)))

(def json-factory (JacksonFactory/getDefaultInstance))

(def http-transport (GoogleNetHttpTransport/newTrustedTransport))

(defn- google-credentials [creds-path]
  (-> (GoogleCredential/fromStream (clojure.java.io/input-stream creds-path))
      (.createScoped [SheetsScopes/SPREADSHEETS])))

(defn sheets-service [google-props]
  (let [app-name    (:app-name google-props "fxl")
        credentials (google-credentials (:credentials google-props))]
    (-> (Sheets$Builder. http-transport json-factory credentials)
        (.setApplicationName app-name)
        .build)))

(defn exponential-backoff [{:keys [wait-ms growth-rate max-ms action!] :as options}]
  (if (<= max-ms wait-ms)
    (action!)
    (try
      (action!)
      (catch Throwable _
        (Thread/sleep wait-ms)
        (exponential-backoff (update options :wait-ms (partial * growth-rate)))))))

(defn safely-execute! [service]
  (Thread/sleep (+ 250 (rand-int 250)))
  (exponential-backoff {:wait-ms     1000
                        :growth-rate 2
                        :max-ms      16000
                        :action!     #(.execute service)}))

(defn sheet-names! [service spreadsheet-id]
  (let [sheet-objs (-> service
                       .spreadsheets
                       (.get spreadsheet-id)
                       safely-execute!
                       .getSheets)]
    (map #(-> % .getProperties .getTitle) sheet-objs)))

(defn sheet-values! [service spreadsheet-id sheet-name]
  (let [sheet-name (or sheet-name
                       (first (sheet-names! service spreadsheet-id)))
        value-objs (-> service
                       .spreadsheets
                       .values
                       (.get spreadsheet-id sheet-name)
                       safely-execute!
                       .getValues)]
    value-objs))

(defn read-google-sheets! [service spreadsheet-id sheet-name]
  (-> (sheet-values! service spreadsheet-id sheet-name) 
      fxl/table->cells))

(comment
  (let [google-props   {:credentials "resources/credentials.json"}
        service        (sheets-service google-props)
        spreadsheet-id "1_8g_ItFMIgpCMFIQ1L1CTRhF4oKsjTs4zYe0UMRSd-w"
        sheet-names    (sheet-names! service spreadsheet-id)
        cells          (read-google-sheets! service spreadsheet-id (first sheet-names))
        values         (->> cells (map :value) set)
        ]
    (sheet-values! service spreadsheet-id (first sheet-names))
    )
  )
