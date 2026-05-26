(ns fix-translator.session-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.session :refer [create-session
                                   payload->fix-msg-vec
                                   fix-msg-vec->payload]]))

(def session
  (create-session {:spec "fix-specs/ctrader.edn"
                   :header {:begin-string "FIX.4.4"
                            :target-comp-id "cServer"
                            :sender-comp-id "demo.tradeviewmarkets.3193335"
                            :target-sub-id "QUOTE"
                            :sender-sub-id "QUOTE"}}))

(deftest payload->fix-msg-vec-keyword-msg-type-test
  (testing "keyword msg-type encodes to wire msg type"
    (let [payload [:logon {:encrypt-method :none-other
                           :heart-bt-int 60
                           :reset-seq-num-flag "Y"
                           :username "3193299"
                           :password "2025Florian"}]
          fix-msg-vec (payload->fix-msg-vec session payload)
          msg-type-field (some #(when (= "35" (first %)) %) fix-msg-vec)]
      (is (= ["35" "A"] msg-type-field))
      (is (= payload
             (fix-msg-vec->payload session fix-msg-vec))))))
