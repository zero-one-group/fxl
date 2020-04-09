(ns zero-one.fxl.write-xlsx
  (:require
    [failjure.core :as f]
    [zero-one.fxl.alignments :as alignments]
    [zero-one.fxl.borders :as borders]
    [zero-one.fxl.colours :as colours]
    [zero-one.fxl.data-formats :as data-formats]
    [zero-one.fxl.specs :as fs])
  (:import
    [java.io FileOutputStream]
    [org.apache.poi.xssf.usermodel XSSFWorkbook]
    [org.apache.poi.ss.usermodel FillPatternType FontUnderline]))

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
    (when-let [data-format (-> cell :style :data-format)]
      (when-let [index (data-formats/data-format-lookup data-format)]
        (.setDataFormat style index)))
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
(defn- throwable-write-xlsx! [cells path]
  (let [workbook      (XSSFWorkbook.)
        output-stream (FileOutputStream. path)]
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
    (.write workbook output-stream)
    (.close workbook)
    {:workbook workbook :output-stream output-stream}))

(defn conform-cells [cells]
  (if (every? #(fs/valid? ::fs/cell %) cells)
    cells
    (f/fail "Invalid cell specs.")))

(defn write-xlsx! [cells path]
  (f/attempt-all [cells  (conform-cells cells)
                  result (f/try* (throwable-write-xlsx! cells path))]
    result))
