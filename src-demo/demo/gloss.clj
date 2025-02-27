(ns demo.gloss
  (:require [gloss.core :as gloss]
            [gloss.io :as io]))

(def fix-field
  (gloss/compile-frame
   [(gloss/string :ascii :delimiters ["="])
    (gloss/string :ascii :delimiters ["|"])]))


; working
;(io/decode fix-field "8=4|")
(io/decode-all fix-field "8=5|T=abc|A=4|")

; bytes left over
;(io/decode fix-field "8=5|T=abc|A=4|")
;(io/decode-all fix-field "8=5|T=abc|A=4|f")

(->> [["8" "FIX.4.4"]
      ["9" "93"]
      ["35" "0"]
      ["49" "demo.tradeviewmarkets.3193335"]
      ["56" "cServer"]
      ["34" "2"]
      ["50" "QUOTE"]
      ["57" "QUOTE"]
      ["52" "20250227-03:10:05"]
      ["10" "121"]]
(io/encode-all fix-field)
(map .toString)     
 )



;(->> (io/encode fix-field ["8=5|T=abc|A=4|"])
    ;(take 2)
    ;)

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

;; Example usage
;(transduce xf-fix-message 
;           conj [] 
;           [["B" 2] ["C" 3] ["10" 1] ["D" 4] ["10" 2] ["E" 5] ["F" 5]])

; [[["B" 2] ["C" 3] ["10" 1]] [["D" 4] ["10" 2]] [["E" 5] ["F" 5]]]


(def header-tags #{"8" "9" "34" "49" "50" "56" "57"
                   ;"52" send time
                   "10"
                   })

(defn header? [[t v]]
  (contains? header-tags t))


(defn without-header [tv]
  (->> tv
       (remove header?)
       (into [])))

;(without-header 
;  [["8" "FIX.4.4"] ["146" "x"] ["35" "W"] ["34" "153"] ["49" "cServer"] ["50" "QUOTE"] ["52" "20250227-01:23:39.107"] ["56" "demo.tradeviewmarkets.3193335"] ["57" "QUOTE"] ["55" "4"] ["268" "2"] ["269" "0"] ["270" "149.161"] ["269" "1"] ["270" "149.169"] ["10" "142"]]
; )

