(ns demo.session
  (:require
   [fix-translator.session :refer [load-accounts create-session
                                   encode-msg2]]))



(def s (-> (load-accounts "fix-accounts.edn")
           (create-session :ctrader-tradeviewmarkets-quote)))

s

; login message
(encode-msg2 s "A"
            {:encrypt-method :none-other,
             :heart-bt-int 60,
             :reset-seq-num-flag "Y",
             :username "3193299",
             :password "2025Florian"})

; logout msg
(encode-msg2 s "5" {:text "RET_NO_SUCH_LOGIN"})

; security list
(encode-msg2 s "x"
            {:security-req-id "125"
             :security-list-request-type :symbol})


; market data subscribe
(encode-msg2 s "V" {:mdreq-id  "123"
                  :subscription-request-type :snapshot-plus-updates,
                  :market-depth 1,
                  :mdupdate-type :incremental-refresh,
                  :no-mdentry-types [{:mdentry-type :bid} {:mdentry-type :offer}],
                  :no-related-sym [{:symbol "4"} ; eurjpy
                                   {:symbol "1"} ; eurusd
                                   ]})

(try 
  (encode-msg2 s "W" {:symbol "MSFT" :qty 3})  
  (catch Exception ex
         (println "error: " (ex-data ex)))
  )






; ctrader python example
; https://github.com/spotware/cTraderFixPy/blob/main/ctrader_fix/messages.py
