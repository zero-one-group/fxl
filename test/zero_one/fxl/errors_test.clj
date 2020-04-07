(ns zero-one.fxl.errors-test
  (:require
    [clojure.string :as str]
    [failjure.core :as f]
    [midje.sweet :refer [facts fact =>]]
    [zero-one.fxl.core :as fxl]))

(defmacro with-out-str-and-value
  ; Source: https://stackoverflow.com/questions/7150776/capturing-the-original-return-value-from-with-out-str
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [v# ~@body]
         (vector (str s#)
                 v#)))))

(facts "On non-existent paths"
  (fact "fxl/read-xlsx! should fail gracefully"
    (fxl/read-xlsx! "non-existent-dir/non-existent.xlsx") => f/failed?)
  (fact "fxl/write-xlsx! should fail gracefully"
    (fxl/write-xlsx! [] "non-existent-dir/some.xlsx") => f/failed?))

(facts "On using invalid styles"
  (fact "invalid style should fail gracefully"
    (let [invalid-style {:font-colour :some-undefined-colour}
          cells         [(fxl/->cell {:style invalid-style})]
          write-fn      #(fxl/write-xlsx! cells "target/temp.xlsx")
          [msg result]  (with-out-str-and-value (write-fn))]
      result => f/failed?
      (str/lower-case msg) => #(str/includes? % "spec failed")))
  (fact "invalid coord should fail gracefully"
    (let [cells      [(fxl/->cell {:coord {:row "abc"}})]
          [_ result] (with-out-str-and-value
                       (fxl/write-xlsx! cells "target/temp.xlsx"))]
      result => f/failed?))
  (fact "invalid value should fail gracefully"
    (let [cells      [(fxl/->cell {:value [1 2 3]})]
          [_ result] (with-out-str-and-value
                       (fxl/write-xlsx! cells "target/temp.xlsx"))]
      result => f/failed?)))
