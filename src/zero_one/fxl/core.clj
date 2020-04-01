(ns zero-one.fxl.core
  (:import
    [java.io FileOutputStream]
    [org.apache.poi.xssf.usermodel XSSFWorkbook]))

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

(defn read-xlsx! [path]
  (-> path XSSFWorkbook. extract-workbook-values))

(defn- get-or-create-sheet! [cell workbook]
  (let [sheet-name (get-in cell [:coord :sheet] "Sheet1")]
    (or (.getSheet workbook sheet-name)
        (.createSheet workbook sheet-name))))

(defn- get-or-create-row! [cell xl-sheet]
  (let [row-index (-> cell :coord :row)]
    (or (.getRow xl-sheet row-index)
        (.createRow xl-sheet row-index))))

(defn- get-or-create-cell! [cell xl-row]
  (let [col-index (-> cell :coord :col)]
    (or (.getCell xl-row col-index)
        (.createCell xl-row col-index))))

(defn- ensure-settable [value]
  (if (number? value)
    (double value)
    value))

(defn write-xlsx! [cells path]
  (let [workbook (XSSFWorkbook.)]
    (doall
      (for [cell cells]
        (as-> workbook ?
             (get-or-create-sheet! cell ?)
             (get-or-create-row! cell ?)
             (get-or-create-cell! cell ?)
             (.setCellValue ? (ensure-settable (:value cell))))))
             ;; TODO: add style
    (.write workbook (FileOutputStream. path))))

(comment

  (def cells (read-xlsx! "test/resources/dummy-spreadsheet.xlsx"))
  (map println (map :coord cells))

  (def workbook (XSSFWorkbook. "test/resources/dummy-spreadsheet.xlsx"))
  (def sheet (.getSheet workbook "Sheet2"))
  (def row (.getRow sheet 1))
  (-> row .iterator iterator-seq)

  (def temp-dir (io/file (System/getProperty "java.io.tmpdir")))
  (defn create-temp-file [extension]
    (File/createTempFile "temporary" extension temp-dir))
  (create-temp-file ".xlsx")

  (println "end"))
