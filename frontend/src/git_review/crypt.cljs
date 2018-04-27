(ns git-review.crypt
  (:require [goog.crypt :as crypt]
            [goog.crypt.Md5 :as Md5]))

(defn md5 [s]
  (let [bytes (crypt/stringToUtf8ByteArray s)
        hasher (goog.crypt.Md5.)]
    (.update hasher bytes)
    (crypt/byteArrayToHex
      (.digest hasher))))
