(ns fix-translator.ctrader-encode-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.session :refer [load-accounts create-session encode-msg decode-msg]]))

(defn session []
  (-> (load-accounts "fix-accounts.edn")
      (create-session :ctrader-tradeviewmarkets-quote)))

(defn round-trip [session payload]
  (decode-msg session (encode-msg session payload)))

(deftest encode-msg-t
  (let [s (session)]
    (testing "login"
      (let [payload ["A" {:encrypt-method :none-other
                           :heart-bt-int 60
                           :reset-seq-num-flag "Y"
                           :username "3193299"
                           :password "2025Florian"}]]
        (is (= payload (round-trip s payload)))))

    (testing "logout"
      (let [payload ["5" {:text "RET_NO_SUCH_LOGIN"}]]
        (is (= payload (round-trip s payload)))))

    (testing "heartbeat"
      (let [payload ["0" {}]]
        (is (= payload (round-trip s payload)))))

    (testing "security-list request"
      (let [payload ["x" {:security-req-id "125"
                          :security-list-request-type :symbol}]]
        (is (= payload (round-trip s payload)))))

    (testing "market-data subscribe"
      (let [payload ["V" {:mdreq-id "123"
                          :subscription-request-type :snapshot-plus-updates
                          :market-depth 1
                          :mdupdate-type :incremental-refresh
                          :no-mdentry-types [{:mdentry-type :bid}
                                             {:mdentry-type :offer}]
                          :no-related-sym [{:symbol "4"}
                                          {:symbol "1"}]}]]
        (is (= payload (round-trip s payload)))))))
