(ns demo.gloss
  (:require 
    [fix-translator.gloss :as ft]
   [gloss.io :as io]
   )
  )


; working
;(io/decode fix-field "8=4|")
(io/decode-all ft/fix-protocol "8=5T=abcA=4")

; bytes left over
;(io/decode fix-field "8=5T=abcA=4")
;(io/decode-all fix-field "8=5T=abcA=4f")

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
(io/encode-all ft/fix-protocol)
;(map .toString)     
 )
;(->> (io/encode ft/fix-protocol ["8=5T=abcA=4"])
    ;(take 2)
    ;)


;; Example usage
;(transduce xf-fix-message 
;           conj [] 
;           [["B" 2] ["C" 3] ["10" 1] ["D" 4] ["10" 2] ["E" 5] ["F" 5]])

; [[["B" 2] ["C" 3] ["10" 1]] [["D" 4] ["10" 2]] [["E" 5] ["F" 5]]]


;(without-header 
;  [["8" "FIX.4.4"] ["146" "x"] ["35" "W"] ["34" "153"] ["49" "cServer"] ["50" "QUOTE"] ["52" "20250227-01:23:39.107"] ["56" "demo.tradeviewmarkets.3193335"] ["57" "QUOTE"] ["55" "4"] ["268" "2"] ["269" "0"] ["270" "149.161"] ["269" "1"] ["270" "149.169"] ["10" "142"]]
; )