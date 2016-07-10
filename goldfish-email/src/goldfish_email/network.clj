(ns goldfish-email.network
  (:import (java.net NetworkInterface)))

(def ips
  (as-> (NetworkInterface/getNetworkInterfaces) x
        (enumeration-seq x)
        (map #(bean %) x)
        (filter #(false? (% :loopback)) x)
        (map :interfaceAddresses x)
        (map #(.split (str %) " ") x)
        (map #(first (nnext %)) x)
        (map #(str (second (.split % "/"))) x)
    ))

(defn print-ips []
  (do
    (println "available IP addresses:")
    (doseq [ip ips]
      (println "    " ip))))

(print-ips)