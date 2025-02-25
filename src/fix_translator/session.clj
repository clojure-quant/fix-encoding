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


(defn encode-msg [{:keys [config outbound-seq-num decoder] :as session} msg-type payload]
  (let [seq-num (swap! outbound-seq-num inc)
        header (assoc (:header config)
                      :msg-type msg-type
                      ; added fields
                      :msg-seq-num seq-num
                      :sending-time (t/instant)
                      ; calculated fields
                      :body-length 0
                      )]
    
    (encode-fix-msg decoder {:header header
                             :payload payload})
    ))

(defn decode-msg [{:keys [config outbound-seq-num decoder] :as session} fix-msg-str]
  (decode-fix-msg decoder fix-msg-str)
  
  )