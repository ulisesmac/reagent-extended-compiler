(ns reagent-extended-compiler.core
  (:require [reagent-extended-compiler.compiler :as compiler]
            [reagent.impl.template :as t]))

(defn create
  [{:keys [function-components js-component-libs convert-props-in-vectors? kebab-case-component-names?]
    :or   {js-component-libs {}}
    :as   opts}]
  (let [id             (gensym "reagent-extended-compiler")
        fn-to-element  (if function-components
                         t/maybe-function-element
                         t/reag-element)
        parse-fn       (get opts :parse-tag compiler/cached-parse)
        component-libs (clj->js js-component-libs)]
    (compiler/->ExtendedCompiler
     id
     fn-to-element
     parse-fn
     component-libs
     convert-props-in-vectors?
     kebab-case-component-names?)))
