(ns fix-translator.trade-decode-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [tick.core :as t]
   [fix-translator.session :refer [create-session fix-msg-vec->payload]]))

(def session
  (create-session {:spec "fix-specs/ctrader.edn"
                   :header {:begin-string "FIX.4.4"
                            :target-comp-id "CSERVER"
                            :sender-comp-id "demo.pepperstone.5292473"
                            :target-sub-id "TRADE"
                            :sender-sub-id "TRADE"}}))

(def new-buy-market-msg
  [["8" "FIX.4.4"] ["9" "209"] ["35" "8"] ["34" "2"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260526-23:56:45.607"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["11" "OhgQxlvb"] ["14" "0"] ["37" "340906296"] ["38" "1000"] ["39" "0"] ["40" "1"]
   ["54" "1"] ["55" "1"] ["59" "3"] ["60" "20260526-23:56:45.605"] ["150" "0"] ["151" "1000"]
   ["721" "221427182"] ["10" "107"]])

(def new-sell-market-msg
  [["8" "FIX.4.4"] ["9" "209"] ["35" "8"] ["34" "3"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260526-23:56:45.607"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["11" "YTvPAHlH"] ["14" "0"] ["37" "340906297"] ["38" "1000"] ["39" "0"] ["40" "1"]
   ["54" "2"] ["55" "2"] ["59" "3"] ["60" "20260526-23:56:45.605"] ["150" "0"] ["151" "1000"]
   ["721" "221427183"] ["10" "245"]])

(def new-buy-limit-msg
  [["8" "FIX.4.4"] ["9" "216"] ["35" "8"] ["34" "4"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260526-23:56:45.608"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["11" "lcyaIzOF"] ["14" "0"] ["37" "340906298"] ["38" "1000"] ["39" "0"] ["40" "2"]
   ["44" "150"] ["54" "1"] ["55" "3"] ["59" "1"] ["60" "20260526-23:56:45.606"] ["150" "0"]
   ["151" "1000"] ["721" "221427184"] ["10" "132"]])

(def new-sell-limit-msg
  [["8" "FIX.4.4"] ["9" "216"] ["35" "8"] ["34" "5"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260526-23:56:45.608"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["11" "Kcx98EXM"] ["14" "0"] ["37" "340906299"] ["38" "1000"] ["39" "0"] ["40" "2"]
   ["44" "160"] ["54" "2"] ["55" "4"] ["59" "1"] ["60" "20260526-23:56:45.606"] ["150" "0"]
   ["151" "1000"] ["721" "221427185"] ["10" "010"]])

(def fill-sell-msg
  [["8" "FIX.4.4"] ["9" "227"] ["35" "8"] ["34" "6"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260526-23:56:45.822"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["6" "1.34551"] ["11" "YTvPAHlH"] ["14" "1000"] ["32" "1000"] ["37" "340906297"]
   ["38" "1000"] ["39" "2"] ["40" "1"] ["54" "2"] ["55" "2"] ["59" "3"]
   ["60" "20260526-23:56:45.810"] ["150" "F"] ["151" "0"] ["721" "221427183"] ["10" "070"]])

(def fill-buy-msg
  [["8" "FIX.4.4"] ["9" "226"] ["35" "8"] ["34" "7"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260526-23:56:45.863"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["6" "1.1638"] ["11" "OhgQxlvb"] ["14" "1000"] ["32" "1000"] ["37" "340906296"]
   ["38" "1000"] ["39" "2"] ["40" "1"] ["54" "1"] ["55" "1"] ["59" "3"]
   ["60" "20260526-23:56:45.856"] ["150" "F"] ["151" "0"] ["721" "221427182"] ["10" "156"]])

(def heartbeat-msg
  [["8" "FIX.4.4"] ["9" "92"] ["35" "0"] ["34" "8"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260526-23:57:46.306"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["10" "086"]])

(def business-reject-msg
  [["8" "FIX.4.4"] ["9" "169"] ["35" "j"] ["34" "6"] ["49" "CSERVER"] ["50" "TRADE"]
   ["52" "20260606-23:33:39.595"] ["56" "demo.pepperstone.5292473"] ["57" "TRADE"]
   ["58" "MARKET_CLOSED:Trading is not available: Market is closed."]
   ["379" "fix-4"] ["380" "0"] ["10" "149"]])

(deftest trade-decode-test
  (testing "new market buy execution report"
    (is (= [:execution-report
            {:cl-ord-id "OhgQxlvb"
             :cum-qty 0M
             :order-id "340906296"
             :order-qty 1000M
             :ord-status :new
             :ord-type :market
             :side :buy
             :symbol "1"
             :time-in-force :immediate-or-cancel
             :transact-time (t/instant "2026-05-26T23:56:45.605Z")
             :exec-type :new
             :leaves-qty 1000M
             :pos-maint-rpt-id "221427182"}]
           (fix-msg-vec->payload session new-buy-market-msg))))

  (testing "new market sell execution report"
    (is (= [:execution-report
            {:cl-ord-id "YTvPAHlH"
             :cum-qty 0M
             :order-id "340906297"
             :order-qty 1000M
             :ord-status :new
             :ord-type :market
             :side :sell
             :symbol "2"
             :time-in-force :immediate-or-cancel
             :transact-time (t/instant "2026-05-26T23:56:45.605Z")
             :exec-type :new
             :leaves-qty 1000M
             :pos-maint-rpt-id "221427183"}]
           (fix-msg-vec->payload session new-sell-market-msg))))

  (testing "new limit buy execution report"
    (is (= [:execution-report
            {:cl-ord-id "lcyaIzOF"
             :cum-qty 0M
             :order-id "340906298"
             :order-qty 1000M
             :ord-status :new
             :ord-type :limit
             :price 150M
             :side :buy
             :symbol "3"
             :time-in-force :good-till-cancel
             :transact-time (t/instant "2026-05-26T23:56:45.606Z")
             :exec-type :new
             :leaves-qty 1000M
             :pos-maint-rpt-id "221427184"}]
           (fix-msg-vec->payload session new-buy-limit-msg))))

  (testing "new limit sell execution report"
    (is (= [:execution-report
            {:cl-ord-id "Kcx98EXM"
             :cum-qty 0M
             :order-id "340906299"
             :order-qty 1000M
             :ord-status :new
             :ord-type :limit
             :price 160M
             :side :sell
             :symbol "4"
             :time-in-force :good-till-cancel
             :transact-time (t/instant "2026-05-26T23:56:45.606Z")
             :exec-type :new
             :leaves-qty 1000M
             :pos-maint-rpt-id "221427185"}]
           (fix-msg-vec->payload session new-sell-limit-msg))))

  (testing "fill sell execution report"
    (is (= [:execution-report
            {:avg-px 1.34551M
             :cl-ord-id "YTvPAHlH"
             :cum-qty 1000M
             :last-qty 1000M
             :order-id "340906297"
             :order-qty 1000M
             :ord-status :filled
             :ord-type :market
             :side :sell
             :symbol "2"
             :time-in-force :immediate-or-cancel
             :transact-time (t/instant "2026-05-26T23:56:45.810Z")
             :exec-type :trade
             :leaves-qty 0M
             :pos-maint-rpt-id "221427183"}]
           (fix-msg-vec->payload session fill-sell-msg))))

  (testing "fill buy execution report"
    (is (= [:execution-report
            {:avg-px 1.1638M
             :cl-ord-id "OhgQxlvb"
             :cum-qty 1000M
             :last-qty 1000M
             :order-id "340906296"
             :order-qty 1000M
             :ord-status :filled
             :ord-type :market
             :side :buy
             :symbol "1"
             :time-in-force :immediate-or-cancel
             :transact-time (t/instant "2026-05-26T23:56:45.856Z")
             :exec-type :trade
             :leaves-qty 0M
             :pos-maint-rpt-id "221427182"}]
           (fix-msg-vec->payload session fill-buy-msg))))

  (testing "trade session heartbeat"
    (is (= [:heartbeat {}]
           (fix-msg-vec->payload session heartbeat-msg))))

  (testing "business message reject with text before ref-id (cTrader wire order)"
    (is (= [:business-message-reject
            {:text "MARKET_CLOSED:Trading is not available: Market is closed."
             :business-reject-ref-id "fix-4"
             :business-reject-reason :other}]
           (fix-msg-vec->payload session business-reject-msg)))))
