(ns fix-translator.session-roundtrip-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.session :refer [create-session fix-msg-vec->payload payload->fix-msg-vec]]))

(def session
  (create-session {:spec "fix-specs/ctrader.edn"
                   :header {:begin-string "FIX.4.4"
                            :target-comp-id "cServer"
                            :sender-comp-id "demo.tradeviewmarkets.3193335"
                            :target-sub-id "QUOTE"
                            :sender-sub-id "QUOTE"}}))


(defn round-trip [session payload]
  (fix-msg-vec->payload session (payload->fix-msg-vec session payload)))

(deftest encode-msg-t
  (let [s session]
    (testing "login"
      (let [payload [:logon {:encrypt-method :none-other
                              :heart-bt-int 60
                              :reset-seq-num-flag "Y"
                              :username "3193299"
                              :password "2025Florian"}]]
        (is (= payload (round-trip s payload)))))

    (testing "logout"
      (let [payload [:logout {:text "RET_NO_SUCH_LOGIN"}]]
        (is (= payload (round-trip s payload)))))

    (testing "heartbeat"
      (let [payload [:heartbeat {}]]
        (is (= payload (round-trip s payload)))))

    (testing "security-list request"
      (let [payload [:security-list-request {:security-req-id "125"
                                             :security-list-request-type :symbol}]]
        (is (= payload (round-trip s payload)))))

    (testing "market-data subscribe"
      (let [payload [:market-data-request {:mdreq-id "123"
                          :subscription-request-type :snapshot-plus-updates
                          :market-depth 1
                          :mdupdate-type :incremental-refresh
                          :no-mdentry-types [{:mdentry-type :bid}
                                             {:mdentry-type :offer}]
                          :no-related-sym [{:symbol "4"}
                                           {:symbol "1"}]}]]
        (is (= payload (round-trip s payload)))))))
