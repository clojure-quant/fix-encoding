(ns demo.decode
  (:require 
   [fix-translator.schema :refer [ create-decoder
                                  ; debugging of schema:
                                  types-in-spec get-msg-type get-field]]
   [fix-translator.field :refer [decode-fields]]
   [fix-translator.message :refer [decode-fix-msg
                                   encode-fix-msg
                                   ]]
   [demo.messages :as m]))


(def ctrader (create-decoder "fix-specs/ctrader.edn"))

ctrader
(keys ctrader)
(:messages ctrader)
(-> ctrader :messages keys)

(:trailer ctrader)
(:tag->field ctrader)

;(ddiff/pretty-print 
; (ddiff/diff (:tag->field ctrader) (:fields ctrader)))


(types-in-spec ctrader)

(get-field ctrader "35")

; for message type lookup we have to use tag, because
; the fxcm dictionary is misleading.
(get-msg-type ctrader :order-single)
(get-msg-type ctrader "D")
(get-msg-type ctrader "V")

;; DECODE FIELDS

; login
(decode-fields ctrader m/login-msg)
(decode-fields ctrader m/logout-msg)

; quote
(decode-fields ctrader m/security-list-req)
(decode-fields ctrader m/quote-subscribe-msg)
(decode-fields ctrader m/new-order-msg)


;; DECODING OF MESSAGE

; login
(decode-fix-msg ctrader m/login-msg)


(decode-fix-msg ctrader m/logout-msg)

; quote
(decode-fix-msg ctrader m/security-list-req)
(decode-fix-msg ctrader m/quote-subscribe-msg)
(decode-fix-msg ctrader m/new-order-msg)






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
 :payload {:text "RET_NO_SUCH_LOGIN"}
 })

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


(->> m/logout-msg
     (decode-fix-msg ctrader)
     (encode-fix-msg ctrader)
    )

; "8=FIX.4.49=11435=549=CSERVER56=demo.tradeviewmarkets.315219534=150=QUOTE57=QUOTE52=20250224-21:13:0158=RET_NO_SUCH_LOGIN",
; "8=FIX.4.49=11835=549=CSERVER56=demo.tradeviewmarkets.315219534=150=QUOTE57=QUOTE52=20250224-21:13:01.52558=RET_NO_SUCH_LOGIN10=172")
;:checksum "172",
;:body-length 118