(ns zero-one.fxl.write-xlsx-test
  (:require
    [midje.sweet :refer [facts fact =>]]
    [zero-one.fxl.write-xlsx :as write-xlsx])
  (:import
    (org.apache.poi.xssf.usermodel XSSFWorkbook)))

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
  (let [workbook (XSSFWorkbook.)
        context  (write-xlsx/build-context! workbook example-cells)]
    (fact "Correct minimum row and col sizes"
      (:min-row-sizes context) => {{:row 0 :sheet "Sheet1"} 2
                                   {:row 1 :sheet "Sheet1"} 6}
      (:min-col-sizes context) => {{:col 0 :sheet "Sheet1"} 9
                                   {:col 1 :sheet "Sheet1"} 8
                                   {:col 2 :sheet "Sheet1"} :auto})
    (fact "Correct style caching"
      (let [unique-styles (->> example-cells (map :style) set)]
        (-> context :cell-styles keys set) => unique-styles))))
