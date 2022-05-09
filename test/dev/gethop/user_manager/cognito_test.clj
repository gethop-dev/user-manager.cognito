;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.user-manager.cognito-test
  (:require
   [clojure.test :refer :all]
   [dev.gethop.user-manager.cognito]
   [dev.gethop.user-manager.cognito.api]
   [dev.gethop.user-manager.test-util :as test-util]
   [integrant.core :as ig])
  (:import
   [dev.gethop.user_manager.cognito.api AWSCognito]))

(def ^:private test-config
  {:user-pool-id (System/getenv "TEST_USER_MANAGER_COGNITO_USER_POOL_ID")
   :client-config {:credentials-provider (test-util/assumed-role-credentials-provider)}})

(deftest protocol-test
  (testing "ig/init-key throws an exception if :user-pool-id is missing"
    (let [msg (try
                (ig/init-key :dev.gethop.user-manager/cognito {})
                (catch Exception e
                  (ex-message e)))]
      (is (= ":user-pool-id can't be nil" msg))))
  (testing "ig/init-key initializes AWSCognito record correctly"
    (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
      (is (= (class record) AWSCognito)))))
