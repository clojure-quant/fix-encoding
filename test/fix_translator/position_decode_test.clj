(ns fix-translator.position-decode-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [fix-translator.session :refer [create-session fix-msg-vec->payload]]))

(def session
  (create-session {:spec "fix-specs/ctrader.edn"
                   :header {:begin-string "FIX.4.4"
                            :target-comp-id "CSERVER"
                            :sender-comp-id "demo.pepperstone.5292473"
                            :target-sub-id "TRADE"
                            :sender-sub-id "TRADE"}}))

(def long-position-msg
  [["8" "FIX.4.4"] ["9" "167"] ["35" "AP"] ["34" "8"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260527-00:17:42.737"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["55" "1"] ["710" "GQgHl"] ["721" "221436915"] ["727" "4"] ["728" "0"] ["730" "1.16395"]
   ["702" "1"] ["704" "1000"] ["705" "0"] ["10" "253"]])

(def short-position-msg
  [["8" "FIX.4.4"] ["9" "167"] ["35" "AP"] ["34" "9"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260527-00:17:42.737"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["55" "2"] ["710" "GQgHl"] ["721" "221436916"] ["727" "4"] ["728" "0"] ["730" "1.34561"]
   ["702" "1"] ["704" "0"] ["705" "1000"] ["10" "251"]])

(def long-position-msg2
  [["8" "FIX.4.4"] ["9" "167"] ["35" "AP"] ["34" "10"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260527-00:17:42.737"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["55" "1"] ["710" "GQgHl"] ["721" "221427182"] ["727" "4"] ["728" "0"] ["730" "1.1638"]
   ["702" "1"] ["704" "1000"] ["705" "0"] ["10" "236"]])

(def short-position-msg2
  [["8" "FIX.4.4"] ["9" "168"] ["35" "AP"] ["34" "11"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260527-00:17:42.737"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["55" "2"] ["710" "GQgHl"] ["721" "221427183"] ["727" "4"] ["728" "0"] ["730" "1.34551"]
   ["702" "1"] ["704" "0"] ["705" "1000"] ["10" "032"]])

(deftest position-decode-test
  (testing "long position report"
    (is (= [:position-report
            {:symbol "1"
             :pos-req-id "GQgHl"
             :pos-maint-rpt-id "221436915"
             :total-num-pos-reports 4
             :pos-req-result :valid-request
             :settl-price 1.16395M
             :no-positions [{:long-qty 1000M :short-qty 0M}]}]
           (fix-msg-vec->payload session long-position-msg))))

  (testing "short position report"
    (is (= [:position-report
            {:symbol "2"
             :pos-req-id "GQgHl"
             :pos-maint-rpt-id "221436916"
             :total-num-pos-reports 4
             :pos-req-result :valid-request
             :settl-price 1.34561M
             :no-positions [{:long-qty 0M :short-qty 1000M}]}]
           (fix-msg-vec->payload session short-position-msg))))

  (testing "long position report (existing position id)"
    (is (= [:position-report
            {:symbol "1"
             :pos-req-id "GQgHl"
             :pos-maint-rpt-id "221427182"
             :total-num-pos-reports 4
             :pos-req-result :valid-request
             :settl-price 1.1638M
             :no-positions [{:long-qty 1000M :short-qty 0M}]}]
           (fix-msg-vec->payload session long-position-msg2))))

  (testing "short position report (existing position id)"
    (is (= [:position-report
            {:symbol "2"
             :pos-req-id "GQgHl"
             :pos-maint-rpt-id "221427183"
             :total-num-pos-reports 4
             :pos-req-result :valid-request
             :settl-price 1.34551M
             :no-positions [{:long-qty 0M :short-qty 1000M}]}]
           (fix-msg-vec->payload session short-position-msg2)))))
