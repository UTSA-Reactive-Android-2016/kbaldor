(ns goldfish-email.crypto
  (:import (java.security.spec X509EncodedKeySpec)
           [java.security KeyFactory]
           (org.apache.commons.codec.binary StringUtils Base64)
           (javax.crypto Cipher))
  (:require [clj-crypto.core :as crypto]))

; defaults to 2048
(def key-pair (crypto/generate-key-pair))

(def encoded-public-key-string
  (StringUtils/newStringUtf8
    (Base64/encodeBase64 (.getEncoded (X509EncodedKeySpec. (.getEncoded (.getPublic key-pair)))) true)))

(defn decode-base64-rsa-public-key
  [key]
  (.generatePublic (KeyFactory/getInstance "RSA") (X509EncodedKeySpec. (crypto/decode-base64 key))))

(def encrypt crypto/encrypt)

(defn decrypt
  ([key data] (decrypt key data (crypto/create-cipher)))
  ([key data cipher]
   (crypto/do-cipher cipher Cipher/DECRYPT_MODE (.getPrivate key) data)))

(defn decrypt-base64-string
  [base64-cipher-text]
  (let [bytes (crypto/decode-base64 base64-cipher-text)]
    (decrypt key-pair bytes)))
