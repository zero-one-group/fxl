(require '[zero-one.fxl.core :as fxl])

(def costs
  [{:item "Rent" :cost 1000}
   {:item "Gas"  :cost 100}
   {:item "Food" :cost 300}
   {:item "Gym"  :cost 50}])

(def header-cells (fxl/row->cells ["Item" "Cost"]))

(def body-cells
  (fxl/records->cells [:item :cost] costs))

(def total-cells
  (let [total-cost (apply + (map :cost costs))]
    (fxl/row->cells ["Total" total-cost])))

(defn bold [cell]
  (assoc-in cell [:style :bold] true))

(defn highlight [cell]
  (assoc-in cell [:style :background-colour] :grey_25_percent))

(defn align-center [cell]
  (assoc-in cell [:style :horizontal] :center))

(fxl/write-xlsx!
 (map align-center
      (fxl/concat-below
       (map (comp bold highlight) header-cells)
       (fxl/pad-below body-cells)
       (map bold total-cells)))
 "examples/spreadsheets/write_to_plain_excel_with_helpers.xlsx")
