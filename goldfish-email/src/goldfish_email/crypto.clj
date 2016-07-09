(ns goldfish-email.crypto
  (:import (java.security KeyPairGenerator Security)
           (java.security.spec RSAKeyGenParameterSpec X509EncodedKeySpec)
           [java.security Key KeyFactory KeyPair KeyPairGenerator MessageDigest PrivateKey PublicKey Security Signature KeyStore]
           (org.apache.commons.codec.binary StringUtils Base64)
           (javax.crypto Cipher))
  (:require [clj-crypto.core :as crypt]))

; defaults to 2048
(def key-pair (crypt/generate-key-pair))

(def public-key-string
  (crypt/encode-base64-as-str
    (.getEncoded (X509EncodedKeySpec. (.getEncoded  (.getPublic key-pair))))))

(def public-key-string
  (StringUtils/newStringUtf8
    (Base64/encodeBase64 (.getEncoded (X509EncodedKeySpec. (.getEncoded (.getPublic key-pair)))) true)))

(defn decode-base64-rsa-public-key
  [key]
  (.generatePublic (KeyFactory/getInstance "RSA") (X509EncodedKeySpec. (crypt/decode-base64 key))))

(defn do-cipher [cipher mode key data]
  (.init cipher mode key)
  (.doFinal cipher data))

(defn decrypt
  ([key data] (decrypt key data (crypt/create-cipher)))
  ([key data cipher]
   (do-cipher cipher Cipher/DECRYPT_MODE (.getPrivate key) data)))

(defn decrypt-base64-string
  [cipher-text]
  (let [decrypted (decrypt key-pair (crypt/decode-base64 cipher-text))]
    (println "newest code")
    (println "length decoded " (count decrypted))
    (crypt/encode-base64-as-str decrypted)))

 ;(.getBytes (crypt/decrypt key-pair (crypt/decode-base64 cipher-text))))

;(def cipher-text (crypto/encode-base64-as-str (crypto/encrypt key-pair (.getBytes "test-message"))))
;(printf "cipher-text %s\n" cipher-text)
;(printf "clear-text %s\n" (crypto/decrypt key-pair (crypto/decode-base64 cipher-text)))