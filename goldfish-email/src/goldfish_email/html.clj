(ns goldfish-email.html
  (:require [goldfish-email.users :as users]))

(defn get-user-row
  [username]
  (format "<tr><td><img src=\"/present-images/%s.png\"/<td>%s</td><td style=\"text-align:center\"><img src=\"/user-images/%s.png\"/></td><td>%s</td></tr>"
          username username username
          (get-in @users/user-info-map [username :key64])))

(defn get-users-table
  [ctx]
  (format "<html><table>%s</table></html>" (clojure.string/join (map get-user-row (keys @users/user-info-map)))))

