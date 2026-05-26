(ns demo.session
  (:require
   [fix-translator.session :refer [create-session
                                   payload->fix-msg-vec fix-msg-vec->payload]]
   ;[fix-translator.data.data-message :as msg]
   ))


(def s (create-session {:spec "fix-specs/ctrader.edn"
                        :header {:begin-string "FIX.4.4"
                                 :target-comp-id "cServer"
                                 :sender-comp-id "demo.tradeviewmarkets.3193335"
                                 :target-sub-id "QUOTE"
                                 :sender-sub-id "QUOTE"}
                        :host "demo-uk-eqx-01.p.c-trader.com"
                        :port 5201 ; plain text
           ;ssl-port 5211
                        :username "3193335"
                        :password "123456789"
                        :log? true}))
s
; login message
(payload->fix-msg-vec
 s [:logon
    {:encrypt-method :none-other,
     :heart-bt-int 60,
     :reset-seq-num-flag "Y",
     :username "3193299",
     :password "2025Florian"}])

; logout msg
(payload->fix-msg-vec s [:logout {:text "RET_NO_SUCH_LOGIN"}])


; heartbeat
(payload->fix-msg-vec s [:heartbeat {}])

; security list
(payload->fix-msg-vec s [:security-list-request {:security-req-id "125"
                                                 :security-list-request-type :symbol}])

; market data subscribe
(payload->fix-msg-vec s [:market-data-request {:mdreq-id  "123"
                                               :subscription-request-type :snapshot-plus-updates,
                                               :market-depth 1,
                                               :mdupdate-type :incremental-refresh,
                                               :no-mdentry-types [{:mdentry-type :bid}
                                                                  {:mdentry-type :offer}],
                                               :no-related-sym [{:symbol "4"} ; eurjpy
                                                                {:symbol "1"} ; eurusd
                                                                ]}])

(try
  (payload->fix-msg-vec s [:market-data-snapshot-full-refresh {:symbol "MSFT" :qty 3}])
  (catch Exception ex
    (println "error: " (ex-data ex))))
;  {:msg-type :no-mdentries, 
;   :missing-field {:type :group, :name :no-mdentries, :required true, 
