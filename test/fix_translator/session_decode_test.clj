(ns fix-translator.session-decode-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.session :refer [create-session fix-msg-vec->payload]]
   [fix-translator.data.message-vec :as msg]))


(def session
  (create-session {:spec "fix-specs/ctrader.edn"
                   :header {:begin-string "FIX.4.4"
                            :target-comp-id "cServer"
                            :sender-comp-id "demo.tradeviewmarkets.3193335"
                            :target-sub-id "QUOTE"
                            :sender-sub-id "QUOTE"}}))



(deftest fix-payload-extractor-t
  (testing "login-response"
    (is (= [:logon {:encrypt-method :none-other
                    :heart-bt-int 60
                    :reset-seq-num-flag "Y"}]
           (fix-msg-vec->payload session msg/login-response))))

  (testing "heartbeat"
    (is (= [:heartbeat {}]
           (fix-msg-vec->payload session msg/heartbeat))))

  (testing "test-request"
    (is (= [:test-request {:test-req-id "TEST"}]
           (fix-msg-vec->payload session msg/test-msg))))

  (testing "seclist-response"
    (let [[msg-type payload] (fix-msg-vec->payload session msg/seclist-response)]
      (is (= :security-list msg-type))
      (is (= {:security-req-id "cTrBh"
              :security-response-id "responce:cTrBh"
              :security-request-result :valid-request}
             (select-keys payload [:security-req-id
                                   :security-response-id
                                   :security-request-result])))
      (is (= 107 (count (:no-related-sym payload))))
      (is (= {:symbol "1" :symbol-name "EURUSD" :symbol-digits :5}
             (first (:no-related-sym payload))))
      (is (= {:symbol "107" :symbol-name "XBR/USD" :symbol-digits :3}
             (last (:no-related-sym payload))))))

  (testing "quote-response"
    (is (= [:market-data-snapshot-full-refresh
            {:symbol "1"
             :no-mdentries [{:mdentry-type :bid
                             :mdentry-px (bigdec "1.03752")}
                            {:mdentry-type :offer
                             :mdentry-px (bigdec "1.03763")}]}]
           (fix-msg-vec->payload session msg/quote-response)))))
