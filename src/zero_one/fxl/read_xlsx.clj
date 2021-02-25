(ns zero-one.fxl.read-xlsx
  (:require
   [failjure.core :as f]
   [zero-one.fxl.alignments :as alignments]
   [zero-one.fxl.borders :as borders]
   [zero-one.fxl.colours :as colours]
   [zero-one.fxl.defaults :as defaults])
  (:import
   [java.io FileInputStream]
   [org.apache.poi.xssf.usermodel XSSFWorkbook]
   [org.apache.poi.ss.usermodel Font]))

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

(defn- extract-cell-formula [cell]
  (let [cell-type (.. cell getCellType name)]
    (case cell-type
      "NUMERIC" nil
      "STRING"  nil
      "BOOLEAN" nil
      "ERROR"   (.getErrorCellString cell)
      "FORMULA" (.getCellFormula cell)
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
     :underline   (not= (.getUnderline font) Font/U_NONE)
     :font-name   (.getFontName font)
     :font-colour (-> font .getColor colours/colours-lookup)
     :font-size   (.getFontHeightInPoints font)}))

(defn- prune-cell-style [cell-style]
  (into {}
        (filter
         (fn [[k v]] (not= v (defaults/style k)))
         cell-style)))

(defn- extract-cell-style [workbook cell]
  (let [col-index  (.getColumnIndex cell)
        cell-style (merge
                    (extract-cell-border-style cell)
                    (extract-cell-alignment-style cell)
                    (extract-cell-font-style workbook cell)
                    {:background-colour (-> cell
                                            .getCellStyle
                                            .getFillForegroundColor
                                            colours/colours-lookup)
                     :data-format       (-> cell
                                            .getCellStyle
                                            .getDataFormatString)
                     :col-size          (-> cell
                                            .getSheet
                                            (.getColumnWidth col-index)
                                            (/ 256.0)
                                            int)
                     :row-size          (-> cell
                                            .getRow
                                            .getHeightInPoints
                                            int)})]
    (prune-cell-style cell-style)))

(defn- extract-poi-cells [workbook]
  (letfn [(->seq [iterable] (-> iterable .iterator iterator-seq))]
    (->> workbook
         ->seq
         (mapcat ->seq)
         (mapcat ->seq))))

(defn- poi-cell->fxl-cell [workbook poi-cell]
  (let [poi-row (.getRow poi-cell)]
    {:coord {:row   (.getRowNum poi-row)
             :col   (.getColumnIndex poi-cell)
             :sheet (.. poi-row getSheet getSheetName)}
     :value   (extract-cell-value poi-cell)
     :formula (extract-cell-formula poi-cell)
     :style   (extract-cell-style workbook poi-cell)}))

(defn- throwable-read-xlsx! [path]
  (let [input-stream (FileInputStream. path)
        workbook     (XSSFWorkbook. input-stream)
        poi-cells    (extract-poi-cells workbook)
        cells        (map #(poi-cell->fxl-cell workbook %) poi-cells)]
    (.close workbook)
    cells))

(defn read-xlsx! [path]
  (f/try* (throwable-read-xlsx! path)))

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
  (count (map #(.getIndex %) (vals colours/colours)))
  (count colours/colours)

  (def cells (-> row .iterator iterator-seq))
  (def cell (first cells))
  (def cell-style (.getCellStyle cell))
  (-> cell-style .getFillBackgroundColor colours/colours-lookup)
  (def cells (read-xlsx! "test/resources/dummy-spreadsheet.xlsx"))
  (map (comp :background-colour :style) cells)

  (println "end"))
