(ns demo.xml-convert
  (:require 
   [fix-translator.xml-convert :refer [extract-fix-data]]
   [fix-translator.fipp :refer [spit-edn]]))



;; Example usage
(def fix-data (extract-fix-data "resources/fix-specs/ctrader/FIX44-CSERVER.xml"))

;; Print header fields
(println "FIX Header Fields:")
(doseq [{:keys [name required]} (:header fix-data)]
  (println (format "  Field: %s, Required: %s" name required)))

;; Print trailer fields
(println "\nFIX Trailer Fields:")
(doseq [{:keys [name required]} (:trailer fix-data)]
  (println (format "  Field: %s, Required: %s" name required)))

;; Print messages with ordered fields and groups
(println "\nFIX Messages with Ordered Fields and Groups:")
(doseq [{:keys [name msgtype category content]} (:messages fix-data)]
  (println (format "\nMessage: %s, MsgType: %s, Category: %s" name msgtype category))

  ;; Print ordered fields and groups
  (doseq [item content]
    (case (:type item)
      :field (println (format "  Field: %s, Required: %s" (:name item) (:required item)))
      :group (do
               (println (format "  Group: %s, Required: %s" (:name item) (:required item)))
               (doseq [{:keys [name required]} (:fields item)]
                 (println (format "    Field: %s, Required: %s" name required)))))))

;; Print global fields with value enums
(println "\nFIX Fields Vector:")
(doseq [{:keys [number name type values]} (:fields fix-data)]
  (println (format "Field: %s (%s), Type: %s" name number type))
  (when (seq values)
    (println "  Values:")
    (doseq [{:keys [enum description]} values]
      (println (format "    Enum: %s, Description: %s" enum description)))))




(spit-edn "resources/fix-specs/ctrader.edn"  fix-data)

