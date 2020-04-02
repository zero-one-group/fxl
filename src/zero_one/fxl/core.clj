(ns zero-one.fxl.core
  (:require
    [zero-one.fxl.alignments :as alignments]
    [zero-one.fxl.borders :as borders]
    [zero-one.fxl.colours :as colours]
    [zero-one.fxl.defaults :as defaults]
    [zero-one.fxl.read-xlsx :as read-xlsx]
    [zero-one.fxl.write-xlsx :as write-xlsx]))

(def colour-set
  (-> colours/colours keys set))

(def border-style-set
  (-> borders/border-styles keys set))

(def horizontal-alignment-set
  (-> alignments/horizontal-alignments keys set))

(def vertical-alignment-set
  (-> alignments/vertical-alignments keys set))

(defn ->cell [maybe-cell]
  (merge maybe-cell defaults/cell))

(def read-xlsx! read-xlsx/read-xlsx!)

(def write-xlsx! write-xlsx/write-xlsx!)
