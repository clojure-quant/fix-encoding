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
       (vswap! acc conj input)
       (if (= "10" (first input))
         (let [to-emit @acc]
           (vreset! acc []) ;; Reset accumulator
           (rf result to-emit))
         result)))))

(def header-tags #{"8" "9" "34" "49" "50" "56" "57"
                   ;"52" send time
                   "10"})

(defn header? [[t v]]
  (contains? header-tags t))

(defn without-header [tv]
  (->> tv
       (remove header?)
       (into [])))



