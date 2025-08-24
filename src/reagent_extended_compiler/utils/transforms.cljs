(ns reagent-extended-compiler.utils.transforms
  (:require-macros [reagent-extended-compiler.utils.transforms])
  (:require [reagent-extended-compiler.utils.transforms.impl :as transforms-impl]
            [reagent-extended-compiler.prop-converter :as prop-converter]
            [reagent.impl.template :as template]))

(def map-array transforms-impl/map-array)

(defn ->js-prop-obj [m]
  "Transforms a cljs map into a JS object with keys as props style.
   Recursive. Runtime. Fast - Cached."
  (prop-converter/convert-custom-prop-value template/*current-default-compiler* m))
