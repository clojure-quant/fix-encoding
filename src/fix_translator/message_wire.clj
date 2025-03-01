(ns fix-translator.message-wire
  (:require
   [gloss.io :as io]
   [fix-translator.gloss :as ft])
  (:import [java.io StringWriter]))

(defn vec->wire
  "converts a fix-message from vector-format to wire-format (string)"
  [fix-msg-vec]
  (let [sw (StringWriter.)]
    (doseq [[tag value-str] fix-msg-vec]
      (.write sw tag)
      (.write sw "=")
      (.write sw value-str)
      (.write sw ""))
    (.toString sw)))

(defn wire->vec
  "converts a fix-message from wire-format (string) to vector-format"
  [fix-msg-wire]
  (io/decode-all ft/fix-protocol fix-msg-wire))
