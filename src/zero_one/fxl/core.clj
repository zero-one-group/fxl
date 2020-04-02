(ns zero-one.fxl.core
  (:require
    [zero-one.fxl.alignments :as alignments]
    [zero-one.fxl.borders :as borders]
    [zero-one.fxl.colours :as colours]
    [zero-one.fxl.defaults :as defaults])
  (:import
    [java.io FileOutputStream]
    [org.apache.poi.xssf.usermodel XSSFWorkbook]
    [org.apache.poi.ss.usermodel FillPatternType FontUnderline]))

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
                                              .getFillForegroundColor
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

(defn read-xlsx! [path]
  (let [workbook (XSSFWorkbook. path)
        cells    (extract-workbook-values workbook)]
    (.close workbook)
    cells))

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

(defn- create-cell-style! [workbook cell]
  (let [style (.createCellStyle workbook)]
    (when-let [horizontal (-> cell :style :horizontal)]
      (.setAlignment style (alignments/horizontal-alignments horizontal)))
    (when-let [vertical (-> cell :style :vertical)]
      (.setVerticalAlignment style (alignments/vertical-alignments vertical)))
    (when-let [bottom-border (-> cell :style :bottom-border)]
      (.setBottomBorderColor style (-> (:colour bottom-border :black) colours/colours .getIndex))
      (.setBorderBottom style (borders/border-styles (:style bottom-border :none))))
    (when-let [left-border (-> cell :style :left-border)]
      (.setLeftBorderColor style (-> (:colour left-border :black) colours/colours .getIndex))
      (.setBorderLeft style (borders/border-styles (:style left-border :none))))
    (when-let [right-border (-> cell :style :right-border)]
      (.setRightBorderColor style (-> (:colour right-border :black) colours/colours .getIndex))
      (.setBorderRight style (borders/border-styles (:style right-border :none))))
    (when-let [top-border (-> cell :style :top-border)]
      (.setTopBorderColor style (-> (:colour top-border :black) colours/colours .getIndex))
      (.setBorderTop style (borders/border-styles (:style top-border :none))))
    (when-let [background (-> cell :style :background-colour)]
        (.setFillForegroundColor style (-> background colours/colours .getIndex))
        (.setFillPattern style FillPatternType/SOLID_FOREGROUND))
    style))

(defn- create-cell-font! [workbook cell]
  (let [font (.createFont workbook)]
    (when (-> cell :style :bold)
      (.setBold font true))
    (when (-> cell :style :italic)
      (.setItalic font true))
    (when (-> cell :style :underline)
      (.setUnderline font FontUnderline/SINGLE))
    (when (-> cell :style :strikeout)
      (.setStrikeout font true))
    (when-let [font-size (-> cell :style :font-size)]
      (.setFontHeightInPoints font font-size))
    (when-let [font-colour (-> cell :style :font-colour)]
      (.setColor font (-> font-colour colours/colours .getIndex)))
    (when-let [font-name (-> cell :style :font-name)]
      (.setFontName font font-name))
    font))

;; TODO: Cache cell style and font whilst looping
(defn write-xlsx! [cells path]
  (let [workbook (XSSFWorkbook.)]
    (doall
      (for [cell cells]
        (let [sheet     (get-or-create-sheet! cell workbook)
              row       (get-or-create-row! cell sheet)
              poi-cell  (get-or-create-cell! cell row)
              style     (create-cell-style! workbook cell)
              font      (create-cell-font! workbook cell)]
         (.setCellValue poi-cell (ensure-settable (:value cell)))
         (.setFont style font)
         (.setCellStyle poi-cell style))))
    (.write workbook (FileOutputStream. path))
    (.close workbook)))

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
  (def cells (read-xlsx! "test/resources/dummy-spreadsheet.xlsx"))
  (map (comp :background-colour :style) cells)

  (println "end"))
