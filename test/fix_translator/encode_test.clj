(ns fix-translator.encode-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.schema :refer [create-decoder]]
   [fix-translator.message :refer [encode-message decode-message]]
   [fix-translator.data.message-vec :as msg]))

(def ctrader (create-decoder "fix-specs/ctrader.edn"))

(def out-logout-msg
  {:header
   {:target-comp-id "demo.tradeviewmarkets.3152195",
    :sending-time #time/instant "2025-02-24T21:13:01Z",
    :body-length 114,
    :sender-comp-id "CSERVER",
    :msg-seq-num 1,
    :msg-type :logout,
    :begin-string "FIX.4.4",
    :target-sub-id "QUOTE",
    :sender-sub-id "QUOTE"}
   :payload {:text "RET_NO_SUCH_LOGIN"}
   :trailer {:check-sum "222"}})


(defn roundtrip [decoder fix-msg-map]
  (->> fix-msg-map
       (encode-message decoder)
       (decode-message decoder)))

(deftest encode-test
  (testing "logout-response"
    (is (= out-logout-msg (roundtrip ctrader out-logout-msg)))))


;(encode-message ctrader out-logout-msg)
;["52" "20250224-21:13:01"]

