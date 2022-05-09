(disable-warning
 {:linter :unused-fn-args
  :for-macro 'clojure.core/fn
  :if-inside-macroexpansion-of #{'clojure.spec.alpha/map-of}})
