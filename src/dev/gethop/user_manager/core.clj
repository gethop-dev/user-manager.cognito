;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.user-manager.core
  (:require
   [clojure.spec.alpha :as s]
   [dev.gethop.user-manager.util :as util]))

(s/def ::this record?)
(s/def ::type string?)
(s/def ::message string?)
(s/def ::category keyword?)
(s/def ::success? boolean?)

(s/def ::id uuid?)
(s/def ::username util/non-blank-string?)
(s/def ::temporary-password util/non-blank-string?)
(s/def ::validation-data (s/map-of keyword? string?))
(s/def ::message-action #{:resend :suppress})
(s/def ::desired-delivery-mediums #{:email :sms})
(s/def ::client-metadata (s/map-of string? string?))
(s/def ::force-alias-creation boolean?)

;; Take from: https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html
(s/def ::standard-attribute #{:address :birthdate :email :email-verified :family-name :gender
                              :given-name :locale :middle-name :name :nickname :phone-number
                              :picture :preferred-username :profile :updated-at :website :zoneinfo})

(s/def ::standard-attributes (s/map-of ::standard-attribute (s/or :string string?
                                                                  :number number?
                                                                  :boolean boolean?)))
(s/def ::custom-attributes (s/map-of keyword? string?))
(s/def ::attributes (s/or :standard-attributes ::standard-attributes
                          :custgom-attributes ::custom-attributes))
(s/def ::user-attributes ::attributes)
(s/def ::user-create-date inst?)
(s/def ::user-last-modified-date inst?)
(s/def ::enabled boolean?)
(s/def ::user-status #{:confirmed :unconfirmed :archived :compromised :unknown
                       :reset-password :force-change-password})
(s/def ::attribute-name string?)
(s/def ::mfa-option (s/keys :req-un [::desired-delivery-medium
                                     ::attribute-name]))
(s/def ::mfa-options (s/coll-of ::mfa-option :kind vector?))

(s/def ::user (s/keys :req-un [::id
                               ::username
                               (or ::attributes ::user-attributes)
                               ::user-create-date
                               ::user-last-modified-date
                               ::enabled
                               ::user-status
                               ::mfa-options]))

(s/def ::create-user-opts (s/keys :opt-un [::temporary-password
                                           ::validation-data
                                           ::message-action
                                           ::desired-delivery-mediums
                                           ::client-metadata
                                           ::force-alias-creation
                                           ::standard-attributes
                                           ::custom-attributes]))
(s/def ::create-user-args (s/cat :this ::this
                                 :username ::username
                                 :opts ::create-user-opts))
(s/def ::create-user-ret (s/keys :req-un [::success?]
                                 :opt-un [::user
                                          ::type
                                          ::message
                                          ::category]))

(s/def ::delete-user-args (s/cat :this ::this
                                 :username ::username))
(s/def ::delete-user-ret (s/keys :req-un [::success?]
                                 :opt-un [::type
                                          ::message
                                          ::category]))

(s/def ::enable-or-disable-user-args (s/cat :this ::this
                                            :username ::username))
(s/def ::enable-or-disable-user-ret (s/keys :req-un [::success?]
                                            :opt-un [::type
                                                     ::message
                                                     ::category]))

(defprotocol UserManager
  (create-user
    [this username]
    [this username opts])
  (delete-user [this username])
  (get-user [this username])
  (disable-user [this username])
  (enable-user [this username]))
