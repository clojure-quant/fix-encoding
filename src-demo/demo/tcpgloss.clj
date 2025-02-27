(ns demo.tcpgloss
  (:require
   [fix-translator.session :refer [load-accounts create-session
                                   encode-msg decode-msg]]
   [aleph.tcp :as tcp]
   [gloss.core :as gloss]
   [gloss.io :as io]
   [manifold.deferred :as d]
   [manifold.stream :as s]
   [nano-id.core :refer [nano-id]]
   [demo.gloss :refer [xf-fix-message]]
   ))



(def fix-protocol-in
  (gloss/compile-frame
   [(gloss/string :ascii :delimiters ["="])
    (gloss/string :ascii :delimiters [""])]))

(def fix-protocol-out
  (gloss/string :ascii)) ; :utf-8

(defn wrap-duplex-stream
  [s]
  (let [out (s/stream)]
    (s/connect
     (s/map #(io/encode fix-protocol-out %) out)
     s)
    (s/splice
     out
     (io/decode-stream s fix-protocol-in))))


(defn transform-message [s]
  (let [msg-s (s/transform xf-fix-message s)]
    (s/consume
       (fn [raw-data]
         (println "IN:" raw-data)
         (spit "msg.log" (str "\nIN: " raw-data) :append true)
         ;(println "IN-EDN: " (decode-msg s text))
     ) msg-s)
  (println "Connection closed 3")  
    
    
    )
  
  
  )


(defn create-client
  [s]
  (let [tcp-config (select-keys (:config s) [:host :port])
        _ (println "connecting fix to: " tcp-config)
        _ (spit "msg.log" "\nCONNECTING" :append true)
        c (tcp/client tcp-config)
        r (d/chain c #(wrap-duplex-stream %))]
    @r))




(defn handle-incoming [s stream]
   (future
     (s/consume
      (fn [raw-data]
        ;(println "IN:" raw-data)
        ;(spit "msg.log" (str "\nIN: " raw-data) :append true)
     ;(println "IN-EDN: " (decode-msg s text))
        )
      stream)
     (println "Connection closed")
     (spit "msg.log" "\nCLOSED " :append true)
     ))

(defn login-payload [s]
  {:fix-type "A"
   :fix-payload {:encrypt-method :none-other,
                 :heart-bt-int 60,
                 :reset-seq-num-flag "Y",
                 :username (str (get-in s [:config :username]))
                 :password (str (get-in s [:config :password]))
                 }})


(defn heartbeat-payload []
  {:fix-type "0"
   :fix-payload {:test-request-id  (nano-id 5)}})


(defn subscribe-payload []
  {:fix-type "V"
   :fix-payload {:mdreq-id  (nano-id 5)
                 :subscription-request-type :snapshot-plus-updates,
                 :market-depth 1,
                 :mdupdate-type :incremental-refresh,
                 :no-mdentry-types [{:mdentry-type :bid} {:mdentry-type :offer}],
                 :no-related-sym [{:symbol "4"} ; eurjpy
                                  {:symbol "1"} ; eurusd
                                  ]}})

(defn security-list-request []
  {:fix-type "x"
   :fix-payload {:security-req-id (nano-id 5) ; req id
                 :security-list-request-type :symbol}})


(defn create-fix-msg [s {:keys [fix-type fix-payload]}]
  (let [out-msg (->> (encode-msg s fix-type fix-payload)
                     :wire)]
    (println "OUT: " out-msg)
    (spit "msg.log" (str "\nOUT: " out-msg) :append true)
    ;(.getBytes out-msg "US-ASCII")
    out-msg
    ))


(defn handle-disconnect [conn]
  (s/on-closed conn 
               (fn [& _]
                 (println "Connection closed2")
                 (spit "msg.log" "\nCLOSED2 " :append true)
                 )))

(defn start []
  (let [s (-> (load-accounts "fix-accounts.edn")
              (create-session :ctrader-tradeviewmarkets2-quote))
        c (create-client s)]
    (handle-disconnect c)
    (handle-incoming s c)
    (transform-message c)
    @(s/put! c (create-fix-msg s (login-payload s)))
    ;@(s/put! c (create-fix-msg s (security-list-request)))
    @(s/put! c (create-fix-msg s (subscribe-payload)))
    {:c c :s s}
    ))

(comment 
  
  
(def cs (start))

@(s/put! (:c cs) (create-fix-msg (:s cs) (security-list-request)))

@(s/put! (:c cs) (create-fix-msg (:s cs) (heartbeat-payload)))
  

@(s/put! (:c cs) (create-fix-msg (:s cs) (subscribe-payload)))  

(s/close! (:c cs))

(:c cs)
(->>(-> (load-accounts "fix-accounts.edn")
        (create-session :ctrader-tradeviewmarkets2-quote))
 :config
 )
  
 ;
)












;@(s/take! c)

