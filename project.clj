(defproject reagent-extended-compiler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljsbuild "1.1.8"]]
  :source-paths ["src"]
  :profiles {:dev {:dependencies [[cljsjs/react "18.3.1-1"]
                                  [camel-snake-kebab "0.4.3"]
                                  [org.clojure/clojure "1.11.1"]
                                  [org.clojure/clojurescript "1.11.132"]
                                  [reagent "1.2.0"]]}}
  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler     {:optimizations :whitespace
                                       :pretty-print  true}}]}
  :repl-options {:init-ns reagent-extended-compiler.core})
