(ns fix-translator.session
  (:require
   [clojure.edn :as edn]
   [tick.core :as t]
   [fix-translator.schema :refer [create-decoder]]
   [fix-translator.message :refer [decode-fix-msg
                                   encode-fix-msg]]
   
   ))


(defn load-accounts [accounts-edn-file]
  (-> accounts-edn-file slurp edn/read-string))


(defn create-session [accounts fix-session-kw]
  (let [config (fix-session-kw accounts)]
    {:config config
     :decoder (create-decoder (:spec config))
     :inbound-seq-num (atom 1)
     :outbound-seq-num (atom 1)}))


(defn encode-msg [{:keys [config outbound-seq-num] :as session} msg-type payload]
  (let [seq-num (swap! outbound-seq-num inc)
        header (assoc (:header config)
                      :msg-type msg-type
                      :msg-seq-num seq-num
                      :body-length 0
                      :sending-time (t/instant))]
    {:header header
     :payload payload}))

