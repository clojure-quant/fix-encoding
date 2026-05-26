(ns fix-translator.field-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [tick.core :as t]
   [fix-translator.schema :refer [create-decoder]]
   [fix-translator.field :refer [parse-utc-timestamp format-utc-timestamp encode-field]]))



(deftest utc-test
  (testing "utc-test"
    (let [i (t/instant "2026-05-26T17:28:09Z")
          s "20250224-21:13:01"]
      (is (= i (-> i format-utc-timestamp parse-utc-timestamp)))
      (is (= s (-> s parse-utc-timestamp format-utc-timestamp))))))


(def ctrader (create-decoder "fix-specs/ctrader.edn"))

(encode-field ctrader {:name :sending-time 
                       :value (t/instant "2026-05-26T17:28:09Z")})

