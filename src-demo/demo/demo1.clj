(ns demo.demo1
  (:require
   [clojure.java.io :as io]
   ;(clj-time [core :as t] [format :as f])
   [fix-translator.core :refer [load-spec get-encoder encode-msg get-msg-type
                                get-decoder decode-msg]]
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
 

 
 


