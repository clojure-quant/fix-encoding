(ns demo.decode
  (:require 
   [fix-translator.schema :refer [ create-decoder
                                  ; debugging of schema:
                                  types-in-spec get-msg-type get-field]]
   [fix-translator.field :refer [decode-fields]]
   [fix-translator.message :refer [decode-fix-msg
                                   encode-fix-msg
                                   ]]
   [demo.messages :as m]
   [lambdaisland.deep-diff2 :as ddiff]))


(def ctrader (create-decoder "resources/fix-specs/ctrader.edn"))

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


(decode-fields ctrader m/new-order-msg)



(decode-fields ctrader m/quote-subscribe-msg)

(decode-fields ctrader m/logout-msg)


(decode-fix-msg ctrader m/new-order-msg)

(decode-fix-msg ctrader m/quote-subscribe-msg)

(decode-fix-msg ctrader m/logout-msg)


(def out-msg 
{:header
 {:target-comp-id "demo.tradeviewmarkets.3152195",
  :sending-time #time/instant "2025-02-24T21:13:01.525Z",
  :body-length 118,
  :sender-comp-id "CSERVER",
  :msg-seq-num 1,
  :msg-type "5",
  :begin-string "FIX.4.4",
  :target-sub-id "QUOTE",
  :sender-sub-id "QUOTE"}})


(encode-fix-msg ctrader out-msg)

(:header ctrader)