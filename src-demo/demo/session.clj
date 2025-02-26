(ns demo.session
  (:require
   [fix-translator.session :refer [load-accounts create-session
                                   encode-msg decode-msg]]))



(def s (-> (load-accounts "fix-accounts.edn")
           (create-session :ctrader-tradeviewmarkets-quote)))

s

(encode-msg s "W" {:symbol "MSFT" :qty 3})

(encode-msg s "A"
            {:encrypt-method :none-other,
             :heart-bt-int 60,
             :reset-seq-num-flag "Y",
             :username "3193299",
             :password "2025Florian"})

(->> (encode-msg s "5" {:text "RET_NO_SUCH_LOGIN"})
     :wire
     (decode-msg s))

; logout msg

(->> (encode-msg s "5" {:text "RET_NO_SUCH_LOGIN"})
     :wire
     (decode-msg s )
     )
