(ns goldfish-email.notifications
  (:require [goldfish-email.status :as status]))

(def message-listeners (ref {}))

(defn add-user
  [username]
  (alter message-listeners assoc username {:promise nil
                                           :pending []}))

(defn remove-user
  [username]
  (alter message-listeners dissoc username))

(defn message?
  [notification]
  (= (:type notification) "message"))

(defn not-user-log-in-out?
  [username]
  (fn [notification]
    (not (and (not (message? notification))
              (= (:username notification) username)))))


(defn add-notification
  [notifications notification]
  (if (message? notification)
    (if (< (count notifications) 100) (conj notifications notification))
    (conj (filter (not-user-log-in-out? (:username notification)) notifications) notification)))

(defn deliver-notification
  [username notification]
  (dosync
    (let [listener (message-listeners username)
          promise (:promise listener)
          pending (:pending listener)]
      (if (nil? listener)
        (status/fail "listener was nil")
        (if (nil? promise)
          (alter message-listeners assoc username {:promise nil
                                                   :pending (add-notification pending notification)}))))))

(defn deliver-message
  [username message]
  (deliver-notification username {:type    "message"
                                  :content message}))

;; TODO: handle promise-based delivery
(defn get-notifications
  [username]
  (dosync
    (let [listener (message-listeners username)
          promise (:promise listener)
          pending (:pending listener)]
      (alter message-listeners assoc username {:promise nil
                                               :pending []})
      ;(println "found pending" pending)
      {:notifications (vec pending)})))


;(add-watch message-listeners nil
;           (fn [key ref old new]
;             (println "new message-listeners" new)))