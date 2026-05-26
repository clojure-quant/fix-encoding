(ns fix-translator.ctrader-decode-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.schema :refer [create-decoder]]
   [fix-translator.message :refer [fix->payload]]
   [fix-translator.data.message-vec :as msg]))

(def ctrader (create-decoder "fix-specs/ctrader.edn"))

(deftest fix-payload-extractor-t
  (testing "login-response"
    (is (= ["A" {:encrypt-method :none-other
                 :heart-bt-int 60
                 :reset-seq-num-flag "Y"}]
           (fix->payload ctrader msg/login-response))))

  (testing "heartbeat"
    (is (= ["0" {}]
           (fix->payload ctrader msg/heartbeat))))

  (testing "test-request"
    (is (= ["1" {:test-req-id "TEST"}]
           (fix->payload ctrader msg/test-msg))))

  (testing "seclist-response"
    (let [[msg-type payload] (fix->payload ctrader msg/seclist-response)]
      (is (= "y" msg-type))
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
    (is (= ["W" {:symbol "1"
                 :no-mdentries [{:mdentry-type :bid
                                 :mdentry-px (bigdec "1.03752")}
                                {:mdentry-type :offer
                                 :mdentry-px (bigdec "1.03763")}]}]
           (fix->payload ctrader msg/quote-response)))))
