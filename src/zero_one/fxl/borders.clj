(ns zero-one.fxl.borders
  (:import
    [org.apache.poi.ss.usermodel BorderStyle]))


(def border-styles
  {:dash_dot            BorderStyle/DASH_DOT
   :dash_dot_dot        BorderStyle/DASH_DOT_DOT
   :dashed              BorderStyle/DASHED
   :dotted              BorderStyle/DOTTED
   :double              BorderStyle/DOUBLE
   :hair                BorderStyle/HAIR
   :medium              BorderStyle/MEDIUM
   :medium_dash_dot     BorderStyle/MEDIUM_DASH_DOT
   :medium_dash_dot_dot BorderStyle/MEDIUM_DASH_DOT_DOT
   :medium_dashed       BorderStyle/MEDIUM_DASHED
   :none                BorderStyle/NONE
   :slanted_dash_dot    BorderStyle/SLANTED_DASH_DOT
   :thick               BorderStyle/THICK
   :thin                BorderStyle/THIN})

(def border-style-lookup
  (into {} (map (fn [[k v]] [v k]) border-styles)))
