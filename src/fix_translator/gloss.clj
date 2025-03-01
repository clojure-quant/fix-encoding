(ns fix-translator.gloss
  (:require
   [gloss.core :as gloss]))

(def fix-protocol
  (gloss/compile-frame
   [(gloss/string :ascii :delimiters ["="])
    (gloss/string :ascii :delimiters [""])]))

(defn xf-fix-message [rf]
  (let [acc (volatile! [])]
    (fn
      ([] (rf))
      ([result] (let [final-result (if (seq @acc)
                                     (conj result @acc)
                                     result)]
                  (rf final-result)))
      ([result input]
       (if (nil? input)
         result
         (do (vswap! acc conj input)
             (if (= "10" (first input))
               (let [to-emit @acc]
                 (vreset! acc []) ;; Reset accumulator
                 (rf result to-emit))
               result)))))))

(def header-tags #{"8" "9" "34" "49" "50" "56" "57"
                   ;"52" send time
                   "10"})

(defn header? [[t v]]
  (contains? header-tags t))

(defn without-header [tv]
  (->> tv
       (remove header?)
       (into [])))


;; below here not used, because we use gloss for tag value parsing.

;; tag=value pairs separated by \u0001 (SOH),  tag can be 1-2 alphanumeric characters
(def tag-value-regex #"([A-Za-z0-9]{1,4})=([^\u0001]+)")

;; Parse FIX message into list of tag-value maps
(defn ->tag-value-pairs [msg]
  (map (fn [[_ tag value]]
         {:tag tag
          :value value})
       (re-seq tag-value-regex msg)))
