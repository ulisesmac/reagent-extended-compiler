(ns reagent-extended-compiler.utils.transforms.impl)

(defn map-array [f coll]
  (let [js-array ^js (array)]
    (doseq [e coll]
      (.push js-array (f e)))
    js-array))

