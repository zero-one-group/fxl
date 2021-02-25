(ns zero-one.fxl.specs-test
  (:require
    [clojure.string :refer [includes?]]
    [midje.sweet :refer [facts fact =>]]
    [zero-one.fxl.specs :as fs]))

(facts "On valid? and invalid?"
  (fact "Should give correct results"
    (fs/valid? nat-int? 10) => true
    (fs/invalid? nat-int? 10) => false
    (fs/invalid? nat-int? -10) => true)
  (fact "Should explain upon invalidity"
    (let [error-msg (with-out-str (fs/valid? nat-int? -10))]
      (string? error-msg) => true
      (includes? error-msg "Spec failed") => true)))

(facts "On fxl coordinates"
  (fact "Should allow example map"
    (fs/valid? ::fs/coord {:row 0 :col 1}) => true)
  (fact "Should allow sheet"
    (fs/valid? ::fs/coord {:row 0 :col 1 :sheet "ABC"}) => true)
  (fact "Should not allow non-string sheet"
    (fs/invalid? ::fs/coord {:row 0 :col 1 :sheet 123}) => true)
  (fact "Should not allow negative coords"
    (fs/invalid? ::fs/coord {:row -1 :col 0}) => true))

(facts "On fxl merged coord"
  (fact "Should allow merged cell"
    (fs/valid? ::fs/merged-coord {:first-row 0 :first-col 1
                                  :last-row 1 :last-col 2}) => true)
  (fact "Should allow merged cell w sheet"
    (fs/valid? ::fs/merged-coord {:first-row 0 :first-col 1
                                  :last-row 1 :last-col 2
                                  :sheet "ABC"}) => true)
  (fact "Should not allow negative merged cell area"
    (fs/invalid? ::fs/merged-coord {:first-row 1 :first-col 1
                                    :last-row 0 :last-col 0
                                    :sheet "ABC"}) => true)
  (fact "Should not allow only row and col"
    (fs/invalid? ::fs/merged-coord {:first-row 0 :first-col 0}) => true)
  (fact "Should not allow only lrow and lcol"
    (fs/invalid? ::fs/merged-coord {:last-row 0 :last-col 0}) => true))

(facts "On fxl data formats"
  (fact "Should allow example format"
    (fs/valid? ::fs/data-format "[h]:mm:ss") => true)
  (fact "Should not allow random data format"
    nil => (fs/invalid? ::fs/data-format)))

(facts "On fxl min-row-size and max-col-size"
  (fact "Should not allow invalid min and max row sizes"
    {:value nil
     :coord {:row 0 :col 0}
     :style {:row-size nil}} => (fs/invalid? ::fs/cell)
    {:value nil
     :coord {:row 0 :col 0}
     :style {:col-size "abc"}} => (fs/invalid? ::fs/cell)
    {:value nil
     :coord {:row 0 :col 0}
     :style {:col-size :auto}} => (fs/valid? ::fs/cell)))

(facts "On fxl font styles"
  (fact "Should allow example font style"
    (fs/valid? ::fs/font-style {:bold        true
                                :italic      false
                                :underline   true
                                :strikeout   false
                                :font-size   12
                                :font-colour :black
                                :font-name   "Arial"}) => true)
  (fact "Should not allow non-boolean bold"
    (fs/invalid? ::fs/font-style {:bold 0}) => true)
  (fact "Should not allow random colours"
    (fs/invalid? ::fs/font-style {:font-colour :white-123}) => true))

(facts "On fxl alignment styles"
  (fact "Should allow example font style"
    (fs/valid? ::fs/alignment-style {:horizontal :fill :vertical :center}) => true))

(facts "On fxl border styles"
  (fact "Should allow example single-border style"
    (fs/valid? ::fs/single-border-style {:style :thick :colour :black}) => true)
  (fact "Should allow example aggregated border style"
    (fs/valid? ::fs/border-style
               {:left-border   {:style :dotted :colour :white}
                :right-border  {:style :thin   :colour :red}
                :bottom-border {:style :medium :colour :blue}
                :top-border    {:style :hair   :colour :yellow}})
    => true))

(facts "On fxl cell style"
  (fact "Should allow example style"
    (fs/valid? ::fs/style
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
    (fs/invalid? ::fs/style {:background-colour :undefined-colour}) => true))

(facts "On fxl cell values"
  (fact "Should allow ints"
    (fs/valid? ::fs/value 123) => true)
  (fact "Should allow floats"
    (fs/valid? ::fs/value 123.) => true)
  (fact "Should allow strings"
    (fs/valid? ::fs/value "abc") => true)
  (fact "Should allow nil"
    (fs/valid? ::fs/value nil) => true)
  (fact "Should allow bool"
    (fs/valid? ::fs/value false) => true)
  (fact "Should not allow collections"
    (fs/invalid? ::fs/value (list 1 2 3)) => true))

(facts "On fxl cells"
  (fact "Should allow example map"
    (fs/valid? ::fs/cell {:coord {:row 1 :col 1}
                          :value "abc"
                          :style {}
                          :formula nil})
    => true)
  (fact "Style and formula should be optional"
    (fs/valid? ::fs/cell {:coord {:row 1 :col 2}
                          :value "abc"})
    => true)
  (fact "Should not allow incorrect value"
    (fs/invalid? ::fs/cell {:coord {:row 1 :col 2}
                            :value [1 2]
                            :style {}
                            :formula nil})
    => true)
  (fact "Should not allow incorrect coord"
    (fs/invalid? ::fs/cell {:coord {:row 1 :col "abc"}
                            :value "abc"
                            :style {}
                            :formula nil})
    => true))
