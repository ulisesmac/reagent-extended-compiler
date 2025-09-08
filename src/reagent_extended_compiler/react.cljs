(ns reagent-extended-compiler.react
  (:require
   ["react" :refer [useEffect useState useCallback useMemo useRef]]))

(defn- fn-wrapper [f]
  (fn []
    (let [result (f)]
      (if (fn? result)
        result
        js/undefined))))

(defn use-effect
  ([f]
   (useEffect (fn-wrapper f)))
  ([f deps]
   (useEffect (fn-wrapper f) (clj->js deps))))

(defn use-callback [f deps]
  (useCallback f (to-array deps)))

(def use-state useState)

(defn use-memo [f deps]
  (useMemo f (to-array deps)))

(defn use-ref [initial-value]
  ^js (useRef initial-value))

(defn use-pass-clj-data
  "Specific for Reagent: memoize a collection but keep items as-is."
  [coll]
  (use-memo #(with-meta coll {:keep-items true})
            [(hash coll)]))
