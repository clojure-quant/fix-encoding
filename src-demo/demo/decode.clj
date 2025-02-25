(ns demo.decode
  (:require 
   [fix-translator.schema :refer [ create-decoder
                                  ; debugging of schema:
                                  types-in-spec get-msg-type get-field]]
   [fix-translator.field :refer [decode-fields]]
   [fix-translator.message :refer [decode-fix-msg]]
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

