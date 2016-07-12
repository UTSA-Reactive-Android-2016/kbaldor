(defproject goldfish-email "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler goldfish-email.core/handler
         ;:host "192.168.0.10"
         ;:host "129.162.167.152"
         :host "172.24.1.218"
         }
  :main goldfish-email.core
  :dependencies [[ring/ring-jetty-adapter "1.2.0"]
                 [org.clojure/clojure "1.8.0"]
                 [liberator "0.13"]
                 [compojure "1.3.4"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-ssl "0.2.1"]
                 [clj-crypto "1.0.2"]
                 [crypto-random "1.2.0"]
                 [lock-key "1.4.1"]
                 [org.clojure/data.json "0.2.6"]
                 [com.novemberain/pantomime "2.8.0"]])
