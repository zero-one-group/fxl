(ns zero-one.fxl.core-test
  (:require
    [midje.sweet :refer [facts fact =>]]
    [clojure.spec.alpha :as s]
    [zero-one.fxl.specs :as fs]
    [zero-one.fxl.core :as fxl]))

(facts "On fxl/->cell"
  (fact "Should fill-in the blanks"
    (fxl/->cell {}) => fxl/default-cell)
  (fact "Should return valid cell from only coord"
    (s/valid? ::fs/cell (fxl/->cell {:coord {:row 0 :col 0}})) => true)
  (fact "Should return valid cell from only value"
    (s/valid? ::fs/cell (fxl/->cell {:value "abc"})) => true)
  (fact "Should return valid cell from only style"
    (s/valid? ::fs/cell (fxl/->cell {:style {}})) => true))

(facts "On fxl/read-xlsx"
  (let [cells (fxl/read-xlsx "test/resources/dummy-spreadsheet.xlsx")]
    (fact "Read cells should all be valid"
      (filter #(not (s/valid? ::fs/cell %)) cells) => ())
    (fact "There should be 23 cells"
      (count cells) => 23)))
