(ns zero-one.fxl.core
  (:import
    (org.apache.poi.xssf.usermodel XSSFWorkbook)))

(def default-cell
  {:value nil
   :coord {:row 0 :col 0}
   :style {}})

(defn ->cell [maybe-cell]
  (merge maybe-cell default-cell))

(defn- extract-cell-value [cell]
  (let [cell-type (.. cell getCellType name)]
    (case cell-type
      "NUMERIC" (.getNumericCellValue cell)
      "STRING"  (.getStringCellValue cell)
      "BOOLEAN" (.getBooleanCellValue cell)
      "ERROR"   (.getErrorCellString cell)
      nil)))

(defn- extract-row-values [row]
  (for [cell (-> row .iterator iterator-seq)]
    {:coord {:row   (.getRowNum row)
             :col   (.getColumnIndex cell)
             :sheet (.. row getSheet getSheetName)}
      :value (extract-cell-value cell)
      :style {}})) ;; TODO})))

(defn- extract-sheet-values [sheet]
  (let [rows       (-> sheet .iterator iterator-seq)]
    (mapcat extract-row-values rows)))

(defn- extract-workbook-values [workbook]
  (let [sheets (-> workbook .iterator iterator-seq)]
    (mapcat extract-sheet-values sheets)))

(defn read-xlsx [path]
  (-> path XSSFWorkbook. extract-workbook-values))

(comment

  (def cells (read-xlsx "test/resources/dummy-spreadsheet.xlsx"))
  (map println (map :coord cells))

  (def workbook (XSSFWorkbook. "test/resources/dummy-spreadsheet.xlsx"))
  (def sheet (.getSheet workbook "Sheet2"))
  (def row (.getRow sheet 1))
  (-> row .iterator iterator-seq)

  (println "end"))
