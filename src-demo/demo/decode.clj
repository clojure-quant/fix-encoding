(ns demo.decode
  (:require 
   [fix-translator.core :refer [create-decoder
                                types-in-spec
                                ->tag-value-pairs
                                enrich-message
                                read-message
                                decode-fix-msg
                                ]]
   [demo.messages :as m]))

(def ctrader (create-decoder "resources/fix-specs/ctrader.edn"))


ctrader
(keys ctrader)
(:messages ctrader)
(:trailer ctrader)
(:fields ctrader)

(types-in-spec ctrader)

(->> ;m/logout-msg
     
 ;m/quote-subscribe-msg
 m/new-order-msg
 ->tag-value-pairs
 (enrich-message ctrader)
 (read-message ctrader)
 )


(decode-fix-msg ctrader m/new-order-msg)

(decode-fix-msg ctrader m/quote-subscribe-msg)

(decode-fix-msg ctrader m/logout-msg)
