(ns fix-translator.session
  (:require
   [clojure.edn :as edn]
   [tick.core :as t]))


(defn load-accounts [accounts-edn-file]
  (-> accounts-edn-file slurp edn/read-string))


(defn create-session [accounts fix-session-kw]
  (let [config (fix-session-kw accounts)]
    {:config config
     :inbound-seq-num (atom 1)
     :outbound-seq-num (atom 1)}))


(defn encode-msg [{:keys [config outbound-seq-num] :as session} msg-type payload]
  (let [seq-num (swap! outbound-seq-num inc)
        header (assoc (:header config)
                      :MsgType msg-type
                      :MsgSeqNum seq-num
                      :BodyLength 0
                      :SendingTime (t/instant))]
    {:header header
     :payload payload}))


(def s (-> (load-accounts "fix-accounts.edn")
           (create-session :ctrader-tradeviewmarkets-quote)))

s

(encode-msg s "W" {:symbol "MSFT" :qty 3})

{:header
 {:target-comp-id "demo.tradeviewmarkets.3152195",
  :SendingTime #time/instant "2025-02-25T16:07:52.661141650Z",
  :sender-comp-id "CSERVER",
  :MsgType "W",
  :MsgSeqNum 2,
  :begin-string "FIX.4.4",
  :BodyLength 0,
  :target-sub-id "QUOTE",
  :sender-sub-id "QUOTE"},
 :payload {:symbol "MSFT", :qty 3}}