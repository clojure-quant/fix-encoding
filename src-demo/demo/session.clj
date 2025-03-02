(ns demo.session
  (:require
   [fix-translator.session :refer [load-accounts create-session
                                   encode-msg]]))



(def s (-> (load-accounts "fix-accounts.edn")
           (create-session :ctrader-tradeviewmarkets-quote)))

s

; login message
(encode-msg s ["A"
               {:encrypt-method :none-other,
                :heart-bt-int 60,
                :reset-seq-num-flag "Y",
                :username "3193299",
                :password "2025Florian"}])

; logout msg
(encode-msg s ["5" {:text "RET_NO_SUCH_LOGIN"}])

; security list
(encode-msg s ["x" {:security-req-id "125"
                    :security-list-request-type :symbol}])

; market data subscribe
(encode-msg s ["V" {:mdreq-id  "123"
                    :subscription-request-type :snapshot-plus-updates,
                    :market-depth 1,
                    :mdupdate-type :incremental-refresh,
                    :no-mdentry-types [{:mdentry-type :bid} {:mdentry-type :offer}],
                    :no-related-sym [{:symbol "4"} ; eurjpy
                                     {:symbol "1"} ; eurusd
                                     ]}])

(try 
  (encode-msg s ["W" {:symbol "MSFT" :qty 3}])  
  (catch Exception ex
         (println "error: " (ex-data ex)))
  )






; ctrader python example
; https://github.com/spotware/cTraderFixPy/blob/main/ctrader_fix/messages.py
