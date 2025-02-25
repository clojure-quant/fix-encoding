(ns demo.session
 (:require 
  
    [fix-translator.session :refer [load-accounts create-session
                                    encode-msg
                                    ]]
  ) 
  
  )



(def s (-> (load-accounts "fix-accounts.edn")
           (create-session :ctrader-tradeviewmarkets-quote)))

s

(encode-msg s "W" {:symbol "MSFT" :qty 3})

{:header
 {:target-comp-id "demo.tradeviewmarkets.3152195",
  :SendingTime #time/instant "2025-02-25T16:07:52.661141650Z",
  :sender-comp-id "CSERVER",
  :MsgType "W",
  :MsgSeqNum 2, ; calc
  :begin-string "FIX.4.4",
  :BodyLength 0, ; calc
  :target-sub-id "QUOTE",
  :sender-sub-id "QUOTE"},
 :payload {:symbol "MSFT", :qty 3}}