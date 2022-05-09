;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.user-manager.cognito
  (:require
   [cognitect.aws.client.api :as aws]
   [dev.gethop.user-manager.cognito.api :as api]
   [integrant.core :as ig]))

(defn init-record
  "Initializes the Cognito record. Method for those not using Integrant
  or Duct. The configuration has a single required key which is the
  `:user-pool-id`. Apart from the `:user-pool-id`, `:client-config`
  can be optionally provided to customize the configuration of the
  `aws-api`. The configuration keys in `:client-config` are the same
  keys specified by the underlying library[1].

  [1] - https://cognitect-labs.github.io/aws-api/cognitect.aws.client.api-api.html"
  [{:keys [user-pool-id client-config] :as config}]
  (if-not (seq user-pool-id)
    (throw (ex-info ":user-pool-id can't be nil" {:config config}))
    (-> config
        (assoc :client (aws/client (merge client-config {:api :cognito-idp})))
        (api/map->AWSCognito))))

(defmethod ig/init-key :dev.gethop.user-manager/cognito
  [_ config]
  (init-record config))
