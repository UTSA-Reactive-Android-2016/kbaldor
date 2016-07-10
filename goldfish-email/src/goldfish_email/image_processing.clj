(ns goldfish-email.image-processing
  (:require [pantomime.mime :as mime]
            [pantomime.extract :as extract]
            [clojure.core :refer :all]))

(defn safe-integer-parse
  [integer-string]
  (if (nil? integer-string)
    nil
    (Integer/parseInt integer-string)))

(defn legal-png?
  [image-bytes]
  (let [type   (mime/mime-type-of image-bytes)
        info   (extract/parse image-bytes)
        height (safe-integer-parse (first (info :height)))
        width  (safe-integer-parse (first (info :width)))]
    (and (= type "image/png")
         (<= height 512)
         (<= width  512))))

(defn png-issues
  [image-bytes]
  (let [type   (mime/mime-type-of image-bytes)
        info   (extract/parse image-bytes)
        height (safe-integer-parse (first (info :height)))
        width  (safe-integer-parse (first (info :width)))]
    (if (and (= type "image/png")
             (<= height 512)
             (<= width 512))
      "ok"
      "Image must be PNG with width and height less than 512 pixels")))
