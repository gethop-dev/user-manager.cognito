;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.user-manager.util
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [clojure.string :as str])
  (:import
   [java.util UUID]))

(defn uuid
  "If no argument is passed, creates a random UUID. If the passed
  paramenter is a UUID, returns it verbatim. If it is a string
  representing a UUID value return the corresponding UUID. Any other
  value or invalid string returns nil. "
  ([]
   (UUID/randomUUID))
  ([uuid]
   (try
     (cond
       (uuid? uuid)
       uuid

       (string? uuid)
       (UUID/fromString uuid))
     (catch Exception _
       nil))))

(defn ->map-kebab-case
  "Transforms all map keys to kebab-case keywords."
  [m]
  (cske/transform-keys csk/->kebab-case-keyword m))

(defn ->map-snake-case-string
  "Transform all map keys to snake_case strings."
  [m]
  (cske/transform-keys csk/->snake_case_string m))

(defn ->map-pascal-case-keyword
  "Transform all map keys to PascalCase keywords"
  [m]
  (cske/transform-keys csk/->PascalCaseKeyword m))

(defn non-blank-string?
  "Predicate to check whether the string `s` is a `java.lang.String` and
  if it's not a blank string."
  [s]
  (and (string? s)
       (not (str/blank? s))))

(defn update-if-exists
  "Update the map `m` key `k` applying the function `update-fn` and
  `args` if the key's value is truthy. Otherwise, returns the map
  unchanged."
  [m k update-fn & args]
  (if (get m k)
    (apply update m k update-fn args)
    m))
