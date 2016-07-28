(ns goldfish-email.users
  (:require [clojure.java.io :as io]
            [goldfish-email.crypto :as my-crypto]
            [goldfish-email.notifications :as notifications]
            [goldfish-email.status :as status])
  (:import [org.apache.commons.codec.binary Base64]
           [org.apache.commons.io IOUtils]))

(def current-users      (ref #{}))
(def friends-map        (ref {}))
(def friends-inv-map    (ref {}))
(def user-info-map      (atom {}))
(def user-challenge-map (atom {}))

(defn register
  [username image key64 key]
  (swap! user-info-map assoc username {:username username
                                       :image image,
                                       :key64 key64,
                                       :key key}))

(defn set-challenge
  [username challenge]
  (swap! user-challenge-map assoc username challenge))

(defn get-last-challenge
  [username]
  (let [last-challenge (@user-challenge-map username)]
    (swap! user-challenge-map dissoc username)
    last-challenge
    ))

(defn set-friends
  [username friends]
  (dosync
    (alter friends-map assoc username friends)
    (doseq [friend friends]
      (alter friends-inv-map assoc friend (set (conj (friends-inv-map friend) username))))))

(defn get-login-status
  [friend]
  (if (contains? @current-users friend)
    "logged-in"
    "logged-out"))

(defn get-status-map
  [friends]
  (zipmap friends (map get-login-status friends)))

(defn remove-from-friends
  [username]
  (dosync
    (doseq [friend (friends-map username)]
      (alter friends-inv-map assoc friend (disj (friends-inv-map friend) username)))
    (alter friends-map dissoc username)))

(defn log-in
  [username]
  (dosync
    (alter current-users conj username)
    (notifications/add-user username)
    (doseq [listener (friends-inv-map username)]
      ;(println "found interested friend" listener)
      (notifications/deliver-notification listener {:type     "login"
                                                    :username username}))))

(defn log-out
  [username]
  ; remove self from current users first
  ; to reduce the chance of collisions
  (dosync
    (alter current-users disj username))
  (dosync
    (notifications/remove-user username)
    (remove-from-friends username)
    (doseq [listener (friends-inv-map username)]
      (notifications/deliver-notification listener {:type      "logout"
                                                    :username  username}))))

(defn is-logged-in
  [username]
  (contains? @current-users username))


(def fake-users (set (list "alice" "bob" "charlie" "cathy")))

(def fake-user-keys (atom {}))

(let [username "alice"
      key-pair (my-crypto/generate-key-pair)
      public-key (.getPublic key-pair)
      public-key64 (my-crypto/encode-public-key-string key-pair)
      image        (Base64/encodeBase64String (IOUtils/toByteArray (io/input-stream (io/resource "alice.png"))))
      ]
  (swap! fake-user-keys assoc username key-pair)
  (register username image public-key64 public-key))

(let [username "bob"
      key-pair (my-crypto/generate-key-pair)
      public-key (.getPublic key-pair)
      public-key64 (my-crypto/encode-public-key-string key-pair)
      image        (Base64/encodeBase64String (IOUtils/toByteArray (io/input-stream (io/resource "bob.png"))))
      ]
  (swap! fake-user-keys assoc username key-pair)
  (register username image public-key64 public-key))

(let [username "charlie"
      key-pair (my-crypto/generate-key-pair)
      public-key (.getPublic key-pair)
      public-key64 (my-crypto/encode-public-key-string key-pair)
      image        (Base64/encodeBase64String (IOUtils/toByteArray (io/input-stream (io/resource "charlie.png"))))
      ]
  (swap! fake-user-keys assoc username key-pair)
  (register username image public-key64 public-key))

(let [username "cathy"
      key-pair (my-crypto/generate-key-pair)
      public-key (.getPublic key-pair)
      public-key64 (my-crypto/encode-public-key-string key-pair)
      image        (Base64/encodeBase64String (IOUtils/toByteArray (io/input-stream (io/resource "cathy.png"))))
      ]
  (swap! fake-user-keys assoc username key-pair)
  (register username image public-key64 public-key))


(log-in "alice")
(log-in "cathy")

(add-watch current-users nil
           (fn [key ref old new]
             (let [old (disj old "bob")
                   new (disj new "bob")]
               (let [logins (filter #(not (contains? old %)) new)
                     logouts (filter #(not (contains? new %)) old)]
                 (doseq [name logins] (println name "logged in"))
                 (doseq [name logouts] (println name "logged out"))))))

;(add-watch friends-map nil
;           (fn [key ref old new]
;             (println "new friends-map" new)))

;(add-watch friends-inv-map nil
;           (fn [key ref old new]
;             (println "new friends-inv-map" new)))


(defn send-fake-email
  [recipient sender subject body ttl]
  (let [aes-key       (my-crypto/generate-secret-key)
        public-key    (get-in @user-info-map [recipient :key])]
    (if (not (nil? public-key))
      (let [
           enc-aes-key (Base64/encodeBase64String (my-crypto/encrypt public-key (.getEncoded aes-key)))
           enc-sender (my-crypto/encrypt-aes-to-base64-str aes-key (.getBytes sender "UTF-8"))
           enc-recipient (my-crypto/encrypt-aes-to-base64-str aes-key (.getBytes recipient "UTF-8"))
           enc-subject (my-crypto/encrypt-aes-to-base64-str aes-key (.getBytes subject "UTF-8"))
           enc-body (my-crypto/encrypt-aes-to-base64-str aes-key (.getBytes body "UTF-8"))
           enc-born (my-crypto/encrypt-aes-to-base64-str aes-key (.getBytes (.toString (System/currentTimeMillis)) "UTF-8"))
           enc-ttl (my-crypto/encrypt-aes-to-base64-str aes-key (.getBytes (.toString ttl) "UTF-8"))
           message {:aes-key      enc-aes-key
                    :sender       enc-sender
                    :recipient    enc-recipient
                    :subject-line enc-subject
                    :body         enc-body
                    :born-on-date enc-born
                    :time-to-live enc-ttl}]
       (notifications/deliver-message recipient message))
      (println "unable to send response to" recipient))))

(defn handle-fake-email
  [recipient json]
  (let [key-pair (@fake-user-keys recipient)
        aes-key  (get-in json ["aes-key"])
        aes-key  (Base64/decodeBase64 aes-key)
        aes-key  (my-crypto/decrypt key-pair aes-key)
        aes-key  (my-crypto/decode-aes-secret-key aes-key)
        body     (get-in json ["body"])
        body     (Base64/decodeBase64 body)
        body     (my-crypto/decrypt-aes aes-key body)
        body     (String. body)
        sender   (get-in json ["sender"])
        sender   (Base64/decodeBase64 sender)
        sender   (my-crypto/decrypt-aes aes-key sender)
        sender   (String. sender)
        ]
    (println recipient "got a message")
    ;(println (format "got body '%s'" (String. (my-crypto/decrypt-aes aes-key body) "UTF-8")))
    (println (format "got body '%s'" body))
    (println (format "from sender '%s'" sender))
    (send-fake-email sender recipient
                     "Re: your message"
                     (format "What exactly do you mean by '%s'?" body)
                     15000)
    status/success))

(defn send-chatty-emails [recipient]
  (send-fake-email recipient "cathy" "15-second message" "This message will live for fifteen seconds" 15000)
  (send-fake-email recipient "cathy" "30-second message" "This message will live for thirty seconds" 30000)
  (send-fake-email recipient "cathy" "60-second message" "This message will live for one minute" 60000))

(defn start-fake-users []
  (future
    (while true
      (do
        (Thread/sleep 5000)
        (log-in "bob")
        (Thread/sleep 5000)
        (log-out "bob"))))
  (future
    (while true
      (do
        (Thread/sleep 15000)
        (println "Cathy sending messages")
        (try
          (doseq [friend (dosync (clojure.set/intersection (@friends-inv-map "cathy") @current-users))]
           (println "Cathy sending to " friend)
           (send-chatty-emails friend))
          (catch Exception e
            (println "Something went wrong :( " e))))))

  ;(future
  ;  (while true
  ;    (do
  ;      (Thread/sleep 7000)
  ;      (log-in "alice")
  ;      (Thread/sleep 7000)
  ;      (log-out "alice"))))
  )

(start-fake-users)
