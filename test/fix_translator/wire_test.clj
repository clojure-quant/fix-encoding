(ns fix-translator.wire-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.message-wire :refer [vec->wire wire->vec]]
   [fix-translator.data.message-wire :as msg-wire]
   [fix-translator.data.message-vec :as msg-vec]))


(defn roundtrip [fix-vec-msg]
  (-> fix-vec-msg
      vec->wire
      wire->vec))

(deftest wire-encode-test
  (testing "login-msg"
    (is (= msg-vec/login-response (roundtrip msg-vec/login-response))))
  (testing "heartbeat"
    (is (= msg-vec/heartbeat (roundtrip msg-vec/heartbeat))))
  (testing "test-request"
    (is (= msg-vec/test-msg (roundtrip msg-vec/test-msg))))
  (testing "seclist-response"
    (is (= msg-vec/seclist-response (roundtrip msg-vec/seclist-response))))
  (testing "quote-response"
    (is (= msg-vec/quote-response (roundtrip msg-vec/quote-response)))))


(defn roundtrip-wire [fix-wire-msg]
  (-> fix-wire-msg
      wire->vec
      vec->wire))

(deftest wire-encode-test
  (testing "login-msg"
    (is (= msg-wire/login-msg (roundtrip-wire msg-wire/login-msg))))
  (testing "login-msg2"
    (is (= msg-wire/login-msg2 (roundtrip-wire msg-wire/login-msg2))))
  (testing "test-msg"
    (is (= msg-wire/test-msg (roundtrip-wire msg-wire/test-msg))))
  (testing "test-request"
    (is (= msg-wire/logout-msg (roundtrip-wire msg-wire/logout-msg))))
  (testing "seclist-response"
    (is (= msg-wire/security-list-req (roundtrip-wire msg-wire/security-list-req))))
  (testing "quote-response"
    (is (= msg-wire/quote-subscribe-msg (roundtrip-wire msg-wire/quote-subscribe-msg))))
  (testing "new-order-msg"
    (is (= msg-wire/new-order-msg (roundtrip-wire msg-wire/new-order-msg)))))