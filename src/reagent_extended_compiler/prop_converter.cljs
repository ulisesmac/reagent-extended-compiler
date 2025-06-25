(ns reagent-extended-compiler.prop-converter
  (:require [goog.object :as gobj]
            [reagent-extended-compiler.protocols :as ep]
            [reagent-extended-compiler.utils :as extended.utils]
            [reagent.impl.template :as t]
            [reagent.impl.util :as util]))

(def prop-to-convert?
  (memoize
   (fn [compiler k]
     (gobj/get (ep/convert-props-in-vectors compiler) (name k)))))

(declare convert-prop-value)

(defn kv-conv [compiler convert-in-vector? o k v]
  (doto o
    (gobj/set (t/cached-prop-name k) (if (or convert-in-vector?
                                             (prop-to-convert? compiler k))
                                       (convert-prop-value compiler v true)
                                       (convert-prop-value compiler v false)))))

(defn convert-prop-value [compiler x convert-in-vector?]
  (cond
    (util/js-val? x) x
    (util/named? x) (name x)
    (map? x) (reduce-kv (partial kv-conv compiler convert-in-vector?) #js {} x)
    (and convert-in-vector? (vector? x)) (extended.utils/map-array #(convert-prop-value compiler % true) x)
    (coll? x) (clj->js x)
    (ifn? x) (fn [& args]
               (apply x args))
    :else (clj->js x)))

(defn custom-kv-conv [compiler o k v]
  (doto o
    (gobj/set (t/cached-custom-prop-name k) (if (prop-to-convert? compiler k)
                                              (convert-prop-value compiler v true)
                                              (convert-prop-value compiler v false)))))

(defn convert-custom-prop-value [compiler x]
  (let [{:keys [keep-items]} (meta x)]
    (cond
      (util/js-val? x) x
      (util/named? x)  (name x)
      (map? x)         (reduce-kv (partial custom-kv-conv compiler) #js{} x)
      (and (coll? x)
           keep-items) (to-array x)
      (coll? x)        (clj->js x)
      (ifn? x)         (fn [& args]
                         (apply x args))
      :else            (clj->js x))))


(defn convert-props [props ^clj id-class compiler]
  (let [class (:class props)
        props (-> props
                  (cond-> class (assoc :class (util/class-names class)))
                  (t/set-id-class id-class))]
    (cond
      (and (.-custom id-class)
           (some? (ep/convert-props-in-vectors compiler)))
      (convert-custom-prop-value compiler props)

      (.-custom id-class)
      (t/convert-custom-prop-value props)

      (some? (ep/convert-props-in-vectors compiler))
      (convert-prop-value compiler props false)

      :else
      (t/convert-prop-value props))))
