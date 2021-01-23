(ns zero-one.fxl.core
  (:require
    [clojure.math.combinatorics :refer [cartesian-product]]
    [zero-one.fxl.alignments :as alignments]
    [zero-one.fxl.borders :as borders]
    [zero-one.fxl.colours :as colours]
    [zero-one.fxl.defaults :as defaults]
    [zero-one.fxl.read-xlsx :as read-xlsx]
    [zero-one.fxl.write-xlsx :as write-xlsx]))

;; Utility Functions
(defn ->cell [maybe-cell]
  (list
    (merge defaults/cell maybe-cell)))

(defn max-col [cells]
  (->> cells
       (map
         (juxt (comp :col :coord)
               (comp :last-col :coord)))
       (#(filter some? (flatten %)))
       (apply max -1)))

(defn max-row [cells]
  (->> cells
       (map
         (juxt (comp :row :coord)
               (comp :last-row :coord)))
       (#(filter some? (flatten %)))
       (apply max -1)))

(defn zip-with-index [coll]
  (map vector (range) coll))

;; Helper Functions: Ordered -> Unordered
(defn row->cells [row]
  (for [[index elem] (zip-with-index row)]
    {:value elem :coord {:row 0 :col index}}))

(defn col->cells [col]
  (for [[index elem] (zip-with-index col)]
    {:value elem :coord {:row index :col 0}}))

(defn table->cells [table]
  (flatten
    (for [[row-index row] (zip-with-index table)]
      (for [[col-index elem] (zip-with-index row)]
        {:value elem :coord {:row row-index :col col-index}}))))

(defn records->table
  ([records]
   (let [ks (distinct (mapcat keys records))]
     (records->table ks records)))
  ([ks records] (map #(map % ks) records)))

(defn records->cells
  ([records] (table->cells (records->table records)))
  ([ks records] (table->cells (records->table ks records))))

;; Inverse Helper Functions: Unordered -> Ordered
(defmacro forv [& body]
  `(vec (for ~@body)))

(defn cells->table
  ([cells] (cells->table cells (first (map :sheet cells))))
  ([cells sheet]
   (let [sheet   (or sheet "Sheet1")
         cells   (->> cells
                      (filter #(= (-> % :coord (:sheet "Sheet1")) sheet))
                      (map #(update % :coord dissoc :sheet)))
         indexed (group-by :coord cells)]
     (forv [i (range (inc (max-row cells)))]
       (forv [j (range (inc (max-col cells)))]
         (-> (indexed {:row i :col j}) first :value))))))

(defn cells->records
  ([cells ks] (cells->records cells ks (first (map :sheet cells))))
  ([cells ks sheet]
   (let [sheet   (or sheet "Sheet1")
         cells   (filter #(= (:sheet % "Sheet1") sheet) cells)
         ks      (into [] ks)
         indexed (group-by :coord cells)]
     (forv [i (range (inc (max-row cells)))]
       (into {}
         (forv [j (range (inc (max-col cells)))
                :let  [k (nth ks j nil)
                       v (-> (indexed {:row i :col j}) first :value)]
                :when (and k v)]
           (vector k v)))))))

;; Helper Functions: Relative Coords
(def merged-cell? write-xlsx/merged-cell?)

(defn- shift-plain-cell [dir shift cell]
  (let [old-index (get-in cell [:coord dir])
        new-index (max 0 (+ old-index shift))]
    (assoc-in cell [:coord dir] new-index)))

(defn- shift-merged-cell [dir shift cell]
  (let [first-key  (keyword (str "first-" (symbol dir)))
        last-key   (keyword (str "last-" (symbol dir)))
        old-index1 (get-in cell [:coord first-key])
        old-index2 (get-in cell [:coord last-key])
        new-index1 (max 0 (+ old-index1 shift))
        new-index2 (max 0 (+ old-index2 shift))]
    ;;TODO: rename and do i need max?
    (-> cell
        (assoc-in [:coord first-key] new-index1) 
        (assoc-in [:coord last-key] new-index2))))

(defn- shift-cell [dir shift cell]
  (if (merged-cell? cell)
    (shift-merged-cell dir shift cell)
    (shift-plain-cell dir shift cell)))

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
     (->cell {:coord {:row 0 :col (dec shift)} :style {}}))))

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
     (->cell {:coord {:row (dec shift) :col 0} :style {}}))))

(defn pad-table [cells]
  (let [coords    (->> cells (map :coord) set)
        indices   (cartesian-product
                    (-> cells max-row inc range)
                    (-> cells max-col inc range))
        pad-cells (for [[row col] indices
                        :let  [coord {:row row :col col}]
                        :when (not (contains? coords coord))]
                    {:value nil :coord coord :style {}})]
    (concat cells pad-cells)))

;; Style Utilities
(defn restyle-borders [cell border-style]
  (-> cell
      (assoc-in [:style :bottom-border] border-style)
      (assoc-in [:style :left-border] border-style)
      (assoc-in [:style :right-border] border-style)
      (assoc-in [:style :top-border] border-style)))

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
