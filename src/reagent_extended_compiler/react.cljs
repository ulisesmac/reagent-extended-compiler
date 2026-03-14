(ns reagent-extended-compiler.react
  (:require
   ["react" :refer [useEffect useState useCallback useMemo useRef]]
   [reagent-extended-compiler.utils.transforms :as transforms]))

(defn- fn-wrapper [f]
  (fn []
    (let [result (f)]
      (if (fn? result)
        result
        js/undefined))))

(defn dep-value [x]
  (if (keyword? x) (name x) x))

(defn deps-array
  "Convert hook deps to a JS array, naming keywords and leaving other values as-is."
  [deps]
  (transforms/map-array dep-value deps))

(defn use-effect
  ([f]
   (useEffect (fn-wrapper f)))
  ([f deps]
   (useEffect (fn-wrapper f) (deps-array deps))))

(defn use-callback [f deps]
  (useCallback f (deps-array deps)))

(def use-state useState)

(defn use-memo [f deps]
  (useMemo f (deps-array deps)))

(defn use-ref [initial-value]
  ^js (useRef initial-value))

(defn use-pass-clj-data
  "Specific for Reagent: memoize a collection but keep items as-is."
  [coll]
  (use-memo #(with-meta coll {:keep-items true})
            [(hash coll)]))
