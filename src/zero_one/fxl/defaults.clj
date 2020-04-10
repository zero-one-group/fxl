(ns zero-one.fxl.defaults)

(def value nil)

(def coord {:row 0 :col 0})

(def style
 {; Fonts
  :bold              false,
  :italic            false,
  :underline         false,
  :strikeout         false
  :font-size         12,
  :font-name         "Calibri",
  :font-colour       :black1,
  ; Alignments
  :horizontal        :general,
  :vertical          :bottom,
  ; Borders
  :bottom-border     {:style :none, :colour :black},
  :left-border       {:style :none, :colour :black},
  :right-border      {:style :none, :colour :black},
  :top-border        {:style :none, :colour :black},
  ; Others
  :background-colour :automatic
  :data-format       "General"
  :row-size          15
  :col-size          8})


(def cell
  {:value value
   :coord coord
   :style style})
