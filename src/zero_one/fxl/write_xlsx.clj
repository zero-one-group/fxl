(ns zero-one.fxl.write-xlsx
  (:require
    [failjure.core :as f]
    [zero-one.fxl.alignments :as alignments]
    [zero-one.fxl.borders :as borders]
    [zero-one.fxl.colours :as colours]
    [zero-one.fxl.data-formats :as data-formats]
    [zero-one.fxl.defaults :as defaults]
    [zero-one.fxl.specs :as fs])
  (:import
    (java.io FileOutputStream)
    (org.apache.poi.xssf.usermodel XSSFWorkbook)
    (org.apache.poi.ss.usermodel FillPatternType FontUnderline)
    (org.apache.poi.ss.util CellRangeAddress)))

;; Apache POI Navigation
(defn- get-or-create-sheet! [cell workbook]
  (let [sheet-name (get-in cell [:coord :sheet] defaults/sheet)]
    (or (.getSheet workbook sheet-name)
        (.createSheet workbook sheet-name))))

(defn- get-or-create-row! [cell xl-sheet]
  (let [row-index (or (-> cell :coord :row)
                      (-> cell :coord :first-row))]
    (or (.getRow xl-sheet row-index)
        (.createRow xl-sheet row-index))))

(defn- get-or-create-cell! [cell xl-row]
  (let [col-index (or (-> cell :coord :col)
                      (-> cell :coord :first-col))]
    (or (.getCell xl-row col-index)
        (.createCell xl-row col-index))))

(defn- ensure-settable [value]
  (if (number? value)
    (double value)
    value))

;; Cell Style and Font
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

(defn- accumulate-style-cache! [workbook current-cache cell]
  (let [fxl-style (:style cell)]
    (if (contains? current-cache fxl-style)
      current-cache
      (let [poi-style (create-cell-style! workbook cell)
            poi-font  (create-cell-font! workbook cell)]
        (.setFont poi-style poi-font)
        (assoc current-cache fxl-style poi-style)))))

;; Row and Column Sizing
(defn- min-size [axis cells]
  (let [coord-key ({:row :row-size :col :col-size} axis)
        sizes     (->> cells
                        (map (comp coord-key :style))
                        (filter some?))]
    (if (some #(= % :auto) sizes)
      :auto
      (apply max -1 sizes))))

(defn- partial-coord [axis cell]
  {:sheet (get-in cell [:coord :sheet] defaults/sheet)
   axis   (get-in cell [:coord axis])})

(defn- grouped-min-size [axis cells]
  (let [grouped-cells (group-by #(partial-coord axis %) cells)]
    (into {}
      (for [[index group] grouped-cells
            :let [min-axis-size (min-size axis group)]
            :when (not= -1 min-axis-size)]
        [index min-axis-size]))))

;; Writing to Excel
(defn build-context! [workbook cells]
  {:min-row-sizes (grouped-min-size :row cells)
   :min-col-sizes (grouped-min-size :col cells)
   :cell-styles   (reduce #(accumulate-style-cache! workbook %1 %2) {} cells)})

(defn- create-merged-region! [cell sheet]
  (let [first-row (-> cell :coord :first-row)
        first-col (-> cell :coord :first-col)
        last-row  (-> cell :coord :last-row)
        last-col  (-> cell :coord :last-col)
        cell-range-address (CellRangeAddress.
                             first-row last-row
                             first-col last-col)]
    (.addMergedRegion sheet cell-range-address)))

(defn merged-cell? [cell]
  (let [coord (:coord cell)]
    (and (contains? coord :last-row)
      (contains? coord :last-col))))

(defn- set-cell-value-and-style! [context workbook cell]
  (let [sheet     (get-or-create-sheet! cell workbook)
        row       (get-or-create-row! cell sheet)
        poi-cell  (get-or-create-cell! cell row)
        style     ((:cell-styles context) (:style cell))]
    (.setCellValue poi-cell (ensure-settable (:value cell)))
    (.setCellFormula poi-cell (:formula cell))
    (.setCellStyle poi-cell style)
    (when (merged-cell? cell)
      (create-merged-region! cell sheet))))

(defn- set-row-height! [workbook coord row-size]
  (let [row-index (or (:row coord) (:first-row coord))
        sheet     (.getSheet workbook (:sheet coord))
        row       (.getRow sheet row-index)]
    (.setHeightInPoints row (float row-size))))

(defn- set-col-width! [workbook coord col-size]
  (let [col-index (:col coord)
        sheet     (.getSheet workbook (:sheet coord))]
    (if (= col-size :auto)
      (.autoSizeColumn sheet col-index)
      (.setColumnWidth sheet col-index (* col-size 256)))))

(defn- throwable-write-xlsx! [cells path]
  (let [workbook      (XSSFWorkbook.)
        output-stream (FileOutputStream. path)
        context       (build-context! workbook cells)]
    (doall (for [cell cells]
             (set-cell-value-and-style! context workbook cell)))
    (doall (for [[coord row-size] (:min-row-sizes context)]
             (set-row-height! workbook coord row-size)))
    (doall (for [[coord col-size] (:min-col-sizes context)]
            (set-col-width! workbook coord col-size)))
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
