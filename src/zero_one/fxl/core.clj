;; TODO: available colours should be accessible here.
(ns zero-one.fxl.core
  (:require
    [zero-one.fxl.alignments :as alignments]
    [zero-one.fxl.borders :as borders]
    [zero-one.fxl.colours :as colours]
    [zero-one.fxl.defaults :as defaults])
  (:import
    [java.io FileOutputStream]
    [org.apache.poi.xssf.usermodel XSSFWorkbook]))

(defn ->cell [maybe-cell]
  (merge maybe-cell defaults/cell))

(defn- extract-cell-value [cell]
  (let [cell-type (.. cell getCellType name)]
    (case cell-type
      "NUMERIC" (.getNumericCellValue cell)
      "STRING"  (.getStringCellValue cell)
      "BOOLEAN" (.getBooleanCellValue cell)
      "ERROR"   (.getErrorCellString cell)
      "FORMULA" (try
                  (.getNumericCellValue cell)
                  (catch Exception _ (.getRawValue cell)))
      nil)))

(defn- extract-cell-border-style [cell]
  (let [cell-style (.getCellStyle cell)]
    {:bottom-border
      {:style (-> cell-style .getBorderBottom borders/border-style-lookup)
       :colour (-> cell-style .getBottomBorderColor colours/colours-lookup)}
     :left-border
      {:style (-> cell-style .getBorderLeft borders/border-style-lookup)
       :colour (-> cell-style .getLeftBorderColor colours/colours-lookup)}
     :right-border
      {:style (-> cell-style .getBorderRight borders/border-style-lookup)
       :colour (-> cell-style .getRightBorderColor colours/colours-lookup)}
     :top-border
      {:style (-> cell-style .getBorderTop borders/border-style-lookup)
       :colour (-> cell-style .getTopBorderColor colours/colours-lookup)}}))

(defn- extract-cell-alignment-style [cell]
  (let [cell-style (.getCellStyle cell)]
    {:horizontal (-> cell-style
                      .getAlignment
                      alignments/horizontal-alignment-lookup)
      :vertical  (-> cell-style
                     .getVerticalAlignment
                     alignments/vertical-alignment-lookup)}))

(defn- extract-cell-font-style [workbook cell]
  (let [font-index (-> cell .getCellStyle .getFontIndexAsInt)
        font       (.getFontAt workbook font-index)]
    {:bold        (.getBold font)
     :italic      (.getItalic font)
     :strikeout   (.getStrikeout font)
     :underline   (boolean (.getUnderline font))
     :font-name   (.getFontName font)
     :font-colour (-> font .getColor colours/colours-lookup)
     :font-size   (.getFontHeightInPoints font)}))

(defn- prune-cell-style [cell-style]
  (into {}
    (filter
      (fn [[k v]] (not= v (defaults/style k)))
      cell-style)))

(defn- extract-cell-style [workbook cell]
  (let [cell-style (merge
                      (extract-cell-border-style cell)
                      (extract-cell-alignment-style cell)
                      (extract-cell-font-style workbook cell)
                      {:background-colour (-> cell
                                              .getCellStyle
                                              .getFillBackgroundColor
                                              colours/colours-lookup)})]
    (prune-cell-style cell-style)))

(defn extract-row-values [workbook row]
  (for [cell (-> row .iterator iterator-seq)]
    {:coord {:row   (.getRowNum row)
             :col   (.getColumnIndex cell)
             :sheet (.. row getSheet getSheetName)}
      :value (extract-cell-value cell)
      :style (extract-cell-style workbook cell)}))

(defn- extract-sheet-values [workbook sheet]
  (let [rows       (-> sheet .iterator iterator-seq)]
    (doall (mapcat #(extract-row-values workbook %) rows))))

(defn- extract-workbook-values [workbook]
  (let [sheets (-> workbook .iterator iterator-seq)]
    (doall (mapcat #(extract-sheet-values workbook %) sheets))))

;; TODO: need to close?
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

;; TODO: need to close?
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
  (map clojure.pprint/pprint (set (map :style cells)))
  (clojure.pprint/pprint (filter #(-> % :coord :row (= 4)) cells))
  (def cell (first cells))
  (def cell-style (:style cell))
  (set (map :value cells))

  (def workbook (XSSFWorkbook. "test/resources/dummy-spreadsheet.xlsx"))
  (def sheet (.getSheet workbook "Sheet1"))
  (def row (.getRow sheet 4))
  (def cell (.getCell row 2))
  (-> cell .getCellStyle .getFillPattern)
  (-> cell .getCellStyle .getFillBackgroundColor)
  (-> cell .getCellStyle .getFillForegroundColor colours/colours-lookup)
  (count (map #(.getIndex % ) (vals colours/colours)))
  (count colours/colours)

  (def cells (-> row .iterator iterator-seq))
  (def cell (first cells))
  (def cell-style (.getCellStyle cell))
  (-> cell-style .getFillBackgroundColor colours/colours-lookup)

  (println "end"))
