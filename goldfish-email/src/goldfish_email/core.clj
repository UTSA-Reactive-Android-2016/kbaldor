(ns goldfish-email.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET PUT]]
            [goldfish-email.users :as users]
            [goldfish-email.crypto :as my-crypto]
            [goldfish-email.network :as network]
            [goldfish-email.image-processing :as im-proc]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [crypto.random]
            [ring.util.response :as response]
            [ring.adapter.jetty :refer :all])
  (:import (org.apache.commons.codec.binary Base64)
           [org.apache.commons.io IOUtils])
  (:gen-class))

(defn register-handler
  [ctx]
  (let [body        (slurp (get-in ctx [:request :body]))
        data        (json/read-str body)
        username    (get-in data ["username"])
        image       (get-in data ["image"])
        image-bytes (Base64/decodeBase64 image)
        key64       (get-in data ["public-key"])
        key         (my-crypto/decode-base64-rsa-public-key key64)]
    (if (im-proc/legal-png? image-bytes)
      (do
        (users/register username image key64 key)
        (println (format "user %s registered" username))
        {:status "ok" :reason "ok"})
      {:status "fail" :reason (im-proc/png-issues image-bytes)}
      )))

(defn get-image-response
  [id]
  (let [record (get-in @users/user-info-map [id]),
        image (Base64/decodeBase64 (:image record))]
    (println "record " record)
    (println "base64 image " (:image record))
    (println "size of image " (count image))
    (-> (response/response (new java.io.ByteArrayInputStream image))
            (response/content-type "image/png")
            (response/header "Content-Length" (count image)))))

(defn get-present-image
  [username]
  (let [status (if (contains? @users/current-users username) "present" "not_present")
        filename (format "%s.png" status)
        image (IOUtils/toByteArray (io/input-stream (io/resource filename)))]
    (-> (response/response (new java.io.ByteArrayInputStream image))
        (response/content-type "image/png")
        (response/header "Content-Length" (count image)))))


(defn get-user-row
  [username]
  (format "<tr><td><img src=\"/present-images/%s.png\"/<td>%s</td><td><img src=\"/images/%s.png\"/></td><td>%s</td></tr>"
          username username username
          (get-in @users/user-info-map [username :key64])))

(defn get-users-table
  [ctx]
  (format "<html><table>%s</table></html>" (clojure.string/join (map get-user-row (keys @users/user-info-map)))))

(defn get-contact-info
  [id]
  {:username (get-in @users/user-info-map [id :username])
   :image (get-in @users/user-info-map [id :image])
   :key (get-in @users/user-info-map [id :key64])
   })

(defn get-challenge
  [username]
  (let [bytes (crypto.random/bytes 64)
        user (@users/user-info-map username)
        user-key64 (:key64 user)
        user-key (:key user)
        none (println "encoding for user " username)
        none (println "encoding with key " user-key64)
        encrypted (my-crypto/encrypt user-key bytes)]
    (if (nil? user)
      "user-not-registered"
      (do
        (users/set-challenge username bytes)
        (Base64/encodeBase64String encrypted)))))

(defn login-handler
  [ctx]
  (let [body             (slurp (get-in ctx [:request :body]))
        data             (json/read-str body)
        username         (get-in data ["username"])
        response         (get-in data ["response"])
        decoded-response (my-crypto/decrypt-base64-string response)
        last-challenge   (users/get-last-challenge username)]
    (if (= (seq last-challenge) (seq decoded-response))
      (do
        (users/sign-in username)
        {:status "succcess"})
      {:status "fail"})))

(defn logout-handler
  [ctx]
  (let [body             (slurp (get-in ctx [:request :body]))
        data             (json/read-str body)
        username         (get-in data ["username"])
        response         (get-in data ["response"])
        decoded-response (my-crypto/decrypt-base64-string response)
        last-challenge   (users/get-last-challenge username)]
    (if (= (seq last-challenge) (seq decoded-response))
      (do
        (users/sign-out username)
        {:status "succcess"})
      {:status "fail"})))

(defroutes app
           (GET "/" [] get-users-table)
           (ANY "/api-version" [] "0.1.0")
           (GET "/get-challenge/:username" [username] (get-challenge username))
           (PUT "/login" [] (resource :available-media-types ["application/json"]
                                      :allowed-methods [:put]
                                      :handle-created login-handler
                                      ))
           (PUT "/logout" [] (resource :available-media-types ["application/json"]
                                      :allowed-methods [:put]
                                      :handle-created logout-handler
                                      ))
           (ANY "/register" []
             (resource :available-media-types ["application/json"]
                       :allowed-methods [:put]
                       :handle-created register-handler
                       ))
           (GET "/get-contact-info/:username" [username] (get-contact-info username))
           (GET "/get-key" [] (resource :available-media-types ["text/html"]
                                        :handle-ok my-crypto/encoded-public-key-string))
           (GET "/images/:username.png" [username] (get-image-response username))
           (GET "/present-images/:username.png" [username] (get-present-image username))
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
  (def host (if (first args) (first args) "127.0.0.1"))
  (def port (if (and (second args) (parse-number (second args)))
              (parse-number (second args)) 3000))
  (run-jetty handler {:host host :port port}))