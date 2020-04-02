(ns zero-one.fxl.specs
  (:require
    [clojure.spec.alpha :as s]
    [zero-one.fxl.alignments :refer [horizontal-alignments vertical-alignments]]
    [zero-one.fxl.borders :refer [border-styles]]
    [zero-one.fxl.colours :refer [colours]]))

;; Coordinates
(def max-rows (int 1e5))
(def max-cols (int 1e4))
(s/def ::row (s/and nat-int? #(<= % max-rows)))
(s/def ::col (s/and nat-int? #(<= % max-cols)))
(s/def ::sheet string?)
(s/def ::coord
  (s/keys :req-un [::row ::col]
          :opt-un [::sheet]))

;; Cell Style
;;;; Font Style
(s/def ::bold boolean?)
(s/def ::italic boolean?)
(s/def ::underline boolean?)
(s/def ::strikeout boolean?)
(s/def ::font-name string?)
(s/def ::font-colour (-> colours keys set))
(s/def ::font-size nat-int?)
(s/def ::font-style
  (s/keys :opt-un [::bold
                   ::italic
                   ::underline
                   ::strikeout
                   ::font-size
                   ::font-name
                   ::font-colour]))

;;;; Alignment Style
(s/def ::horizontal (-> horizontal-alignments keys set))
(s/def ::vertical (-> vertical-alignments keys set))
(s/def ::alignment-style (s/keys :opt-un [::horizontal ::vertical]))

;;;; Border Style
(s/def :border/style (-> border-styles keys set))
(s/def :border/colour (-> colours keys set))
(s/def ::single-border-style (s/keys :opt-un [:border/style :border/colour]))

(s/def :border/bottom-border ::single-border-style)
(s/def :border/left-border ::single-border-style)
(s/def :border/right-border ::single-border-style)
(s/def :border/top-border ::single-border-style)
(s/def ::border-style (s/keys :opt-un [:border/bottom-border
                                       :border/left-border
                                       :border/right-border
                                       :border/top-border]))

;;;; Aggregated Style
(s/def ::data-format string?)
(s/def ::background-colour (-> colours keys set))
(s/def ::style
  (s/merge ::font-style
           ::alignment-style
           ::border-style
           (s/keys :opt-un [::background-colour])))

;; Cell Value
(s/def ::value (s/nilable (s/or :string  string?
                                :number  number?
                                :boolean boolean?)))

;; Cell
(s/def ::cell
  (s/keys :req-un [::value ::coord ::style]))
