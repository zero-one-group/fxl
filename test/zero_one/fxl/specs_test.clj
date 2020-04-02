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

(facts "On fxl font styles"
  (fact "Should allow example font style"
    (valid? ::fs/font-style {:bold        true
                             :italic      false
                             :underline   true
                             :strikeout   false
                             :font-size   12
                             :font-colour :black
                             :font-name   "Arial"}) => true)
  (fact "Should not allow non-boolean bold"
    (invalid? ::fs/font-style {:bold 0}) => true)
  (fact "Should not allow random colours"
    (invalid? ::fs/font-style {:font-colour :white-123}) => true))

(facts "On fxl alignment styles"
  (fact "Should allow example font style"
    (valid? ::fs/alignment-style {:horizontal :fill :vertical :center}) => true))

(facts "On fxl border styles"
  (fact "Should allow example single-border style"
    (valid? ::fs/single-border-style {:style :thick :colour :black}) => true)
  (fact "Should allow example aggregated border style"
    (valid? ::fs/border-style
            {:left-border   {:style :dotted :colour :white}
             :right-border  {:style :thin   :colour :red}
             :bottom-border {:style :medium :colour :blue}
             :top-border    {:style :hair   :colour :yellow}})
    => true))

(facts "On fxl cell style"
  (fact "Should allow example style"
    (valid? ::fs/style
            {:bold          true
             :italic        false
             :underline     true
             :strikeout     false
             :font-size     10
             :font-colour   :blue
             :font-name     "Arial"
             :horizontal    :fill
             :vertical      :center
             :left-border   {:style :dashed :colour :gold}
             :right-border  {:style :dotted :colour :lime}
             :bottom-border {:style :double :colour :maroon}
             :top-border    {:style :medium :colour :orange}})
    => true)
  (fact "Should not allow non-sense background style"
    (invalid? ::fs/style {:background-colour :undefined-colour}) => true))

(facts "On fxl cell values"
  (fact "Should allow ints"
    (valid? ::fs/value 123) => true)
  (fact "Should allow floats"
    (valid? ::fs/value 123.) => true)
  (fact "Should allow strings"
    (valid? ::fs/value "abc") => true)
  (fact "Should allow nil"
    (valid? ::fs/value nil) => true)
  (fact "Should allow bool"
    (valid? ::fs/value false) => true)
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
