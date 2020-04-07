(ns zero-one.fxl.core-test
  (:require
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]
    [midje.sweet :refer [facts fact =>]]
    [zero-one.fxl.specs :as fs]
    [zero-one.fxl.core :as fxl])
  (:import
    [java.io File]))

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
    (fact "Data formats should be extracted"
      (->> styles (map :data-format) count) => #(< 1 %))
    ;; TODO: background-colour seems to be undetected!
    (fact "Values should be extracted"
      (contains? values 1.4142) => true)))

(defn write-then-read-xlsx! [cells]
  (let [temp-dir  (io/file (System/getProperty "java.io.tmpdir"))
        temp-file (File/createTempFile "temporary" ".xlsx" temp-dir)]
    (fxl/write-xlsx! cells temp-file)
    (fxl/read-xlsx! temp-file)))

(facts "On fxl/write-xlsx"
  (let [write-cells [{:coord {:row 0 :col 0 :sheet "S1"} :value 1234
                      :style {:horizontal :fill :vertical :justify}}
                     {:coord {:row 0 :col 1 :sheet "S1"} :value 5678
                      :style {:bottom-border {:style :dashed :colour :gold}
                              :left-border {:style :hair :colour :white}
                              :right-border {:style :medium :colour :black}
                              :top-border {:style :thick :colour :yellow}}}
                     {:coord {:row 1 :col 0 :sheet "S1"} :value "AB"
                      :style {:background-colour :yellow}}
                     {:coord {:row 1 :col 1 :sheet "S1"} :value "XY"
                      :style {:bold        true
                              :italic      true
                              :underline   true
                              :strikeout   true
                              :font-colour :red
                              :font-size   12
                              :font-name   "Arial"}}
                     {:coord {:row 2 :col 0 :sheet "S2"} :value 3.14
                      :style {:data-format "@"}}
                     {:coord {:row 2 :col 1 :sheet "S2"} :value 2.71
                      :style {:data-format "non-builtin"}}
                     {:coord {:row 3 :col 0 :sheet "S2"} :value true  :style {}}
                     {:coord {:row 3 :col 1 :sheet "S2"} :value false :style {}}]
        read-cells  (write-then-read-xlsx! write-cells)]
    (fact "Write and read cells should have the same count"
      (count read-cells) => (count write-cells))
    (fact "Horizontal alignment style should be preserved"
      (let [horizontals (->> read-cells (map (comp :horizontal :style)) set)]
        (contains? horizontals :fill) => true))
    (fact "Vertical alignment style should be preserved"
      (let [verticals (->> read-cells (map (comp :vertical :style)) set)]
        (contains? verticals :justify) => true))
    (fact "Border style should be preserved"
      (let [borders (->> read-cells (map (comp :bottom-border :style)) set)]
        (contains? borders {:style :dashed :colour :gold}) => true))
    (fact "Background colour should be preserved"
      (let [bg-colours (->> read-cells (map (comp :background-colour :style)) set)]
        (contains? bg-colours :yellow) => true))
    (fact "Font name should be preserved"
      (let [font-names (->> read-cells (map (comp :font-name :style)) set)]
        (contains? font-names "Arial") => true))
    (fact "Font style should be preserved"
      (let [font-style (->> read-cells
                            (filter #(= (:value %) "XY"))
                            first
                            :style)]
        font-style => {:bold        true
                       :italic      true
                       :underline   true
                       :strikeout   true
                       :font-colour :red
                       :font-name   "Arial"}))
    (fact "Data formats should be preserved"
      (let [data-formats (->> read-cells (map (comp :data-format :style)) set)]
        (contains? data-formats "@") => true))
    (fact "Non-builtin data format should be dropped"
      (let [data-formats (->> read-cells (map (comp :data-format :style)) set)]
        (contains? data-formats "non-builtin") => false)))
  (let [write-cells [{:coord {:row 0 :col 0} :value 12345 :style {}}
                     {:coord {:row 0 :col 0} :value "abc" :style {}}]
        read-cells  (write-then-read-xlsx! write-cells)]
    (fact "Overwrite cells should work correctly"
      (count read-cells) => 1
      (-> read-cells first :value) => "abc")))
