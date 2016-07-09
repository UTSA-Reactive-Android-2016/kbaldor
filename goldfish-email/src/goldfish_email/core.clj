(ns goldfish-email.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET PUT]]
            [goldfish-email.users :as users]
            [goldfish-email.crypto :as my-crypto]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [crypto.random]
            [ring.util.response :as response]
            [ring.adapter.jetty :refer :all]
            [clj-crypto.core :as crypto])
  (:import (java.net NetworkInterface)
           (org.apache.commons.codec.binary Base64))
  (:gen-class))

(defn register-handler
  [ctx]
  (let [body     (slurp (get-in ctx [:request :body]))
        data     (json/read-str body)
        username (get-in data ["username"])
        image    (get-in data ["image"])
        key64    (get-in data ["public-key"])
        key      (my-crypto/decode-base64-rsa-public-key key64)]
    (println "register-handler called")
    (println key)
    (users/register username image key64 key)
    {:username username}))

(defn get-image-response
  [id]
  (let [record (get-in @users/user-info-map [id]),
        image (crypto/decode-base64 (:image record))]
    (println "record " record)
    (println "base64 image " (:image record))
    (println "size of image " (count image))
    (-> (response/response (new java.io.ByteArrayInputStream image))
            (response/content-type "image/png")
            (response/header "Content-Length" (count image)))))

(defn get-user-row
  [id]
  (format "<tr><td>%s</td><td><img src=\"/images/%s.png\"/></td><td>%s</td></tr>" id id (get-in @users/user-info-map [id :key64])))

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
        base64-bytes (crypto/encode-base64-as-str bytes)
        user (@users/user-info-map username)]
    (if (nil? user)
      "user-not-found"
      (do
        (println base64-bytes)
        (println "setting challenge for " username)
        (users/set-challenge username base64-bytes)
        (crypto/encode-base64-as-str (crypto/encrypt (:key user) bytes))))))

(defn login-handler
  [ctx]
  (let [body     (slurp (get-in ctx [:request :body]))
        data     (json/read-str body)
        username  (get-in data ["username"])
        response (get-in data ["response"])
        decoded-response (my-crypto/decrypt-base64-string response)
        last-challenge (@users/user-challenge-map username)]
    (println "username: " username)
    (println "response       " response)
    (println "response length " (.length response))
    (println "last challenge " last-challenge)
    (println "decoded response " decoded-response)
    (println "match: " (= last-challenge decoded-response))
    (if (= last-challenge decoded-response)
      {:status "succcess"}
      {:status "fail"})
    ))

(defn get-public-key
  [ctx]
  (do (println "sending key " my-crypto/public-key-string)
      my-crypto/public-key-string))

(defroutes app
           (GET "/" [] get-users-table)
           (ANY "/api-version" [] "0.1.0")
           (GET "/get-challenge/:id" [id] (get-challenge id))
           (PUT "/login" [] (resource :available-media-types ["application/json"]
                                      :allowed-methods [:put]
                                      :handle-created login-handler
                                      ))
           (ANY "/register" []
             (resource :available-media-types ["application/json"]
                       :allowed-methods [:put]
                       :handle-created register-handler
                       ))
           (GET "/get-contact-info/:id" [id] (get-contact-info id))
           (GET "/get-key" [] (resource :available-media-types ["text/html"]
                                :handle-ok get-public-key) )
           (GET "/images/:id.png" [id] (get-image-response id))
           )

(def handler
  (-> app
      wrap-params))


;TODO: this could be modified to provide a list of candidate IP addresses rather than just picking the first
(def ip
  (let [ifc (NetworkInterface/getNetworkInterfaces)
        ifsq (enumeration-seq ifc)
        ifmp (map #(bean %) ifsq)
        ipsq (filter #(false? (% :loopback)) ifmp)
        ipa (map :interfaceAddresses ipsq)
        ipaf (nth ipa 0)
        ipafs (.split (str ipaf) " " )
        ips (first (nnext ipafs))]
    (str (second (.split ips "/")))))

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