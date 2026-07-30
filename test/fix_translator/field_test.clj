(ns fix-translator.field-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [tick.core :as t]
   [fix-translator.schema :refer [create-decoder]]
   [fix-translator.field :refer [parse-utc-timestamp
                                 format-utc-timestamp
                                 decode-fields
                                 encode-field]]))



(deftest utc-test
  (testing "utc-test"
    (let [i (t/inst "2026-05-26T17:28:09Z")
          s "20250224-21:13:01"]
      (is (inst? (parse-utc-timestamp s)))
      (is (= i (-> i format-utc-timestamp parse-utc-timestamp)))
      (is (= s (-> s parse-utc-timestamp format-utc-timestamp))))))


(def ctrader (create-decoder "fix-specs/ctrader.edn"))

(encode-field ctrader {:name :sending-time 
                       :value (t/inst "2026-05-26T17:28:09Z")})

(deftest utc-timestamp-fields-decode-to-date
  (let [expected (t/inst "2026-05-26T17:28:09.123Z")
        decoded (decode-fields ctrader [["52" "20260526-17:28:09.123"]
                                        ["60" "20260526-17:28:09.123"]
                                        ["122" "20260526-17:28:09.123"]
                                        ["126" "20260526-17:28:09.123"]])]
    (is (= #{:sending-time :transact-time :orig-sending-time :expire-time}
           (set (map :name decoded))))
    (is (every? inst? (map :value decoded)))
    (is (every? #(= expected %) (map :value decoded)))))

(deftest char-enum-conversion-test
  (testing "CHAR enums decode and encode correctly"
    (let [decoded (first (decode-fields ctrader [["39" "0"]]))
          encoded (encode-field ctrader {:name :ord-status :value :new})]
      (is (= :ord-status (:name decoded)))
      (is (= :new (:value decoded)))
      (is (= "39" (:tag encoded)))
      (is (= "0" (:value-str encoded))))))

(deftest msg-type-enum-conversion-test
  (testing "tag 35 (msg-type) decodes and encodes correctly"
    (let [decoded (first (decode-fields ctrader [["35" "0"]]))
          encoded (encode-field ctrader {:name :msg-type :value :heartbeat})]
      (is (= :msg-type (:name decoded)))
      (is (= :heartbeat (:value decoded)))
      (is (= "35" (:tag encoded)))
      (is (= "0" (:value-str encoded))))))

