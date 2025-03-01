(ns demo.encode
  (:require
     [fix-translator.schema :refer [create-decoder]]
   [fix-translator.message :refer [encode-fix-body
                                   checksum
                                   encode-header
                                   insert-begin-size
                                   add-checksum
                                   encode-fix-msg]]
   ))

(def ctrader (create-decoder "fix-specs/ctrader.edn"))

(def out-logout-msg
  {:header
   {:target-comp-id "demo.tradeviewmarkets.3152195",
    :sending-time #time/instant "2025-02-24T21:13:01.525Z",
    :body-length 118,
    :sender-comp-id "CSERVER",
    :msg-seq-num 1,
    :msg-type "5",
    :begin-string "FIX.4.4",
    :target-sub-id "QUOTE",
    :sender-sub-id "QUOTE"}
   :payload {:text "RET_NO_SUCH_LOGIN"}})


(encode-fix-body ctrader ["5" {:text "RET_NO_SUCH_LOGIN"}])

(encode-header ctrader (:header out-logout-msg))

 (insert-begin-size "FIX.4.4"
                        ; header
                   [["35" "5"]
                    ["49" "CSERVER"]
                    ["56" "demo.tradeviewmarkets.3152195"]
                    ["34" "1"]
                    ["50" "QUOTE"]
                    ["57" "QUOTE"]
                    ["52" "20250224-21:13:01"]]
                        ;payload
                   [["58" "RET_NO_SUCH_LOGIN"]])


(add-checksum
 [["8" "FIX.4.4"]
  ["9" "114"]
  ["35" "5"]
  ; header
  ["49" "CSERVER"]
  ["56" "demo.tradeviewmarkets.3152195"]
  ["34" "1"]
  ["50" "QUOTE"]
  ["57" "QUOTE"]
  ["52" "20250224-21:13:01"]
  ; payload
  ["58" "RET_NO_SUCH_LOGIN"]])


(encode-fix-msg ctrader out-logout-msg)



(def out-quote-subscribe-msg
  {:header
   {:target-comp-id "CSERVER",
    :sending-time #time/instant "2025-02-24T21:13:01Z",
    :body-length 146,
    :sender-comp-id "demo.tradeviewmarkets.3152195",
    :msg-seq-num 6,
    :msg-type "V",
    :begin-string "FIX.4.4",
    :target-sub-id "QUOTE",
    :sender-sub-id "QUOTE"},
   :payload
   {:mdreq-id "6",
    :subscription-request-type :snapshot-plus-updates,
    :market-depth 1,
    :mdupdate-type :incremental-refresh,
    :no-mdentry-types [{:mdentry-type :bid} {:mdentry-type :offer}],
    :no-related-sym [{:symbol "4"}]}})

(encode-fix-msg ctrader out-quote-subscribe-msg)