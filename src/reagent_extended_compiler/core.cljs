(ns reagent-extended-compiler.core
  (:require [reagent-extended-compiler.compiler :as compiler]
            [reagent.impl.template :as t]))

(defn create
  [{:keys [function-components project-libs convert-props-in-vectors?]
    :or   {project-libs {}}
    :as   opts}]
  (let [id            (gensym "reagent-extended-compiler")
        fn-to-element (if function-components
                        t/maybe-function-element
                        t/reag-element)
        parse-fn      (get opts :parse-tag compiler/cached-parse)]
    (compiler/->ExtendedCompiler
     id
     fn-to-element
     parse-fn
     (clj->js project-libs)
     convert-props-in-vectors?)))
