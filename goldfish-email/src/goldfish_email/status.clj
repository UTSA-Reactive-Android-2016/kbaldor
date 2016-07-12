(ns goldfish-email.status)

(def success
  {:status "ok"
   :reason "ok"})

(defn fail [reason]
  {:status "fail"
   :reason reason})