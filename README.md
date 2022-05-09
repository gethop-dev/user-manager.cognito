# user-manager.cognito

A Library for interacting with the [AWS Cognito User Pools API](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_Operations.html) which optionally provides [Integrant](https://github.com/weavejester/integrant) initialization keys for the [Duct](https://github.com/duct-framework/duct) framework.

## Table of Contents

* [Installation](#installation)
* [Usage](#usage)
  * [Configuration](#configuration)
    * [Using Duct](#using-duct)
    * [Not using Duct](#not-using-duct)
    * [Configuration example](#configuration-example)
  * [Obtaining an AWSCognito record](#obtaining-an-awscognito-record)
    * [Using Duct](#using-duct)
    * [Not using Duct](#not-using-duct)
  * [Creating an user](#creating-an-user)
  * [Enabling or disabling an user](#enabling-or-disabling-an-user)
  * [Getting an user](#getting-an-user)
  * [Deleting an user](#deleting-an-user)

## Installation

## Usage

### Configuration

#### Using Duct

To use this library add the following key to your configuration:

`:dev.gethop.user-manager/cognito`

#### Not using Duct

Just call:

``` clojure
(dev.gethop.user-manager.cognito/init-record <configuration>)
```

The configuration is is a map with one mandatory key:
* `:user-pool-id`: The Cognito User Pool ID to manage. If not
  provided, an exception is thrown.

Optional key:
* `:client-config`: Cognito client configuration. The library uses
  [`aws-api`](https://github.com/cognitect-labs/aws-api) underneath to call the AWS API. All the configuration
  described in the [`client`](https://cognitect-labs.github.io/aws-api/cognitect.aws.client.api-api.html#cognitect.aws.client.api/client) documentation can be specified in the
  map and will be passed untouched to the `aws-api/client`.

Key initialization returns a `AWSCognito` record that can be used to perform the Cognito operations described below.

#### Configuration example

Basic configuration:
```edn
  :dev.gethop.user-manager/cognito
  {:user-pool-id "eu-west-1_XXXXXX}
```

Configuration with custom client configuration:
```edn
  :dev.gethop.user-manager/cognito
  {:user-pool-id "eu-west-1_XXXXXX
   :client-config {:region "eu-west-1"}}
```

### Obtaining an `AWSCognito` record

#### Using Duct
If you are using the library as part of a [Duct](https://github.com/duct-framework/duct)-based project, adding any of the previous configurations to your `config.edn` file will perform all the steps necessary to initialize the key and return a `AWSCognito` record for the associated configuration. In order to show a few interactive usages of the library, we will do all the steps manually in the REPL.

First we require the relevant namespaces:

```clj
user> (require '[integrant.core :as ig])
nil
user>
```

Next we create the configuration var holding the AWSCognito integration configuration details:

```clj
user> (def config :dev.gethop.user-manager/cognito {:user-pool-id "eu-west-1_XXXXXX})
#'user/config
user>
```

Now that we have all pieces in place, we can initialize the `:dev.gethop.user-manager/cognito` Integrant key to get a `AWSCognito` record. As we are doing all this from the REPL, we have to manually require `dev.gethop.user-manager.cognito` namespace, where the `init-key` multimethod for that key is defined (this is not needed when Duct takes care of initializing the key as part of the application start up):

``` clj
user> (require '[dev.gethop.user-manager.cognito :as cognito])
nil
user>
```

And we finally initialize the key with the configuration defined above, to get our `AWSCognito` record:

``` clj
user> (def cognito-record (ig/init-key :dev.gethop.user-manager/cognito config))
#'user/ds-record
user> cognito-record
#dev.gethop.user-manager.cognito.api.AWSCognito{:client #object[cognitect.aws.client.Client 0x61df8be9 "cognitect.aws.client.Client@61df8be9"]
                                                :user-pool-id "eu-west-1_XXXXXX"}
user>
```

#### Not using Duct

```clj
user> (require '[dev.gethop.user-manager.cognito :as cognito])
user> (cognito/init-record {:user-pool-id "eu-west-1_XXXXXX"})

#dev.gethop.user-manager.cognito.api.AWSCognito{:client #object[cognitect.aws.client.Client 0x61df8be9 "cognitect.aws.client.Client@61df8be9"]
                                                :user-pool-id "eu-west-1_XXXXXX"}
```

Now that we have our `AWSCognito` record, we are ready to use the methods defined by the protocols defined in `dev.gethop.user-manager.core` namespace.

All the function calls returns a map regardless of the outcome. The returning map will always include the following key:
* `:success?`: boolean value indicating the success of the operation.

And it may include more keys that are specific to the method.

If the operation is not successful it will always return the following additional keys in the map:
* `:reason`: a string with the description about the error.
* `:error-details`: which is a map with the following keys:
 * `:type`: the exception type.
 * `:message`: same as `:reason`.
 * `:category`: a keyword indicating the error type defined by the underlying client library.

### Creating an user

To create an user there is only two mandatory parameters:
* `this`: the `AWSCognito` record.
* `username`: the user's username.

The `create-user` method also accepts a map of optional parameters:
* `:temporary-password`: the users temporary password which a string.
* `:validation-data`: map of user attributes and attribute values that can be used for validation.
* `:message-action`: a keyword. Message action can be either `:resend` or `:suppress`.
* `:desired-delivery-mediums`: a keyword. It can be either `:email` or `:sms`.
* `:client-metadata`: a map of custom key-value pairs.
* `:force-alias-creation`: a boolean value.
* `:standard-attributes`: map of key-value pairs to specify standard attributes known by AWSCognito.
* `:custom-attributes`: map of custom key-value pairs to define custom user attributes.

For more elaborate and complete description of all these parameters please refer to the [AWS Cognito documentation](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminCreateUser.html)

Example:

``` clj
user> (core/create-user cognito "foo@bar.barz" {:desired-delivery-mediums :email
                                                                   :standard-attributes {:email "foo@bar.barz"
                                                                                         :email-verified true
                                                                                         :name "foo"}})
{:user
 {:username "ff583035-f994-45cb-af2a-76fc28e5b4e5",
  :attributes
  {:sub "ff583035-f994-45cb-af2a-76fc28e5b4e5",
   :email-verified "true",
   :name "foo",
   :email "foo@bar.barz"},
  :user-create-date #inst "2022-05-09T09:14:23.000-00:00",
  :user-last-modified-date #inst "2022-05-09T09:14:23.000-00:00",
  :enabled true,
  :user-status :force-change-password,
  :id #uuid "ff583035-f994-45cb-af2a-76fc28e5b4e5",
  :mfa-options []},
 :success? true}
```

### Getting an user

To get an user it requires:
* `this`: the `AWSCognito` record.
* `username`: the user's username.

Example:

``` clj
user> (core/get-user r "foo@bar.barz")
{:success? true,
 :user
 {:user-status :force-change-password,
  :user-create-date #inst "2022-05-09T14:15:20.000-00:00",
  :enabled true,
  :user-attributes
  {:sub "8b376fc1-4cc6-45ed-b252-46603302540f",
   :email-verified "true",
   :name "foo",
   :email "foo@bar.barz"},
  :user-last-modified-date #inst "2022-05-09T14:15:20.000-00:00",
  :username "8b376fc1-4cc6-45ed-b252-46603302540f",
  :id #uuid "8b376fc1-4cc6-45ed-b252-46603302540f",
  :mfa-options []}}
```

### Enabling or disabling an user

Both operations accepts the same parameters:
* `this`: the `AWSCognito` record.
* `username`: the user's username.

Example:

``` clj
user> (core/disable-user cognito "foo@bar.barz")
{:success? true}
```

### Deleting an user

To delete an user it requires:
* `this`: the `AWSCognito` record.
* `username`: the user's username.

Example:

``` clj
user> (core/delete-user cognito "foo@bar.barz")
{:success? true}
```

## License

Copyright (c) 2022 HOP Technologies.

The source code for the library is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
