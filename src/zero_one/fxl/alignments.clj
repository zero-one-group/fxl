(ns zero-one.fxl.alignments
  (:import
    [org.apache.poi.ss.usermodel HorizontalAlignment VerticalAlignment]))

(def horizontal-alignments
  {:center           HorizontalAlignment/CENTER
   :center-selection HorizontalAlignment/CENTER_SELECTION
   :distributed      HorizontalAlignment/DISTRIBUTED
   :fill             HorizontalAlignment/FILL
   :general          HorizontalAlignment/GENERAL
   :justify          HorizontalAlignment/JUSTIFY
   :left             HorizontalAlignment/LEFT
   :right            HorizontalAlignment/RIGHT})

(def vertical-alignments
  {:bottom      VerticalAlignment/BOTTOM
   :center      VerticalAlignment/CENTER
   :distributed VerticalAlignment/DISTRIBUTED
   :justify     VerticalAlignment/JUSTIFY
   :top         VerticalAlignment/TOP})

(def horizontal-alignment-lookup
  (into {} (map (fn [[k v]] [v k]) horizontal-alignments)))

(def vertical-alignment-lookup
  (into {} (map (fn [[k v]] [v k]) vertical-alignments)))
