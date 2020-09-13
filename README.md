<p align="center">
    <img src="logo/fxl.png" width="375px">
</p>

`fxl` (*/ˈfɪk.səl/* or "pixel" with an f) is a Clojure library for manipulating spreadsheets.

[![Continuous Integration](https://github.com/zero-one-group/fxl/workflows/Continuous%20Integration/badge.svg?branch=develop)](https://github.com/zero-one-group/fxl/commits/develop)
[![Code Coverage](https://codecov.io/gh/zero-one-group/fxl/branch/develop/graph/badge.svg)](https://codecov.io/gh/zero-one-group/fxl)
[![Clojars Project](https://img.shields.io/clojars/v/zero.one/fxl.svg)](http://clojars.org/zero.one/fxl)

WARNING! This library is still unstable. Some information here may be outdated. Do not use it in production just yet!

See [docjure](https://github.com/mjul/docjure) and [excel-clj](https://github.com/matthewdowney/excel-clj/tree/master/src/excel_clj) for more mature alternatives.

# Introduction

The goal of the project is to provide a composable data-oriented spreadsheet interface for Clojure. The library is written with simplicity in mind - particularly as discussed in Rich Hickey's talk [Simplicity Matters](https://www.youtube.com/watch?v=rI8tNMsozo0) on the [list-and-order problem](https://youtu.be/rI8tNMsozo0?t=1448).

<blockquote>
    <p> 
        If order matters, complexity has been introduced to the system
    </p>
    &mdash;
    <a href="https://youtu.be/rI8tNMsozo0">
        Rich Hickey, Simplicity Matters
    </a>
</blockquote>

What `fxl` attempts to do differently to [docjure](https://github.com/mjul/docjure) and [excel-clj](https://github.com/matthewdowney/excel-clj/tree/master/src/excel_clj) is to represent spreadsheets as an unordered collection of maps, instead of relying on tabular formats. This makes it easier to deal with independent, smaller components of the spreadsheet and simply apply `concat` to put together different components of a spreadsheet.

[![cljdoc](https://cljdoc.org/badge/zero.one/fxl)](https://cljdoc.org/d/zero.one/fxl/CURRENT)
[![slack](https://badgen.net/badge/-/clojurians%2Ffxl?icon=slack&label)](https://clojurians.slack.com/messages/fxl/)
[![zulip](https://img.shields.io/badge/zulip-clojurians%2Ffxl-brightgreen.svg)](https://clojurians.zulipchat.com/#narrow/stream/257213-fxl)

# Examples

## Map Representation of Cells

A `fxl` cell is represented by a map that tells us its value, location and style. For instance:

```clojure
{:value -2.2
 :coord {:row 4 :col 3 :sheet "Growth"}
 :style {:data-format "0.00%" :background-colour :yellow}}
```

is rendered as a highlighted cell with a value of "-2.2%" on the fifth row and fourth column of a sheet called "Growth".

By knowing cells, you know almost all of `fxl`! The rest of the library is composed of IO functions such as `read-xlsx!` and `write-xlsx!` and helper functions to transform Clojure data structures into cell maps.

To find out more about the available styles, see their [specs](https://gitlab.com/zero-one-open-source/fxl/-/blob/develop/src/zero_one/fxl/specs.clj).

## Creating Simple Spreadsheets with Builtin Clojure

Suppose we would like to create a spreadsheet such as the following:

```
| Item     | Cost     |
| -------- | -------- |
| Rent     | 1000     |
| Gas      | 100      |
| Food     | 300      |
| Gym      | 50       |
|          |          |
| Total    | 1450     |
```

Assume that we have the cost data in the following form:

``` clojure
(def costs
  [{:item "Rent" :cost 1000}
   {:item "Gas"  :cost 100}
   {:item "Food" :cost 300}
   {:item "Gym"  :cost 50}]
```

We would break the spreadsheet down into three components, namely the header, the body and the total:

``` clojure
(require '[zero-one.fxl.core :as fxl])

(def header-cells
  [{:value "Item" :coord {:row 0 :col 0} :style {}}
   {:value "Cost" :coord {:row 0 :col 1} :style {}}])

(def body-cells
  (flatten
    (for [[row cost] (map vector (range) costs)]
      (list
        {:value (:item cost) :coord {:row (inc row) :col 0} :style {}}
        {:value (:cost cost) :coord {:row (inc row) :col 1} :style {}}))))

(def total-cells
  (let [row        (count costs)
        total-cost (apply + (map :cost costs))]
    [{:value "Total"    :coord {:row (+ row 2) :col 0} :style {}}
     {:value total-cost :coord {:row (+ row 2) :col 1} :style {}}]))

(fxl/write-xlsx!
  (concat header-cells body-cells total-cells)
  "examples/spreadsheets/write_to_plain_excel.xlsx")
```

This works, but dealing with the coordinates are fiddly. We can make the intent clearer using `fxl` helper functions.

## Creating Simple Spreadsheets with Helper Functions

Here we use `row->cells`, `table->cells`, `pad-below` and `concat-below` to help us initialise and navigate relative coordinates.

``` clojure
(def header-cells (fxl/row->cells ["Item" "Cost"]))

(def body-cells
  (fxl/records->cells [:item :cost] costs))

(def total-cells
  (let [total-cost (apply + (map :cost costs))]
    (fxl/row->cells ["Total" total-cost])))

(fxl/write-xlsx!
  (fxl/concat-below header-cells
                    (fxl/pad-below body-cells)
                    total-cells)
  "examples/spreadsheets/write_to_plain_excel_with_helpers.xlsx")
```

More helper functions are available - see [here](https://gitlab.com/zero-one-open-source/fxl/-/blob/develop/src/zero_one/fxl/core.clj).

## Modular Styling

With a Clojure-map representation for cells, manipulating the spreadsheet is easy using built-in functions. Suppose we would like to:

1. highlight the header row and make it bold
2. make the total row bold
2. horizontally align all cells to the center

We can achieve this by composing simple styling functions:

``` clojure
(defn bold [cell]
  (assoc-in cell [:style :bold] true))

(defn highlight [cell]
  (assoc-in cell [:style :background-colour] :grey_25_percent))

(defn align-center [cell]
  (assoc-in cell [:style :horizontal] :center))

(def all-cells
  (map align-center
    (fxl/concat-below
      (map (comp bold highlight) header-cells)
      (fxl/pad-below body-cells)
      (map bold total-cells))))
```

# Installation

Add the following to your `project.clj` dependency:

[![Clojars Project](https://clojars.org/zero.one/fxl/latest-version.svg)](http://clojars.org/zero.one/fxl)

# Future Work

Features:
- Core:
    - Column width and row heights.
    - Freezing panes.
    - Excel coords -> index coords.
    - Support for formulae.
    - Support merged cells.
    - Support data-val cells.
- Support to Google Sheet API.
- Error handling with `failjure`.
- Property-based testing.

# License

Copyright 2020 Zero One Group.

fxl is licensed under Apache License v2.0.
