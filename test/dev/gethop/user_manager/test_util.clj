;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.user-manager.test-util
  (:require
   [cognitect.aws.client.api :as aws]
   [cognitect.aws.credentials :as credentials]))

(def ^:const ^:private test-role-arn (System/getenv "TEST_USER_MANAGER_ROLE_ARN"))

(defn assumed-role-credentials-provider
  "Creates a credentials provider assuming a specific AWS role."
  []
  (if (or (System/getenv "AWS_SECURITY_TOKEN")
          (System/getenv "AWS_SESSION_TOKEN"))
    ;; There is already a STS session ongoing. So we've probably used
    ;; AssumeRole already from outside the code (e.g., through
    ;; aws-vault). So don't do anything and continue using the current
    ;; AWS credentials.
    nil
    (let [sts (aws/client {:api :sts})]
      (credentials/cached-credentials-with-auto-refresh
       (reify credentials/CredentialsProvider
         (fetch [_]
           (when-let [creds (:Credentials
                             (aws/invoke sts
                                         {:op :AssumeRole
                                          :request {:RoleArn test-role-arn
                                                    :RoleSessionName (str (gensym "user_manager.cognito_tests"))}}))]
             {:aws/access-key-id     (:AccessKeyId creds)
              :aws/secret-access-key (:SecretAccessKey creds)
              :aws/session-token     (:SessionToken creds)
              ::credentials/ttl      (credentials/calculate-ttl creds)})))))))
