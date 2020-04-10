(ns zero-one.fxl.write-xlsx-test
  (:require
    [midje.sweet :refer [facts fact =>]]
    [zero-one.fxl.write-xlsx :as write-xlsx]))

(def example-cells
  [{:value "X" :coord {:row 0 :col 0} :style {:row-size 1 :col-size 9}}
   {:value "Y" :coord {:row 0 :col 1} :style {:row-size 2 :col-size 8}}
   {:value "Z" :coord {:row 0 :col 2} :style {}}
   {:value 123 :coord {:row 1 :col 0} :style {:row-size 4 :col-size 6}}
   {:value 456 :coord {:row 1 :col 1} :style {:row-size 5 :col-size 5}}
   {:value 789 :coord {:row 1 :col 2} :style {:row-size 6 :col-size 4}}
   {:value nil :coord {:row 2 :col 0} :style {}}
   {:value nil :coord {:row 2 :col 1} :style {}}
   {:value nil :coord {:row 2 :col 2} :style {:col-size :auto}}])

(facts "On write-xlsx/build-context"
  (let [context (write-xlsx/spreadsheet-context example-cells)]
    (fact "Correct minimum row and col sizes"
      context => {:min-row-sizes {0 2
                                  1 6}
                  :min-col-sizes {0 9
                                  1 8
                                  2 :auto}})))
