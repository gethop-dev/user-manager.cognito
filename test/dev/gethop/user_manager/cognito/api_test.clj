;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.user-manager.cognito.api-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [dev.gethop.user-manager.cognito]
   [dev.gethop.user-manager.core :as core]
   [dev.gethop.user-manager.test-util :as test-util]
   [integrant.core :as ig]))

(defonce ^:private credentials-provider (test-util/assumed-role-credentials-provider))

(def ^:private test-config
  {:user-pool-id (System/getenv "TEST_USER_MANAGER_COGNITO_USER_POOL_ID")
   :client-config {:credentials-provider credentials-provider}})

(def ^:const ^:private test-username "test@foo.com")

(defn- create-test-user
  []
  (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
    (core/create-user record test-username {:standard-attributes {:email test-username
                                                                  :email-verified true}})))

(defn- delete-test-user
  []
  (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
    (core/delete-user record test-username)))

(defn- with-test-user
  [f]
  (create-test-user)
  (f)
  (delete-test-user))

(use-fixtures :each with-test-user)

(deftest ^:integration create-user-test
  (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
    (testing "Creating an user successfully"
      (let [result (core/create-user record "test+1@foo.com" {:standard-attributes {:email "test+1@foo.com"
                                                                                    :email-verified true}})]
        (is (:success? result))
        (is (s/valid? ::core/user (:user result)))))
    (core/delete-user record "test+1@foo.com")))

(deftest ^:integration get-user-test
  (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
    (testing "Getting an user successfully"
      (let [result (core/get-user record test-username)]
        (is (:success? result))
        (is (s/valid? ::core/user (:user result)))))
    (testing "Fail if the username doesn't exist"
      (let [{:keys [success? error-details]} (core/get-user record "foo@bar.com")]
        (is (not success?))
        (is (= (:type error-details) "UserNotFoundException"))
        (is (= (:category error-details) :cognitect.anomalies/incorrect))))))

(deftest ^:integration disable-user-test
  (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
    (testing "Disable user"
      (let [result (core/disable-user record test-username)]
        (is (:success? result))
        (is (not (get-in (core/get-user record test-username) [:user :enabled])))))))

(deftest ^:integration enable-user-test
  (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
    (testing "Enable user"
      (let [result (core/enable-user record test-username)]
        (is (:success? result))
        (is (get-in (core/get-user record test-username) [:user :enabled]))))))

(deftest ^:integration delete-user-test
  (let [record (ig/init-key :dev.gethop.user-manager/cognito test-config)]
    (testing "Deleting an user successfully"
      (let [result (core/delete-user record test-username)]
        (is (:success? result))))))
