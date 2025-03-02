(ns demo.quotes
  (:require
   [fix-translator.session :refer [load-accounts create-session decode-msg]]
   [fix-translator.ctrader :refer [->quote]]))

(def s (-> (load-accounts "fix-accounts.edn")
           (create-session :ctrader-tradeviewmarkets-quote)))

(def quote-msg
  [["8" "FIX.4.4"] ["9" "144"] ["35" "W"] ["34" "3"] ["49" "cServer"] ["50" "QUOTE"] ["52" "20250302-13:51:38.677"] ["56" "demo.tradeviewmarkets.3193335"] ["57" "QUOTE"] ["55" "1"] ["268" "2"] ["269" "0"] ["270" "1.03752"] ["269" "1"] ["270" "1.03763"] ["10" "030"]])

(decode-msg s quote-msg)

; ["W"
; {:symbol "1",
;  :no-mdentries [{:mdentry-type :bid, :mdentry-px 1.03752M}
;                 {:mdentry-type :offer, :mdentry-px 1.03763M}]}]

(-> (decode-msg s quote-msg)
    ->quote)
