# Reagent Extended Compiler

A drop-in Reagent compiler that makes CLJS-first UI ergonomic.
Works anywhere Reagent runs (web and React Native); React Native users benefit most:

- CLJS maps for props/styles with kebab-case keys auto-converted to camelCase.
- Optional conversion of vectors within props (e.g., `:style`, `:transform`).
- Component library lookup (e.g., `:rn/view` resolved from a JS lib map).
- Handy transforms: `style`/`defstyle` macros and `->js-prop-obj` at runtime.

## Install

- deps.edn (example in a monorepo):

```clojure
{:deps {reagent/reagent                                     {:mvn/version "1.3.0"}
        reagent-extended-compiler/reagent-extended-compiler {:local/root "reagent-extended-compiler"}}}
```

## Setup

Create and set the compiler once at app startup. Configure:

- `:js-component-libs`: where tags resolve from (root and named libs).
- `:convert-props-in-vectors`: props whose vector values should be converted item-wise.
- `:kebab-case-component-names?`: convert `:rn/linear-gradient` → `LinearGradient`.
- Load order: require your setup ns first in your app entry (or add to Shadow CLJS `:preloads`).

Setup namespace:

```clojure
(ns app.reagent-compiler-setup
  (:require [reagent.core :as r]
            [reagent-extended-compiler.core :as rec]
            ["react-native" :as rn]
            ["react-native-gesture-handler" :as gh]
            ["react-native-linear-gradient" :default LinearGradient]))

(defonce compiler
  (rec/create
   {:function-components         true ;; The one from reagent
    :kebab-case-component-names? true
    :convert-props-in-vectors    #{:style :content-container-style :transform}
    :js-component-libs           {:root            rn             ;; :view, :text, ...
                                  :rn              rn             ;; :rn/view, :rn/text, ...
                                  :gh              gh             ;; gesture-handler
                                  :linear-gradient #js{:View LinearGradient}}}))

(r/set-default-compiler! compiler)
```

Entry point (require first):

```clojure
(ns app.core
  (:require
   [app.reagent-compiler-setup]   ;; keep this first so the compiler is set
   ...
   [reagent.core :as r]
   [reagent.dom :as rdom]))       ;; or RN root
```

## Usage

With the compiler set, Hiccup becomes data-first and concise:

```clojure
[:rn/view {:style {:padding-horizontal 16
                   :row-gap            8}}
 [:rn/text {:style {:font-weight :bold}} "Hello"]]
```

Vectors inside props (e.g., `:transform`) are converted when listed in
`:convert-props-in-vectors`:

```clojure
[:rn/view {:style {:transform [{:translate-x 10}
                               {:translate-y 6}]}}]
```

### Styles and Transforms

Use the macros and runtime helper from `utils.transforms`.

```clojure
(ns ui.styles
  (:require [reagent-extended-compiler.utils.transforms :refer [defstyle]]
            [reagent.core :as r]))

;; Macro: compile-time conversion to JS object
(defstyle base-padding {:padding-left 12 :padding-right 12})

```

### Keep Items (preserve CLJS maps)

By default, collections inside props are converted with `clj->js`, so nested maps
become plain JS objects (and round-tripping back loses keyword namespaces).

Add metadata `{:keep-items true}` to a collection to keep its items as-is (CLJS data)
while only wrapping the outer collection as a JS array:

```clojure
(def items ^:keep-items[{:user/id 1} {:user/id 2}])

[:rn/flat-list {:data        items
                :key-extractor (fn [m _] (str (:user/id m)))
                :render-item (fn [m]
                               (let [item (.-item m)]  ; item is the original CLJS map
                                 [:rn/text (pr-str (:user/id item))]))}]
```

This preserves CLJS maps (including keyword namespaces) across React component boundaries
without re-converting JS→CLJ.

### Component lookup

When `:js-component-libs` is configured, tags are resolved from your JS libs:

- `:view` or `:text` resolve from `:root`.
- `:rn/view` or `:rn/text` resolve from the `:rn` lib.
- With `:kebab-case-component-names? true`, names are converted (e.g., `:rn/linear-gradient` → `LinearGradient`).

## Notes

- Add keys to `:convert-props-in-vectors` for any prop that contains vectors/arrays.
