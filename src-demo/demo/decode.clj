(ns demo.decode
  (:require 
   [fix-translator.core :refer [create-decoder
                                types-in-spec
                                parse-fix-message
                                enrich-message
                                read-message]]
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
 parse-fix-message
 (enrich-message ctrader)
 (read-message ctrader)
 )



(str 1.0M)

(-> "1" bigdec str)
