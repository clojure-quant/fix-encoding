(ns fix-translator.session
  (:require
   [tick.core :as t]
   [fix-translator.schema :refer [create-decoder]]
   [fix-translator.message :refer [decode-message
                                   encode-message]]))

(defn create-session  [account-config]
  {:config account-config
   :decoder (create-decoder (:spec account-config))
   :inbound-seq-num (atom 0)
   :outbound-seq-num (atom 0)
   :converter (atom {})})

(defn payload->fix-msg-vec [{:keys [config outbound-seq-num decoder] :as session}
                            [msg-type payload]]
  (let [seq-num (swap! outbound-seq-num inc)
        header (assoc (:header config)
                      :msg-type msg-type
                      :msg-seq-num seq-num
                      :sending-time (t/instant))
        fix-message {:header header
                     :payload payload}]
    (encode-message decoder fix-message)))

(defn fix-msg-vec->payload [{:keys [decoder]} fix-msg-vec]
  (let [{:keys [header payload]} (decode-message decoder fix-msg-vec)]
    [(:msg-type header) payload]))

