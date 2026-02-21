# reagent-extended-compiler

Custom Reagent compiler for CLJS-first interop.

This library keeps Hiccup syntax, while making JS component and JS prop interop
more ergonomic and predictable.

## What it provides

- Use JS components directly from Hiccup tags.
  - Example: `:rn/view`, `:gh/flat-list`, `:ui/button`
- Write props as normal CLJS data, with kebab-case keys.
  - They are converted to JS-style prop keys automatically.
- Enable deep conversion for maps inside selected vector props.
  - Example keys: `:style`, `:transform`, `:series`
- Keep CLJS map items when passing arrays to JS components.
  - Use `^:keep-items` when callbacks should receive CLJS maps (not JS objects).

## Install

```clojure
{:deps {reagent/reagent                                     {:mvn/version "1.3.0"}
        reagent-extended-compiler/reagent-extended-compiler {:local/root "reagent-extended-compiler"}}}
```

## Quick start

Create one compiler instance, set it as default once at app startup, and
require that setup namespace before rendering.

```clojure
(ns app.reagent-compiler-setup
  (:require [reagent.core :as r]
            [reagent-extended-compiler.core :as rec]
            ["my-ui-lib" :as ui-lib]
            ["my-gesture-lib" :as gesture-lib]
            ["my-animation-lib" :as animation-lib]))

(defonce compiler
  (rec/create
   {:function-components         true
    ;; Reagent function-component path (recommended default).
    :kebab-case-component-names? true
    ;; :rn/linear-gradient -> ui-lib.LinearGradient
    :js-component-libs           {;; :rn/view -> ui-lib.View
                                  :rn       ui-lib
                                  ;; :gh/gesture-detector -> gesture-lib.GestureDetector
                                  :gh       gesture-lib
                                  ;; :animated/view -> animation-lib.View
                                  :animated animation-lib}
    :convert-props-in-vectors    #{:style :content-container-style :transform}
    ;; Reagent already supports vector values.
    ;; This option controls deep key conversion for maps inside vectors
    ;; for these selected prop keys.
    }))

(r/set-default-compiler! compiler)
```

```clojure
(ns app.core
  (:require
   [app.reagent-compiler-setup]
   [reagent.core :as r]))
```

## Common configuration patterns

Use these as small templates depending on your app shape.

Unqualified tags (`:view`, `:text`) via `:root`:

```clojure
(rec/create
 {:function-components true
  :js-component-libs   {:root ui-lib}})
```

Aliased libraries plus irregular export names:

```clojure
(rec/create
 {:kebab-case-component-names? true
  :js-component-libs           {:ui    ui-lib
                                :gh    gesture-lib
                                ;; explicit mapping when export name is not predictable
                                :icons #js{:Search SearchIcon
                                           :Close  CloseIcon}}})
```

Vector-prop deep conversion:

```clojure
(rec/create
 {:convert-props-in-vectors #{:style :transform :series}})
```

## What it does

### 1) Component lookup from JS libs

With `:js-component-libs`, tags are resolved from configured libraries:

- `:rn/view` resolves from the `:rn` library
- `:gh/gesture-detector` resolves from the `:gh` library
- `:view` resolves from `:root` when `:root` is configured

When `:kebab-case-component-names?` is `true`, component names are converted to
PascalCase before lookup:

- `:rn/linear-gradient` -> `LinearGradient`
- `:gh/gesture-detector` -> `GestureDetector`

### 2) Prop key/value conversion

Props are converted recursively for JS interop:

- map keys are converted to JS prop names
- keywords/named values become strings
- maps and collections become JS values
- functions are wrapped so they can be called from JS

### 3) Deep conversion inside vector props

Reagent already supports vectors. This library adds optional deep conversion for
maps nested inside vectors, selected by `:convert-props-in-vectors`.

Example:

```clojure
;; with :convert-props-in-vectors containing :style and :series
[:ui/line-chart
 {:series [{:line-width 2}
           {:line-width 1}]
  :style  {:transform [{:translate-x 10}
                       {:translate-y 6}]}}]
```

In the example above, nested keys such as `:line-width` and `:translate-x` are
converted for JS props in those vector items.

### 4) Keep CLJS items in JS arrays (`^:keep-items`)

Without metadata, collection items are converted deeply to JS.

With `^:keep-items`, the outer collection is converted to a JS array, but each
item stays as original CLJS data.

React Native, without `^:keep-items`:

```clojure
(def rows [{:id 1 :name "Ada"}])

[:rn/flat-list
 {:data        rows
  :render-item (fn [^js x]
                 (let [item (.-item x)] ; JS object
                   [:rn/text (aget item "name")]))}]
```

React Native, with `^:keep-items`:

```clojure
(def rows ^:keep-items [{:id 1 :name "Ada"}])

[:rn/flat-list
 {:data        rows
  :render-item (fn [^js x]
                 (let [item (.-item x)] ; CLJS map
                   [:rn/text (:name item)]))}]
```

Web interop callback, without `^:keep-items`:

```clojure
;; Assume :ui/web-list is a JS component from :js-component-libs
;; and it calls :on-select with an item from :items.
(def rows [{:id 1 :name "Ada"}])

[:ui/web-list
 {:items     rows
  :on-select (fn [item] ; JS object
               (js/console.log (aget item "name")))}]
```

Web interop callback, with `^:keep-items`:

```clojure
(def rows ^:keep-items [{:id 1 :name "Ada"}])

[:ui/web-list
 {:items     rows
  :on-select (fn [item] ; CLJS map
               (js/console.log (:name item)))}]
```

## Utilities

### `style` and `defstyle` (macros)

Compile-time map transformation to JS-style objects.

```clojure
(ns user
  (:require [reagent-extended-compiler.utils.transforms :refer [defstyle style]]))

(defstyle row
  {:flex-direction :row
   :align-items    :center
   :column-gap     8})
;; row (shape):
;; #js {:flexDirection "row"
;;      :alignItems    "center"
;;      :columnGap     8}

(def dynamic
  (style {:transform [{:translate-x 10}
                      {:scale 0.98}]}))
;; dynamic (shape):
;; #js {:transform #js [#js {:translateX 10}
;;                      #js {:scale 0.98}]}
```

### `->js-prop-obj` (runtime helper)

Convert a CLJS map into a JS prop object at runtime:

```clojure
(require '[reagent-extended-compiler.utils.transforms :as transforms])

(def opts
  (transforms/->js-prop-obj
   {:line-width 2
    :legend     {:position :bottom}
    :padding    [8 12]}))
;; opts (shape):
;; #js {:lineWidth 2
;;      :legend    #js {:position "bottom"}
;;      :padding   #js [8 12]}
```

## Configuration reference

`reagent-extended-compiler.core/create` accepts:

- `:function-components`
  - `true` to use Reagent function-component path
- `:js-component-libs`
  - map of alias -> JS module/object
  - optional `:root` for unqualified tags like `:view`
- `:kebab-case-component-names?`
  - `true` to resolve component names as PascalCase
- `:convert-props-in-vectors`
  - set of prop keys where map keys inside vector values should also be
    converted

## Notes

- Reagent already handles vectors. `:convert-props-in-vectors` is specifically
  for deeper key conversion of maps nested inside selected vector props.
- `->js-prop-obj` uses the current default compiler, so behavior (including
  vector-prop conversion) follows your configured compiler options.
