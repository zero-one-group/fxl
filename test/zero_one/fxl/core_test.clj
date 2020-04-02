(ns zero-one.fxl.core-test
  (:require
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]
    [midje.sweet :refer [facts fact =>]]
    [zero-one.fxl.specs :as fs]
    [zero-one.fxl.core :as fxl]
    [zero-one.fxl.defaults :as defaults])
  (:import
    [java.io File]))

(facts "On fxl/->cell"
  (fact "Should fill-in the blanks"
    (fxl/->cell {}) => defaults/cell)
  (fact "Should return valid cell from only coord"
    (s/valid? ::fs/cell (fxl/->cell {:coord {:row 0 :col 0}})) => true)
  (fact "Should return valid cell from only value"
    (s/valid? ::fs/cell (fxl/->cell {:value "abc"})) => true)
  (fact "Should return valid cell from only style"
    (s/valid? ::fs/cell (fxl/->cell {:style {}})) => true))

(facts "On fxl/read-xlsx"
  (let [cells  (fxl/read-xlsx! "test/resources/dummy-spreadsheet.xlsx")
        values (->> cells (map :value) set)
        styles (->> cells (map :style) set)]
    (fact "Read cells should all be valid"
      (filter #(not (s/valid? ::fs/cell %)) cells) => ())
    (fact "There should be 23 cells"
      (count cells) => 23)
    (fact "Font style should be extracted"
      (contains? styles {:font-size 14}) => true)
    (fact "Border style should be extracted"
      (contains?  styles {:bottom-border {:style :thin :colour :black1}}) => true)
    (fact "Alignment style should be extracted"
      (contains? styles {:vertical :center}) => true)
    ;; TODO: background-colour seems to be undetected!
    (fact "Values should be extracted"
      (contains? values 1.4142) => true)))

(defn write-then-read-xlsx! [cells]
  (let [temp-dir  (io/file (System/getProperty "java.io.tmpdir"))
        temp-file (File/createTempFile "temporary" ".xlsx" temp-dir)]
    (fxl/write-xlsx! cells temp-file)
    (fxl/read-xlsx! temp-file)))

(facts "On fxl/write-xlsx"
  (let [write-cells [{:coord {:row 0 :col 0 :sheet "S1"} :value 1234  :style {}}
                     {:coord {:row 0 :col 1 :sheet "S1"} :value 5678  :style {}}
                     {:coord {:row 1 :col 0 :sheet "S1"} :value "AB"  :style {}}
                     {:coord {:row 1 :col 1 :sheet "S1"} :value "XY"  :style {}}
                     {:coord {:row 2 :col 0 :sheet "S2"} :value 3.14  :style {}}
                     {:coord {:row 2 :col 1 :sheet "S2"} :value 2.71  :style {}}
                     {:coord {:row 3 :col 0 :sheet "S2"} :value true  :style {}}
                     {:coord {:row 3 :col 1 :sheet "S2"} :value false :style {}}]
        read-cells  (write-then-read-xlsx! write-cells)]
    (fact "Write and read cells should have the same count"
      (count read-cells) => (count write-cells))))
