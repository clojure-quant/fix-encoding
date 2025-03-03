(ns fix-translator.ctrader
  (:require
   [clojure.set :refer [rename-keys]]
   [nano-id.core :refer [nano-id]]
   [fix-translator.fipp :refer [spit-edn]]
   ))

(defn seclist->assets [[_ {:keys [security-request-result no-related-sym] :as sec-list-response}]]
  (when
   (= security-request-result :valid-request)
    (map (fn [asset]
           (rename-keys asset {:symbol-name :asset
                               :symbol-digits :digits
                               :symbol :ctrader})) no-related-sym)))

(defn write-assets [assets]
  (when assets
    (spit-edn "ctrader-assets.edn" assets)))

(defn create-asset-converter [assets]
  {:dict-by-id (->> assets
                    (map (juxt :ctrader :asset))
                    (into {}))
   :dict-by-name (->> assets
                      (map (juxt :asset :ctrader))
                      (into {}))})

(defn get-asset-id [converter asset-name]
  (if-let [asset-id (get-in converter [:dict-by-name asset-name])]
    asset-id
    asset-name))

(defn get-asset-name [converter asset-id]
  (if-let [asset-name (get-in converter [:dict-by-id asset-id])]
    asset-name
    asset-id))

(defn- eventually-add-last-volume [{:keys [bid ask] :as quote}]
  (if (and bid ask)
    (assoc quote :price (/ (+ bid ask) 2.0M)
           :volume 1.0M
           :spread (- ask bid))
    (assoc quote :volume 0.0M)))


(defn ->quote [[msg-type {:keys [symbol no-mdentries]}]]
  (when (= msg-type "W")
    (let [quote (reduce (fn [s {:keys [mdentry-type mdentry-px]}]
                          (assoc s mdentry-type mdentry-px))
                        {} no-mdentries)]
      (-> quote
          (rename-keys {:offer :ask})
          (assoc :asset symbol)
          (eventually-add-last-volume)))))

(defn incoming-quote-id-convert [s quote]
  (update quote :asset #(get-asset-name @(:converter s) %)))
  

(defn subscribe-payload 
  "returns a fix-payload to subscribe for realtime updates 
   for a seq of asset (string)"
  [assets]
  ["V" {:mdreq-id  (nano-id 5)
        :subscription-request-type :snapshot-plus-updates,
        :market-depth 1,
        :mdupdate-type :incremental-refresh,
        :no-mdentry-types [{:mdentry-type :bid} {:mdentry-type :offer}],
        :no-related-sym (->> assets 
                             (map (fn [asset] {:symbol asset}))
                             (into []))}])