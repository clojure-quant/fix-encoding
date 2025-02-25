(ns demo.demo1
  (:require
   [clojure.java.io :as io]
   ;(clj-time [core :as t] [format :as f])
   [fix-translator.core :refer [load-spec get-encoder encode-msg get-msg-type
                                          get-decoder decode-msg 
                                parse-fix-message
                                create-decoder
                                enrich-message
                                read-section
                                read-header
                                ]]
   ))

(format "%05d" 3)

(format "%04d" 234)

(slurp (io/resource "bongo.edn"))


(load-spec :test-market)

(def encoder (get-encoder :test-market))


(def decoder (get-decoder :test-market))

 
(encoder :msg-type)

 (decode-msg :test-market :execution-report
            "35=8\u000144=1.0\u000155=NESNz\u000139=0\u0001")

(let [msg [:msg-type :new-order-single 
           :side :buy 
           :order-qty 100
           :symbol "NESNz" 
           :price 1.00]]
  (->> (encode-msg :test-market msg)
       ;(get-msg-type :test-market)
       ;(decode-msg :test-market :new-order-single)
   
   ))
;; => "8=FIX.4.29=3335=D54=138=10055=NESNz44=1.010=131"

(def new-order-msg  "8=FIX.4.2\u00019=33\u000135=D\u000154=1\u000138=100\u000155=NESNz\u000144=1.0\u000110=131\u0001")

(decode-msg :test-market :new-order-single 
           new-order-msg
            )
 

 (parse-fix-message "35=8\u000144=1.0\u000155=NESNz\u000139=0\u0001")



 (parse-fix-message "8=FIX.4.29=3335=D54=138=10055=NESNz44=1.010=131")
 
 
 (def logout-msg
   "8=FIX.4.49=11835=549=CSERVER56=demo.tradeviewmarkets.315219534=150=QUOTE57=QUOTE52=20250224-21:13:01.52558=RET_NO_SUCH_LOGIN10=172")

(def quote-subscribe-msg 
  "8=FIX.4.49=14635=V49=demo.tradeviewmarkets.315219556=CSERVER50=QUOTE57=QUOTE34=652=20250224-21:13:01262=6263=1265=1264=1267=2269=0269=1146=155=410=080"
  )


(def ctrader (create-decoder "resources/fix-specs/ctrader.edn"))


ctrader
 (:messages ctrader)
 (:trailer ctrader)
;; (def 
;; (def )
;; (def message '({:tag "8", :value "FIX.4.4"} ...))

(->> logout-msg
     ;new-order-msg
 ;quote-subscribe-msg
 parse-fix-message
 (enrich-message ctrader)
 ;(read-section (:header ctrader))
 (read-header ctrader)
 
 )



({:tag "8", :value "FIX.4.4", :name "BeginString", :value2 ""}
 {:tag "9", :value "118", :name "BodyLength", :value2 ""}
 {:tag "35", :value "5", :name "MsgType", :value2 "LOGOUT"}
 {:tag "34", :value "1", :name "MsgSeqNum", :value2 ""}
 {:tag "49", :value "CSERVER", :name "SenderCompID", :value2 ""}
 {:tag "50", :value "QUOTE", :name "SenderSubID", :value2 ""}
 {:tag "52", :value "20250224-21:13:01.525", :name "SendingTime", :value2 ""}
 {:tag "56", :value "demo.tradeviewmarkets.3152195", :name "TargetCompID", :value2 ""}
 {:tag "57", :value "QUOTE", :name "TargetSubID", :value2 ""}
 {:tag "58", :value "RET_NO_SUCH_LOGIN", :name "Text", :value2 ""}
 {:tag "10", :value "172", :name "CheckSum", :value2 ""})




