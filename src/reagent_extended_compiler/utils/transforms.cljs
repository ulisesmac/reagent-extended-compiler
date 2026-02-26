(ns reagent-extended-compiler.utils.transforms
  (:require-macros [reagent-extended-compiler.utils.transforms])
  (:require [reagent-extended-compiler.utils.transforms.impl :as transforms-impl]
            [reagent-extended-compiler.prop-converter :as prop-converter]
            [reagent.impl.template :as template]))

(def map-array transforms-impl/map-array)

(defn ->js-prop-obj
  "Transforms a cljs map into a JS object with keys as props style.
   Recursive. Runtime. Fast - Cached."
  [m]
  (prop-converter/convert-custom-prop-value template/*current-default-compiler* m))

(defn- compose-styles-process [result entry]
  (if (vector? entry)
    (reduce compose-styles-process result entry)
    (conj! result entry)))

(defn add-styles
  "Compose a vector of style entries from various runtime values. Accepts
  maps, JS objects, vectors (recursively flattened), and ignores nil/false
  entries. Returns a vector suitable for React Native's style prop.

  Optimized to minimize intermediate allocations when flattening nested
  style vectors."
  [& styles]
  (persistent!
   (reduce compose-styles-process
           (transient [])
           styles)))
