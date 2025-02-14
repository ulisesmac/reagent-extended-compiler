(ns reagent-extended-compiler.protocols)

(defprotocol ExtendedCompiler
  (js-component-libs [this])
  (get-component-from-lib [this tag])
  (convert-props-in-vectors [this]))
