(require '[zero-one.fxl.core :as fxl])

(def costs
  [{:item "Rent" :cost 1000}
   {:item "Gas"  :cost 100}
   {:item "Food" :cost 300}
   {:item "Gym"  :cost 50}])

(def header-cells (fxl/row->cells ["Item" "Cost"]))

(def body-cells
  (fxl/table->cells (map #(list (:item %) (:cost %)) costs)))

(def total-cells
  (let [total-cost (apply + (map :cost costs))]
    (fxl/row->cells ["Total" total-cost])))

(def highlight-style {:bold true :background-colour :grey_25_percent})

(fxl/write-xlsx!
  (fxl/concat-below
    (map #(assoc % :style highlight-style) header-cells)
    (fxl/pad-below body-cells)
    (map #(assoc % :style highlight-style) total-cells))
  "examples/spreadsheets/write_to_plain_excel_with_helpers.xlsx")
