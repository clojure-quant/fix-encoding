(ns demo.gloss
  (:require [gloss.core :as gloss]
            [gloss.io :as io]))

(def fix-field
  (gloss/compile-frame
   [(gloss/string :ascii :delimiters ["="])
    (gloss/string :ascii :delimiters ["|"])]))


; working
;(io/decode fix-field "8=4|")
;(io/decode-all fix-field "8=5|T=abc|A=4|")

; bytes left over
;(io/decode fix-field "8=5|T=abc|A=4|")
;(io/decode-all fix-field "8=5|T=abc|A=4|f")



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


