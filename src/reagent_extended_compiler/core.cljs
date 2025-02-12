(ns reagent-extended-compiler.core
  (:require [reagent-extended-compiler.compiler :as compiler]
            [reagent.impl.template :as t]
            [camel-snake-kebab.core :as csk]
            [applied-science.js-interop :as j]))

(defn js-component-names->clj-names [libs]
  (time
   (let [libs-fixed (js-obj)]
     (doseq [[alias js-lib-components] libs
             [component-name component] (js/Object.entries js-lib-components)]
       (j/assoc-in! libs-fixed [(name alias) (csk/->kebab-case component-name)] component))
     libs-fixed)))

(defn create
  [{:keys [function-components js-component-libs convert-props-in-vectors? kebab-case-component-names?]
    :or   {js-component-libs {}}
    :as   opts}]
  (let [id             (gensym "reagent-extended-compiler")
        fn-to-element  (if function-components
                         t/maybe-function-element
                         t/reag-element)
        parse-fn       (get opts :parse-tag compiler/cached-parse)
        component-libs (if kebab-case-component-names?
                         (js-component-names->clj-names js-component-libs)
                         (clj->js js-component-libs))]
    (compiler/->ExtendedCompiler
     id
     fn-to-element
     parse-fn
     component-libs
     convert-props-in-vectors?
     kebab-case-component-names?)))
