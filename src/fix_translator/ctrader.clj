(ns fix-translator.ctrader
  (:require
   [clojure.set :refer [rename-keys]]
   [fix-translator.fipp :refer [spit-edn]]
   ))

(defn write-assets [[_ {:keys [security-request-result no-related-sym] :as sec-list-response}]]
  (when
   (= security-request-result :valid-request)
    (let [assets (map (fn [asset]
                        (rename-keys asset {:symbol-name :asset
                                            :symbol-digits :digits
                                            :symbol :ctrader})) no-related-sym)]
      (spit-edn "ctrader-assets.edn" assets))))

(defn- eventually-add-last-volume [{:keys [bid ask] :as quote}]
  (if (and bid ask)
    (assoc quote :last (/ (+ bid ask) 2.0M)
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