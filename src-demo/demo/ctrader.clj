(ns demo.ctrader 
  (:require
   [fix-translator.schema :refer [create-decoder]]
   [fix-translator.field :refer [decode-fields]]
   [fix-translator.message :refer [decode-message
                                   decode-payload
                                   decode-header
                                   decode-trailer
                                   create-reader
                                   fix->payload
                                   ]]
   [fix-translator.ctrader :refer [seclist->assets 
                                   write-assets 
                                   create-asset-converter
                                   get-asset-id
                                   get-asset-name
                                   incoming-quote-id-convert
                                   subscribe-payload]]
   [demo.data-payload :as payload]
   [demo.data-message :as msg]
   ))
  
(def ctrader (create-decoder "fix-specs/ctrader.edn"))

; field parser
(decode-fields ctrader (second payload/login-response))
(decode-fields ctrader msg/login-response)

; payload parser
(decode-payload ctrader payload/login-response)
(decode-payload ctrader payload/heartbeat)
(decode-payload ctrader payload/Test)
(decode-payload ctrader payload/seclist-response)

; header/trailer parser

(decode-header ctrader 
               (->> (decode-fields ctrader [["8" "FIX.4.4"] ["9" "97"] ["35" "0"] ["34" "3"] ["49" "cServer"] ["50" "QUOTE"]
                                            ["52" "20250228-21:10:20.425"] ["56" "demo.tradeviewmarkets.3193335"] ["57" "QUOTE"]])
                    create-reader)
               )

(decode-trailer ctrader
                (->> (decode-fields ctrader [["10" "069"]])
                     create-reader))

; message parser
(decode-message ctrader msg/login-response)
(decode-message ctrader msg/heartbeat)
(decode-message ctrader msg/Test)
(decode-message ctrader msg/seclist-response)




; payload extractor
(fix->payload ctrader msg/login-response)
(fix->payload ctrader msg/heartbeat)
(fix->payload ctrader msg/Test)
(fix->payload ctrader msg/seclist-response)
(fix->payload ctrader msg/quote-response)


(-> (fix->payload ctrader msg/seclist-response)
    (seclist->assets)
    (write-assets))

(def converter 
  (-> (fix->payload ctrader msg/seclist-response)
      (seclist->assets)
      (create-asset-converter)))

converter


(get-asset-name converter "1")
(get-asset-id converter "EURUSD")

(get-asset-id converter "x")
(get-asset-name converter "x")

(get-asset-id nil "x")
(get-asset-name nil "x")

(-> (fix->payload ctrader msg/seclist-response)
    (write-assets))

(incoming-quote-id-convert {:converter (atom converter)} {:asset "1" 
                                                          :price 3 :x :y
                                                          })

(incoming-quote-id-convert {:converter nil} {:asset "1"
                                                          :price 3 :x :y})


(subscribe-payload ["1" "2" "3"])
