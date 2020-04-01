(ns zero-one.fxl.specs-test
  (:require
    [midje.sweet :refer [facts fact =>]]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [zero-one.fxl.specs :as fs]))

(defn valid? [spec value]
  (if (s/valid? spec value)
    true
    (do
      (expound/expound spec value)
      false)))

(defn invalid? [spec value]
  (not (s/valid? spec value)))

(facts "On fxl coordinates"
  (fact "Should allow example map"
    (valid? ::fs/coord {:row 0 :col 1}) => true)
  (fact "Should allow sheet"
    (valid? ::fs/coord {:row 0 :col 1 :sheet "ABC"}) => true)
  (fact "Should not allow non-string sheet"
    (invalid? ::fs/coord {:row 0 :col 1 :sheet 123}) => true)
  (fact "Should not allow negative coords"
    (invalid? ::fs/coord {:row -1 :col 0}) => true))

(facts "On fxl cell values"
  (fact "Should allow ints"
    (valid? ::fs/value 123) => true)
  (fact "Should allow floats"
    (valid? ::fs/value 123.) => true)
  (fact "Should allow strings"
    (valid? ::fs/value "abc") => true)
  (fact "Should allow nil"
    (valid? ::fs/value nil) => true)
  (fact "Should not allow collections"
    (invalid? ::fs/value (list 1 2 3)) => true))

(facts "On fxl cells"
  (fact "Should allow example map"
    (valid? ::fs/cell {:coord {:row 1 :col 1}
                       :value "abc"
                       :style {}})
    => true)
  (fact "Should not allow incorrect coord"
    (invalid? ::fs/cell {:coord {:row 1 :col "abc"}
                         :value "abc"
                         :style {}})
    => true)
  (fact "Should not allow incorrect value"
    (invalid? ::fs/cell {:coord {:row 1 :col 2}
                         :value [1 2]
                         :style {}})
    => true)
  (fact "Should not allow missing style"
    (invalid? ::fs/cell {:coord {:row 1 :col 2}
                         :value [1 2]})
    => true))
