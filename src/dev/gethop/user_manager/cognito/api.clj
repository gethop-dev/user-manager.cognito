;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.user-manager.cognito.api
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [cognitect.aws.client.api :as aws]
   [dev.gethop.user-manager.core :as core]
   [dev.gethop.user-manager.util :as util]))

(s/fdef enable-or-disable-user
  :args ::core/enable-or-disable-user-args
  :ret ::core/enable-or-disable-user-ret)

(defn- enable-or-disable-user
  [{:keys [client user-pool-id] :as this} op username]
  {:pre [(s/valid? ::core/this this)
         (s/valid? ::core/username username)]}
  (let [result (util/->map-kebab-case
                (aws/invoke client {:op op
                                    :request {:UserPoolId user-pool-id
                                              :Username username}}))]
    (if-not (contains? result :category)
      (assoc result :success? true)
      {:success? false
       :reason (:message result)
       :error-details result})))

(s/fdef delete-user
  :args ::core/delete-user-args
  :ret ::core/delete-user-ret)

(defn- delete-user
  [{:keys [client user-pool-id] :as this} username]
  {:pre [(s/valid? ::core/this this)
         (s/valid? ::core/username username)]}
  (let [result (util/->map-kebab-case
                (aws/invoke client {:op :AdminDeleteUser
                                    :request {:UserPoolId user-pool-id
                                              :Username username}}))]
    (if-not (contains? result :category)
      (assoc result :success? true)
      {:success? false
       :reason (:message result)
       :error-details result})))

(defn- ->aws-custom-attributes
  [custom-attributes]
  (reduce-kv
   (fn [atts attribute-k attribute-v]
     (assoc atts (keyword (str "custom:" (name attribute-k))) attribute-v))
   {}
   custom-attributes))

(defn- ->aws-coll
  [m]
  (reduce (fn [acc [k v]]
            (conj acc {:name k
                       :value (str v)}))
          []
          (util/->map-snake-case-string m)))

(defn- parse-create-user-opts
  [{:keys [desired-delivery-mediums
           standard-attributes
           custom-attributes
           message-action
           validation-data] :as opts}]
  (cond-> opts
    (or (seq standard-attributes)
        (seq custom-attributes))
    (assoc :user-attributes (->> custom-attributes
                                 ->aws-custom-attributes
                                 (merge standard-attributes)
                                 ->aws-coll))

    desired-delivery-mediums
    (assoc :desired-delivery-mediums [((comp str/upper-case name) desired-delivery-mediums)])

    message-action
    ((comp str/upper-case name) message-action)

    (seq validation-data)
    (assoc :validation-data (->aws-coll validation-data))

    true
    (dissoc :standard-attributes :custom-attributes)))

(defn- find-user-id
  [attributes]
  (some (fn [{:keys [name value]}]
          (when (= name "sub")
            value))
        attributes))

(defn- aws-user->user
  [aws-user]
  (letfn [(parse-user-attributes [attributes]
            (reduce (fn [m {:keys [name value]}]
                      (assoc m (csk/->kebab-case-keyword name) value))
                    {}
                    attributes))
          (parse-mfa-options [mfa-options]
            (mapv #(update % :delivery-medium csk/->kebab-case-keyword) mfa-options))]
    (-> aws-user
        (update :id (fn [_] (util/uuid (find-user-id (or (:attributes aws-user)
                                                         (:user-attributes aws-user))))))
        (update :user-status csk/->kebab-case-keyword)
        (update :mfa-options parse-mfa-options)
        ;; AdminCreateUser uses the key Attributes for the user entity
        ;; and AdminGetUser uses UserAttributes.
        (util/update-if-exists :attributes parse-user-attributes)
        (util/update-if-exists :user-attributes parse-user-attributes))))

(s/fdef create-user
  :args ::core/create-user-args
  :ret ::core/create-user-ret)

(defn- create-user
  [{:keys [client user-pool-id] :as this} username opts]
  {:pre [(s/valid? ::core/this this)
         (s/valid? ::core/username username)
         (s/valid? ::core/create-user-opts opts)]}
  (let [request (-> {:user-pool-id user-pool-id
                     :username username}
                    (merge (parse-create-user-opts opts))
                    util/->map-pascal-case-keyword)
        result (util/->map-kebab-case
                (aws/invoke client {:op :AdminCreateUser
                                    :request request}))]
    (if-not (contains? result :category)
      (-> result
          (update :user aws-user->user)
          (assoc :success? true))
      {:success? false
       :reason (:message result)
       :error-details result})))

(defn- get-user
  [{:keys [client user-pool-id] :as this} username]
  (let [result (util/->map-kebab-case
                (aws/invoke client {:op :AdminGetUser
                                    :request {:UserPoolId user-pool-id
                                              :Username username}}))]
    (if-not (contains? result :category)
      {:success? true
       :user (aws-user->user result)}
      {:success? false
       :reason (:message result)
       :error-details result})))

(defrecord AWSCognito [client user-pool-id]
  core/UserManager
  (get-user [this username]
    (get-user this username))

  (create-user [this username]
    (create-user this username {}))
  (create-user [this username opts]
    (create-user this username opts))

  (delete-user [this username]
    (delete-user this username))

  (disable-user [this username]
    (enable-or-disable-user this :AdminDisableUser username))
  (enable-user [this username]
    (enable-or-disable-user this :AdminEnableUser username)))
