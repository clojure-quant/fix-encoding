(ns demo.wire
  (:require 
   [fix-translator.message-wire :refer [vec->wire wire->vec ]]))

(wire->vec "8=5T=abcA=4")

(def msg-login-response
  [["8" "FIX.4.4"] ["9" "115"] ["35" "A"] ["34" "1"] ["49" "cServer"] ["50" "QUOTE"]
   ["52" "20250228-21:09:20.287"] ["56" "demo.tradeviewmarkets.3193335"] ["57" "QUOTE"]
   ;
   ["98" "0"] ["108" "60"] ["141" "Y"]
   ["10" "210"]])

(vec->wire msg-login-response)



;; Example usage
;(transduce xf-fix-message 
;           conj [] 
;           [["B" 2] ["C" 3] ["10" 1] ["D" 4] ["10" 2] ["E" 5] ["F" 5]])

; [[["B" 2] ["C" 3] ["10" 1]] [["D" 4] ["10" 2]] [["E" 5] ["F" 5]]]


;(without-header 
;  [["8" "FIX.4.4"] ["146" "x"] ["35" "W"] ["34" "153"] ["49" "cServer"] ["50" "QUOTE"] ["52" "20250227-01:23:39.107"] ["56" "demo.tradeviewmarkets.3193335"] ["57" "QUOTE"] ["55" "4"] ["268" "2"] ["269" "0"] ["270" "149.161"] ["269" "1"] ["270" "149.169"] ["10" "142"]]
; )