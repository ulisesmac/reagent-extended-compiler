
(ns reagent-extended-compiler.utils
  (:require-macros [reagent-extended-compiler.utils :refer [style defstyle]]))

(defn map-array [f coll]
  (let [js-array ^js (array)]
    (doseq [e coll]
      (.push js-array (f e)))
    js-array))
