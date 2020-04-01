(ns zero-one.fxl.specs
  (:require
    [clojure.spec.alpha :as s]))

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
(s/def ::style (s/keys))

;; Cell Value
(s/def ::value (s/nilable (s/or :string  string?
                                :number  number?
                                :boolean boolean?)))

;; Cell
(s/def ::cell
  (s/keys :req-un [::value ::coord ::style]))
