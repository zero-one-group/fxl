(ns zero-one.fxl.core
  (:require
    [zero-one.fxl.alignments :as alignments]
    [zero-one.fxl.borders :as borders]
    [zero-one.fxl.colours :as colours]
    [zero-one.fxl.defaults :as defaults]
    [zero-one.fxl.read-xlsx :as read-xlsx]
    [zero-one.fxl.write-xlsx :as write-xlsx]))

;; Utility Functions
(defn ->cell [maybe-cell]
  (merge defaults/cell maybe-cell))

(defn max-col [cells]
  (->> cells
       (map (comp :col :coord))
       (apply max -1)))

(defn max-row [cells]
  (->> cells
       (map (comp :row :coord))
       (apply max -1)))

(defn zip-with-index [coll]
  (map vector (range) coll))

;; Helper Functions: Ordered -> Unordered
(defn row->cells [row]
  (for [[index elem] (zip-with-index row)]
    {:value elem :coord {:row 0 :col index} :style {}}))

(defn col->cells [col]
  (for [[index elem] (zip-with-index col)]
    {:value elem :coord {:row index :col 0} :style {}}))

(defn table->cells [table]
  (flatten
    (for [[row-index row] (zip-with-index table)]
      (for [[col-index elem] (zip-with-index row)]
        {:value elem :coord {:row row-index :col col-index} :style {}}))))


(defn records->table
  ([records]
   (let [ks (distinct (mapcat keys records))]
     (records->table ks records)))
  ([ks records] (map #(map % ks) records)))

(defn records->cells
  ([records] (table->cells (records->table records)))
  ([ks records] (table->cells (records->table ks records))))

;; Helper Functions: Relative Coords
(defn- shift-cell [dir shift cell]
  (let [old-index (get-in cell [:coord dir])
        new-index (max 0 (+ old-index shift))]
    (assoc-in cell [:coord dir] new-index)))

(defn shift-right [shift cell]
  (shift-cell :col shift cell))

(defn shift-left [shift cell]
  (shift-cell :col (- shift) cell))

(defn shift-down [shift cell]
  (shift-cell :row shift cell))

(defn shift-up [shift cell]
  (shift-cell :row (- shift) cell))

(defn concat-right
  ([] nil)
  ([cells] cells)
  ([l-cells r-cells]
   (let [shift   (inc (max-col l-cells))
         shifted (map #(shift-right shift %) r-cells)]
     (concat l-cells shifted)))
  ([l-cells r-cells & tail]
   (reduce concat-right (concat-right l-cells r-cells) tail)))

(defn pad-right
  ([cells] (pad-right 1 cells))
  ([shift cells]
   (concat-right
     cells
     [(->cell {:coord {:row 0 :col (dec shift)} :style {}})])))

(defn concat-below
  ([] nil)
  ([cells] cells)
  ([l-cells r-cells]
   (let [shift   (inc (max-row l-cells))
         shifted (map #(shift-down shift %) r-cells)]
     (concat l-cells shifted)))
  ([l-cells r-cells & tail]
   (reduce concat-below (concat-below l-cells r-cells) tail)))

(defn pad-below
  ([cells] (pad-below 1 cells))
  ([shift cells]
   (concat-below
     cells
     [(->cell {:coord {:row (dec shift) :col 0} :style {}})])))

;; Style Options
(def colour-set
  (-> colours/colours keys set))

(def border-style-set
  (-> borders/border-styles keys set))

(def horizontal-alignment-set
  (-> alignments/horizontal-alignments keys set))

(def vertical-alignment-set
  (-> alignments/vertical-alignments keys set))

;; IO Functions
(def read-xlsx! read-xlsx/read-xlsx!)

(def write-xlsx! write-xlsx/write-xlsx!)
