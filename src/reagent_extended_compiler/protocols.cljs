(ns reagent-extended-compiler.protocols)

(defprotocol ExtendedCompiler
  (get-component-from-lib [this tag])
  (convert-props-in-vectors? [this]))
