(ns demo.tcp
  (:require
   [fix-translator.session :refer [load-accounts create-session
                                   encode-msg decode-msg]]
   [aleph.tcp :as tcp]
   [manifold.deferred :as d]
   [manifold.stream :as s]
   [clj-commons.byte-streams :as bs]))

(def s (-> (load-accounts "fix-accounts.edn")
           (create-session :ctrader-tradeviewmarkets-quote)))

s

(defn handle-incoming [stream]
  (s/consume
   (fn [raw-data]
     (let [text (bs/to-string raw-data "US-ASCII") ;; Convert bytes to string
           ;(.getBytes "Hello, ASCII!" "US-ASCII")
           ]
       (println "IN:" text)
       (spit "msg.log" (str "\nIN: " text) :append true)
       (println "IN-EDN: " (decode-msg s text))
       ))
   stream))

(defn create-client
  []
  (let [tcp-config (select-keys (:config s) [:host :port])]
    (println "connecting fix to: " tcp-config)
    @(tcp/client tcp-config)))


(def c (create-client))

(handle-incoming c)


(defn create-fix-msg [{:keys [fix-type fix-payload]}]
  (let [out-msg (->> (encode-msg s fix-type fix-payload)
                     :wire)
        ]
    (println "OUT: " out-msg)
    (spit "msg.log" (str "\nOUT: " out-msg) :append true)
    (.getBytes out-msg "US-ASCII")
    ))

(def login-payload
  {:fix-type "A"
   :fix-payload {:encrypt-method :none-other,
                 :heart-bt-int 60,
                 :reset-seq-num-flag "Y",
                 :username "3193299",
                 :password "2025Florian"}
   })

@(s/put! c (create-fix-msg login-payload))




@(s/take! c)

c