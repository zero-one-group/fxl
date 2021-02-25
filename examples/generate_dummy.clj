(require '[zero-one.fxl.core :as fxl])

;; (fxl/read-xlsx! "test/resources/dummy-spreadsheet.xlsx")

(def cells '({:coord {:row 0, :col 0, :sheet "Sheet1"},
              :value "A",
              :formula nil,
              :style {:bold true}}
             {:coord {:row 0, :col 1, :sheet "Sheet1"},
              :value "B",
              :formula nil,
              :style {:italic true}}
             {:coord {:row 0, :col 2, :sheet "Sheet1"},
              :value "C",
              :formula nil,
              :style {:underline true}}
             {:coord {:row 1, :col 0, :sheet "Sheet1"},
              :value 1.0,
              :formula nil,
              :style {:strikeout true}}
             {:coord {:row 1, :col 1, :sheet "Sheet1"},
              :value 2.0,
              :formula nil,
              :style {:font-name "Times New Roman"}}
             {:coord {:row 1, :col 2, :sheet "Sheet1"},
              :value 3.1416,
              :formula nil,
              :style {:font-colour :red}}
             {:coord {:row 2, :col 0, :sheet "Sheet1"},
              :value 2.0,
              :formula nil,
              :style {:font-size 14, :row-size 19}}
             {:coord {:row 2, :col 1, :sheet "Sheet1"},
              :value 4.0,
              :formula nil,
              :style {:vertical :center, :row-size 19}}
             {:coord {:row 2, :col 2, :sheet "Sheet1"},
              :value 2.7183,
              :formula nil,
              :style {:vertical :top, :row-size 19}}
             {:coord {:row 3, :col 0, :sheet "Sheet1"},
              :value 3.0,
              :formula nil,
              :style {:horizontal :center}}
             {:coord {:row 3, :col 1, :sheet "Sheet1"},
              :value 8.0,
              :formula nil,
              :style {:horizontal :left}}
             {:coord {:row 3, :col 2, :sheet "Sheet1"},
              :value 1.4142,
              :formula nil,
              :style {:horizontal :right}}
             {:coord {:row 4, :col 0, :sheet "Sheet1"},
              :value 6.0,
              :formula "SUM(A2:A4)",
              :style {:bottom-border {:style :thin, :colour :black1}}}
             {:coord {:row 4, :col 1, :sheet "Sheet1"},
              :value 64.0,
              :formula "PRODUCT(B2:B4)",
              :style {:background-colour :grey_50_percent}}
             {:coord {:row 4, :col 2, :sheet "Sheet1"},
              :value 2.4247,
              :formula "AVERAGE(C2:C4)",
              :style {:background-colour :yellow
                      :data-format       "0.00"}}
             {:coord {:row 0, :col 0, :sheet "Sheet2"},
              :value "X",
              :formula nil,
              :style {}}
             {:coord {:row 0, :col 1, :sheet "Sheet2"},
              :value "Y",
              :formula nil,
              :style {}}
             {:coord {:row 0, :col 2, :sheet "Sheet2"},
              :value "Z",
              :formula nil,
              :style {}}
             {:coord {:row 1, :col 0, :sheet "Sheet2"},
              :value "6",
              :formula "TRUE()",
              :style {}}
             {:coord {:row 1, :col 2, :sheet "Sheet2"},
              :value "7",
              :formula "FALSE()",
              :style {}}
             {:coord {:row 2, :col 0, :sheet "Sheet2"},
              :value "8",
              :formula "1/0",
              :style {}}
             {:coord {:row 2, :col 1, :sheet "Sheet2"},
              :value "9",
              :formula "#N/A",
              :style {}}
             {:coord {:row 2, :col 2, :sheet "Sheet2"},
              :value "9",
              :formula "#N/A",
              :style {}}))

(fxl/write-xlsx!
 cells
 "test/resources/dummy-spreadsheet.xlsx")
