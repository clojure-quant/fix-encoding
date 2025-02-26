(ns demo.messages)



(def login-msg
"8=FIX.4.49=13935=A49=demo.tradeviewmarkets.319329956=CSERVER34=150=QUOTE57=QUOTE52=20250226-00:02:2698=0108=60141=Y553=3193299554=2025Florian10=011"
)

(def login-msg2
  ; this is an example from ctrader
"8=FIX.4.49=12635=A49=live.theBroker.1234556=CSERVER34=152=20170117-08:03:0457=TRADE50=any_string98=0108=30141=Y553=12345554=passw0rd!10=131"  
  )

(def test-msg 
  "8=FIX.4.19=6135=A34=149=EXEC52=20121105-23:24:0656=BANZAI98=0108=3010=003"
  )

(count "35=A34=149=EXEC52=20121105-23:24:0656=BANZAI98=0108=30")

(def logout-msg
  "8=FIX.4.49=11835=549=CSERVER56=demo.tradeviewmarkets.315219534=150=QUOTE57=QUOTE52=20250224-21:13:01.52558=RET_NO_SUCH_LOGIN10=172")

(def security-list-req
  "8=FIX.4.49=10535=x49=demo.tradeviewmarkets.319329956=CSERVER50=QUOTE57=QUOTE34=252=20250226-00:02:26320=2559=010=074"
  )




(def quote-subscribe-msg
  "8=FIX.4.49=14635=V49=demo.tradeviewmarkets.315219556=CSERVER34=650=QUOTE57=QUOTE52=20250224-21:13:01262=6263=1264=1265=1267=2269=0269=1146=155=410=080")
 

(def new-order-msg "8=FIX.4.29=3335=D55=NESNz54=138=10044=1.010=131")