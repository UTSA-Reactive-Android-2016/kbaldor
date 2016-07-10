(ns goldfish-email.users
  )

(def current-users (atom #{}))
(def friends-map (atom {}))
(def user-info-map (atom {}))
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
  [id friends-list]
  (swap! friends-map assoc id friends-list))

(defn sign-in
  [username]
  (swap! current-users conj username))

(defn sign-out
  [username]
  (swap! current-users disj username))
