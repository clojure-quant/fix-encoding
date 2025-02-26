(ns demo.tcp
  (:require
   [fix-translator.session :refer [load-accounts create-session
                                   encode-msg decode-msg]]
   [aleph.tcp :as tcp]
   [manifold.deferred :as d]
   [manifold.stream :as s]
   [clj-commons.byte-streams :as bs]))

(defn handle-incoming [s stream]
  (s/consume
   (fn [raw-data]
     (let [text (bs/to-string raw-data "US-ASCII") ;; Convert bytes to string
           ;(.getBytes "Hello, ASCII!" "US-ASCII")
           ]
       (println "IN:" text)
       (spit "msg.log" (str "\nIN: " text) :append true)
       (println "IN-EDN: " (decode-msg s text))))
   stream))

(defn create-client
  [s]
  (let [tcp-config (select-keys (:config s) [:host :port])]
    (println "connecting fix to: " tcp-config)
    (spit "msg.log" "\nCONNECTING" :append true)
    @(tcp/client tcp-config)))

(defn login-payload [s]
  {:fix-type "A"
   :fix-payload {:encrypt-method :none-other,
                 :heart-bt-int 60,
                 :reset-seq-num-flag "Y",
                 :username (str (get-in s [:config :username]))
                 :password (str (get-in s [:config :password]))
                 }})

(def subscribe-payload
  {:fix-type "V"
   :fix-payload {:mdreq-id "1455",
                 :subscription-request-type :snapshot-plus-updates,
                 :market-depth 1,
                 :mdupdate-type :incremental-refresh,
                 :no-mdentry-types [{:mdentry-type :bid} {:mdentry-type :offer}],
                 :no-related-sym [{:symbol "4"}]}})

(defn create-fix-msg [s {:keys [fix-type fix-payload]}]
  (let [out-msg (->> (encode-msg s fix-type fix-payload)
                     :wire)]
    (println "OUT: " out-msg)
    (spit "msg.log" (str "\nOUT: " out-msg) :append true)
    (.getBytes out-msg "US-ASCII")))



(defn start []
  (let [s (-> (load-accounts "fix-accounts.edn")
              (create-session :ctrader-tradeviewmarkets2-quote))
        c (create-client s)]
    (handle-incoming s c)
    @(s/put! c (create-fix-msg s (login-payload s)))
    @(s/put! c (create-fix-msg s subscribe-payload))
    ))


(start)

(->>(-> (load-accounts "fix-accounts.edn")
        (create-session :ctrader-tradeviewmarkets2-quote))
 :config
 )
 













;@(s/take! c)

