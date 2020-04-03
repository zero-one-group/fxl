(require '[zero-one.fxl.core :as fxl])

(def costs
  [{:item "Rent" :cost 1000}
   {:item "Gas"  :cost 100}
   {:item "Food" :cost 300}
   {:item "Gym"  :cost 50}])

(def header-cells
  [{:value "Item" :coord {:row 0 :col 0} :style {}}
   {:value "Cost" :coord {:row 0 :col 1} :style {}}])

(def body-cells
  (flatten
    (for [[row cost] (map vector (range) costs)]
      (list
        {:value (:item cost) :coord {:row (inc row) :col 0} :style {}}
        {:value (:cost cost) :coord {:row (inc row) :col 1} :style {}}))))

(def total-cells
  (let [row        (count costs)
        total-cost (apply + (map :cost costs))]
    [{:value "Total"    :coord {:row (+ row 2) :col 0} :style {}}
     {:value total-cost :coord {:row (+ row 2) :col 1} :style {}}]))

(fxl/write-xlsx!
  (concat header-cells body-cells total-cells)
  "examples/spreadsheets/write_to_plain_excel.xlsx")
