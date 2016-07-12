(ns goldfish-email.crypto
  (:import (java.security.spec X509EncodedKeySpec)
           [java.security KeyFactory]
           (org.apache.commons.codec.binary StringUtils Base64)
           (javax.crypto Cipher KeyGenerator)
           (javax.crypto.spec SecretKeySpec IvParameterSpec))
  (:require [clj-crypto.core :as crypto]
            [crypto.random :as random]
            [lock-key.core :as aes-crypto]))

(def generate-key-pair crypto/generate-key-pair)

(defn generate-secret-key []
  (let [generator (KeyGenerator/getInstance "AES" "BC")]
    (.init generator 128)
    (.generateKey generator)))

; defaults to 2048
(def key-pair (crypto/generate-key-pair))

(def encoded-public-key-string
  (StringUtils/newStringUtf8
    (Base64/encodeBase64 (.getEncoded (X509EncodedKeySpec. (.getEncoded (.getPublic key-pair)))) true)))

(defn encode-public-key-string
  [key-pair]
  (StringUtils/newStringUtf8
    (Base64/encodeBase64 (.getEncoded (X509EncodedKeySpec. (.getEncoded (.getPublic key-pair)))) true)))


(defn decode-base64-rsa-public-key
  [key]
  (.generatePublic (KeyFactory/getInstance "RSA") (X509EncodedKeySpec. (crypto/decode-base64 key))))

(defn decode-aes-secret-key
  [key]
  (SecretKeySpec. key "AES"))

(defn decrypt-aes
  [key data]
  (let [[iv-bytes encrypted-data] (split-at 16 data)
        iv-bytes       (into-array Byte/TYPE iv-bytes)
        encrypted-data (into-array Byte/TYPE encrypted-data)
        cipher         (Cipher/getInstance "AES/CBC/PKCS5Padding" "BC")
        ]
    (.init cipher Cipher/DECRYPT_MODE key (new IvParameterSpec iv-bytes))
    (.doFinal cipher encrypted-data)))

(defn encrypt-aes
  [key data]
  (let [cipher         (Cipher/getInstance "AES/CBC/PKCS5Padding" "BC")]
    (.init cipher Cipher/ENCRYPT_MODE key)
    (let [cipher-bytes  (.doFinal cipher data)
          iv-bytes      (.getIV cipher)
          bytes         (byte-array (+ (count iv-bytes) (count cipher-bytes)))]
      (System/arraycopy iv-bytes     0 bytes 0                (count iv-bytes))
      (System/arraycopy cipher-bytes 0 bytes (count iv-bytes) (count cipher-bytes))
      bytes)))

(defn encrypt-aes-to-base64-str
  [key data]
  (Base64/encodeBase64String (encrypt-aes key data)))

(def encrypt crypto/encrypt)

(defn decrypt
  ([key data] (decrypt key data (crypto/create-cipher)))
  ([key data cipher]
   (crypto/do-cipher cipher Cipher/DECRYPT_MODE (.getPrivate key) data)))

(defn decrypt-base64-string
  [base64-cipher-text]
  (let [bytes (crypto/decode-base64 base64-cipher-text)]
    (decrypt key-pair bytes)))
