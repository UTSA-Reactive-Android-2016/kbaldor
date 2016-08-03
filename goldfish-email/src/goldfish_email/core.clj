(ns goldfish-email.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET PUT]]
            [goldfish-email.users :as users]
            [goldfish-email.crypto :as my-crypto]
            [goldfish-email.network :as network]
            [goldfish-email.html :as html]
            [goldfish-email.status :as status]
            [goldfish-email.image-processing :as im-proc]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [crypto.random]
            [ring.util.response :as response]
            [ring.adapter.jetty :refer :all]
            [goldfish-email.notifications :as notifications])
  (:import (org.apache.commons.codec.binary Base64)
           [org.apache.commons.io IOUtils])
  (:gen-class))

(defn legal-username?
  [username]
  (re-matches #"^[a-zA-Z][a-zA-Z0-9\-]*$" username)
  )

(defn special-username?
  [username]
  (.contains #{"alice" "bob" "charlie" "cathy"} (.toLowerCase username))
  )

(defn register-handler
  [ctx]
  (try
    (let [body (slurp (get-in ctx [:request :body]))
         data (json/read-str body)
         username (get-in data ["username"])
         image (get-in data ["image"])
         image-bytes (Base64/decodeBase64 image)
          key64 (get-in data ["public-key"])
          key (my-crypto/decode-base64-rsa-public-key key64)]
      (println username " registering")
      (if (legal-username? username)
        (if (not (special-username? username))
          (if (im-proc/legal-png? image-bytes)
            (do
              (users/register username image key64 key)
              status/success)
            (status/fail (im-proc/png-issues image-bytes)))
          (status/fail (format "username %s is reserved" username)))
        (status/fail "username must begin with a letter and consist only of letters, numbers, and hyphens")))
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on register: %s" (.getMessage e)))))))

(defn register-friends-handler
  [ctx]
  (try
    (let [body (slurp (get-in ctx [:request :body]))
          json (json/read-str body)
          username (get-in json ["username"])
          friends (get-in json ["friends"])]
      ;(println "body" body)
      ;(println username "has friends" friends)
     (users/set-friends username friends)
     (assoc status/success
            :friend-status-map
            (users/get-status-map friends)))
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on register-friends: %s" (.getMessage e)))))))

(defn add-friend-handler
  [ctx]
  (try
    (let [body (slurp (get-in ctx [:request :body]))
          json (json/read-str body)
          username (get-in json ["username"])
          friend (get-in json ["friend"])]
      ;(println "body" body)
      ;(println username "has friends" friends)
      (users/add-friend username friend)
      (assoc status/success
        :friend-status-map
        (users/get-status-map #{friend})))
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on register-friends: %s" (.getMessage e)))))))

(defn remove-friend-handler
  [ctx]
  (try
    (let [body (slurp (get-in ctx [:request :body]))
          json (json/read-str body)
          username (get-in json ["username"])
          friend (get-in json ["friend"])]
      ;(println "body" body)
      ;(println username "has friends" friends)
      (users/remove-friend username friend)
      status/success)
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on register-friends: %s" (.getMessage e)))))))

(defn get-contact-info
  [username]
  (try
    (println "getting contact info for" username)
    (if (contains? @users/user-info-map username)
      {:status "ok"
       :username (get-in @users/user-info-map [username :username])
       :image (get-in @users/user-info-map [username :image])
       :key (get-in @users/user-info-map [username :key64])
       }
      (status/fail "user not found"))
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on get-contact-info: %s" (.getMessage e)))))))

(defn get-challenge
  [username]
  (try
    (let [user (@users/user-info-map username)]
     (if (nil? user)
       "user-not-registered"
       (try
         (let [bytes (crypto.random/bytes 64)
               encrypted (my-crypto/encrypt (:key user) bytes)]
           (users/set-challenge username bytes)
           (Base64/encodeBase64String encrypted))
         (catch Exception e
           (println "caught exception" e)
           (status/fail (.getMessage e))))))
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on get-challenge: %s" (.getMessage e)))))))

(defn login-handler
  [ctx]
  (try
    (let [body (slurp (get-in ctx [:request :body]))
         data (json/read-str body)
         username (get-in data ["username"])
         response (get-in data ["response"])
         decoded-response (my-crypto/decrypt-base64-string response)
         last-challenge (users/get-last-challenge username)]
     (if (= (seq last-challenge) (seq decoded-response))
       (do
         (users/log-in username)
         status/success)
       (status/fail "failed challenge")))
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on login: %s" (.getMessage e)))))))

(defn logout-handler
  [ctx]
  (try
    (let [body (slurp (get-in ctx [:request :body]))
         data (json/read-str body)
         username (get-in data ["username"])
         response (get-in data ["response"])
         decoded-response (my-crypto/decrypt-base64-string response)
         last-challenge (users/get-last-challenge username)]
     (if (= (seq last-challenge) (seq decoded-response))
       (do
         (users/log-out username)
         status/success)
       (status/fail "failed challenge")))
    (catch Exception e
      (do
        (println "caught exception " e)
        (status/fail (format "Caught exception on logout: %s" (.getMessage e)))))))

(defn get-user-image
  [id]
  (let [record (get-in @users/user-info-map [id]),
        image  (Base64/decodeBase64 (:image record))]
    (-> (response/response (new java.io.ByteArrayInputStream image))
        (response/content-type "image/png")
        (response/header "Content-Length" (count image)))))

(defn get-present-image
  [username]
  (let [status   (if (contains? @users/current-users username) "present" "not_present")
        filename (format "%s.png" status)
        image    (IOUtils/toByteArray (io/input-stream (io/resource filename)))]
    (-> (response/response (new java.io.ByteArrayInputStream image))
        (response/content-type "image/png")
        (response/header "Content-Length" (count image)))))

(defn wait-for-push
  [username]
  (notifications/get-notifications username))

(defn send-message-handler
  [ctx]
  (let [recipient (get-in ctx [:request :params :recipient])
        body      (slurp (get-in ctx [:request :body]))
        json      (json/read-str body)]
    (dosync
      (if (not (users/is-logged-in recipient))
        (status/fail "user-unavailable")
        ;else
        (if (contains? users/fake-users recipient)
          (do
           (users/handle-fake-email recipient json)
           status/success)
          (notifications/deliver-message recipient body))))))

(defn remove-user
  [username]
  (swap! users/user-info-map dissoc username))

(defmacro json-put-resource
  [handler]
  (list resource
        :available-media-types ["application/json"]
        :allowed-methods [:put]
        :handle-created handler))

(defmacro json-get-resource
  [handler]
  (list resource
        :available-media-types ["application/json"]
        :allowed-methods [:get]
        :handle-ok handler))

(defroutes app
           (GET "/" [] html/get-users-table)
           (ANY "/api-version" [] "0.4.0")
           (GET "/get-challenge/:username"      [username]  (get-challenge username))
           (PUT "/login"                        []          (json-put-resource login-handler))
           (PUT "/logout"                       []          (json-put-resource logout-handler))
           (PUT "/register"                     []          (json-put-resource register-handler))
           (PUT "/register-friends"             []          (json-put-resource register-friends-handler))
           (PUT "/add-friend"                   []          (json-put-resource add-friend-handler))
           (PUT "/remove-friend"                []          (json-put-resource remove-friend-handler))
           (PUT "/send-message/:recipient"      []          (json-put-resource send-message-handler))
           (GET "/get-contact-info/:username"   [username]  (json-get-resource (get-contact-info username)))
           (GET "/get-key"                      []          my-crypto/encoded-public-key-string)
           (GET "/wait-for-push/:username"      [username]  (json-get-resource (wait-for-push username)))
           (GET "/user-images/:username.png"    [username]  (get-user-image username))
           (GET "/present-images/:username.png" [username]  (get-present-image username))
           (GET "/remove-user/:username"        [username]  (remove-user username))
           )

(def handler
  (-> app
      wrap-params))


(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    (read-string s)))

(defn -main [& args]
  (def host (if (first args) (first args) (first network/ips)))
  (def port (if (and (second args) (parse-number (second args)))
              (parse-number (second args)) 3000))
  (users/start-fake-users)
  (run-jetty handler {:host host :port port}))
