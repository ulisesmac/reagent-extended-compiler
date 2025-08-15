(ns reagent-extended-compiler.utils
  (:require [clojure.string :as str]))

(defn- capitalize-first [^String s]
  (if (seq s)
    (str (.toUpperCase (subs s 0 1)) (subs s 1))
    s))

(defn- kebab->camel-str [k]
  (let [s     (cond
                (keyword? k) (name k)
                (symbol? k) (name k)
                :else (str k))
        parts (str/split s #"-")]
    (if (seq (rest parts))
      (apply str (first parts) (map capitalize-first (rest parts)))
      s)))

(declare emit-style)

(defn- emit-style-map [m]
  (let [pairs (mapcat (fn [[k v]]
                        [(kebab->camel-str k) (emit-style v)])
                      m)]
    `(~'js-obj ~@pairs)))

(defn- emit-style [form]
  (cond
    (map? form) (emit-style-map form)
    (vector? form) `(~'array ~@(map emit-style form))
    (keyword? form) (name form)
    :else form))

(defmacro style
  "Macro that takes a CLJS map and expands into an expression that creates a
   JS object with camelCased keys. All key-name conversions happen at macro
   time in CLJ; values are left as expressions for CLJS to evaluate."
  [m]
  (emit-style m))

(defmacro defstyle
  "Defines a CLJS var bound to `(style m)`.
   Usage: (defstyle my-style {:font-size 14})"
  [name m]
  `(def ~name ~(emit-style m)))

(comment

 (macroexpand-1 '(style {:padding-left    24
                            :padding-right   20
                            :padding-top     12
                            :padding-bottom  8
                            :flex-direction  :row
                            :align-items     :center
                            :justify-content :space-between
                            :column-gap      4
                            :transform       [{:translate-x 1}
                                              {:translate-y 1}]}))

 (macroexpand-1 '(defstyle base-padding {:padding-left 12 :padding-right 12}))
)
