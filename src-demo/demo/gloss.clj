(ns demo.gloss
  (:require [gloss.core :as gloss]
            [gloss.io :as io]))

(def fix-field
  (gloss/compile-frame
   [(gloss/string :ascii :delimiters ["="])
    (gloss/string :ascii :delimiters ["|"])]))
  

; working
(io/decode fix-field "8=4|")
(io/decode-all fix-field "8=5|T=abc|A=4|")

; bytes left over
(io/decode fix-field "8=5|T=abc|A=4|")
(io/decode-all fix-field "8=5|T=abc|A=4|f")



(->> (io/encode fix-field ["8=5|T=abc|A=4|"])
    (take 2)
    )



