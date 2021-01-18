(ns zero-one.fxl.specs
  (:require
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [zero-one.fxl.alignments :refer [horizontal-alignments vertical-alignments]]
    [zero-one.fxl.borders :refer [border-styles]]
    [zero-one.fxl.colours :refer [colours]]))

;; Coordinates
(def max-rows (int 1e5))
(def max-cols (int 1e4))
(s/def ::row (s/and nat-int? #(<= % max-rows)))
(s/def ::col (s/and nat-int? #(<= % max-cols)))
;; For merged cells
(s/def ::lrow (s/and nat-int? #(<= % max-rows)))
(s/def ::lcol (s/and nat-int? #(<= % max-cols)))
(s/def ::sheet string?)

(s/def ::plain-coord
  (s/keys :req-un [::row ::col]
          :opt-un [::sheet]))

(s/def ::merged-coord
  (s/and
    (s/keys :req-un [::row ::col ::lrow ::lcol]
            :opt-un [::sheet])
    #(<= (:row %) (:lrow %))
    #(<= (:col %) (:lcol %))))

(s/def ::coord
  (s/or ::merged-coord
        ::plain-coord))

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
(s/def ::row-size ::row)
(s/def ::col-size (s/or :number ::col
                        :auto   #(= % :auto)))
(s/def ::style
  (s/merge ::font-style
           ::alignment-style
           ::border-style
           (s/keys :opt-un [::data-format
                            ::background-colour
                            ::row-size
                            ::col-size])))

;; Cell Value
(s/def ::value (s/nilable (s/or :string  string?
                                :number  number?
                                :boolean boolean?)))

(s/def ::formula (s/nilable string?))

;; Cell
(s/def ::cell
  (s/keys :req-un [::value ::coord]
          :opt-un [::style ::formula]))

;; Handy Functions
(defn valid?
  ([spec]
   (fn [value] (valid? spec value)))
  ([spec value]
   (if (s/valid? spec value)
     true
     (do
       (expound/expound spec value)
       false))))

(defn invalid?
  ([spec]
   (fn [value] (invalid? spec value)))
  ([spec value]
   (not (s/valid? spec value))))
