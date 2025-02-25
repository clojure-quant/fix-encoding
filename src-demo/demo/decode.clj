(ns demo.decode
  (:require 
   [fix-translator.schema :refer [types-in-spec create-decoder]]
   [fix-translator.field :refer [decode-fields]]
   [fix-translator.message :refer [decode-fix-msg]]
   [demo.messages :as m]))


(def ctrader (create-decoder "resources/fix-specs/ctrader.edn"))


ctrader
(keys ctrader)
(:messages ctrader)
(:trailer ctrader)
(:fields ctrader)

(types-in-spec ctrader)



(decode-fields ctrader m/new-order-msg)

(decode-fields ctrader m/quote-subscribe-msg)

(decode-fields ctrader m/logout-msg)


(decode-fix-msg ctrader m/new-order-msg)

(decode-fix-msg ctrader m/quote-subscribe-msg)

(decode-fix-msg ctrader m/logout-msg)

