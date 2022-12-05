(defproject dev.gethop/user-manager.cognito "0.1.1-SNAPSHOT"
  :description "A library for interacting with the AWS Cognito User Pools API"
  :url "https://github.com/gethop-dev/user-manager.cognito"
  :license {:name "Mozilla Public Licence 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :min-lein-version "2.9.8"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.cognitect.aws/api "0.8.620"]
                 [com.cognitect.aws/endpoints "1.1.12.353"]
                 [com.cognitect.aws/cognito-idp "825.2.1250.0"]
                 [camel-snake-kebab "0.4.3"]
                 [integrant "0.8.0"]]
  :repl-options {:init-ns dev.gethop.user-manager.cognito}
  :deploy-repositories [["snapshots" {:sign-releases false
                                      :url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD}]
                        ["releases" {:sign-releases false
                                     :url "https://clojars.org/repo"
                                     :username :env/CLOJARS_USERNAME
                                     :password :env/CLOJARS_PASSWORD}]]
  :profiles
  {:dev [:project/dev :profiles/dev]
   :repl {:repl-options {:host "0.0.0.0"
                         :port 4001}}
   :profiles/dev {}
   :project/dev {:plugins [[jonase/eastwood "1.3.0"]
                           [lein-cljfmt "0.9.0"]]
                 :dependencies [[com.cognitect.aws/sts "825.2.1262.0"]]
                 :eastwood {:linters [:all]
                            :source-paths ["src"]
                            :test-paths ["test"]
                            :config-files ["eastwood_config.clj"]
                            :exclude-linters [:keyword-typos
                                              :boxed-math
                                              :non-clojure-file
                                              :unused-namespaces
                                              :performance]
                            :debug [:progress :time]}}})
