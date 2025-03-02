(ns fix-translator.ctrader
  (:require
   [clojure.set :refer [rename-keys]]
   [fix-translator.fipp :refer [spit-edn]]))

(defn write-assets [[_ {:keys [security-request-result no-related-sym] :as sec-list-response}]]
  (when
   (= security-request-result :valid-request)
    (let [assets (map (fn [asset]
                        (rename-keys asset {:symbol-name :asset
                                            :symbol-digits :digits
                                            :symbol :ctrader})) no-related-sym)]
      (spit-edn "ctrader-assets.edn" assets))))


