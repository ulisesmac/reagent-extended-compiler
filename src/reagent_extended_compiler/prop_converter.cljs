(ns reagent-extended-compiler.prop-converter
  (:require [goog.object :as gobj]
            [reagent-extended-compiler.protocols :as ep]
            [reagent-extended-compiler.utils :as extended.utils]
            [reagent.impl.template :as t]
            [reagent.impl.util :as util]))

(declare convert-prop-value)

(defn kv-conv [o k v]
  (doto o
    (gobj/set (t/cached-prop-name k) (convert-prop-value v))))

(defn convert-prop-value [x]
  (cond
    (util/js-val? x) x
    (util/named? x) (name x)
    (map? x) (reduce-kv kv-conv #js {} x)
    (vector? x) (extended.utils/map-array convert-prop-value x)
    (coll? x) (clj->js x)
    (ifn? x) (fn [& args]
               (apply x args))
    :else (clj->js x)))

(defn custom-kv-conv [o k v]
  (doto o
    (gobj/set (t/cached-custom-prop-name k) (convert-prop-value v))))

(defn convert-custom-prop-value [x]
  (cond
    (util/js-val? x) x
    (util/named? x) (name x)
    (map? x) (reduce-kv custom-kv-conv #js{} x)
    (vector? x) (extended.utils/map-array convert-custom-prop-value x)
    (coll? x) (clj->js x)
    (ifn? x) (fn [& args]
               (apply x args))
    :else (clj->js x)))


(defn convert-props [props ^clj id-class compiler]
  (let [class (:class props)
        props (-> props
                  (cond-> class (assoc :class (util/class-names class)))
                  (t/set-id-class id-class))]
    (cond
      (and (.-custom id-class)
           (ep/convert-props-in-vectors? compiler))
      (convert-custom-prop-value props)

      (.-custom id-class)
      (t/convert-custom-prop-value props)

      (ep/convert-props-in-vectors? compiler)
      (convert-prop-value props)

      :else
      (t/convert-prop-value props))))
