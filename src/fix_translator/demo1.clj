(ns fix-translator.demo1
  (:require
   [clojure.data.xml :as xml]
   [clojure.zip :as zip]
   [clojure.java.io :as io]
   [fix-translator.fipp :refer [spit-edn]]))

;; Parse XML file
 (defn parse-xml [file-path]
   (xml/parse (io/input-stream file-path)))

;; Create zipper for XML traversal
 (defn xml-zipper [xml-doc]
   (zip/xml-zip xml-doc))

;; Extract fields and groups in order for a message
 (defn extract-fields-groups [nodes]
   (map (fn [node]
          (case (:tag node)
            :field {:type        :field
                    :field-name  (get-in node [:attrs :name])
                    :required    (get-in node [:attrs :required])}
            :group {:type        :group
                    :group-name  (get-in node [:attrs :name])
                    :required    (get-in node [:attrs :required])
                    :fields      (map (fn [field]
                                        {:field-name (get-in field [:attrs :name])
                                         :required   (get-in field [:attrs :required])})
                                      (filter #(= :field (:tag %)) (:content node)))}
            nil))
     (filter #(or (= :field (:tag %)) (= :group (:tag %))) nodes))      ;; Skip nodes that are neither field nor group
        
        ) 
      

;; Extract all message types with ordered fields and groups
(defn extract-fix-message-types [z]
  (loop [loc z
         msg-types []]
    (if (zip/end? loc)
      msg-types
      (let [node (zip/node loc)]
        (recur (zip/next loc)
               (if (and (= :message (:tag node))
                        (:attrs node))
                 (conj msg-types {:name          (get-in node [:attrs :name])
                                  :msgtype       (get-in node [:attrs :msgtype])
                                  :category      (get-in node [:attrs :msgcat])
                                  :fields-groups (extract-fields-groups (:content node))})
                 msg-types))))))

;; Extract global fields with full value details (enum + description)
(defn extract-fix-fields [z]
  (loop [loc z
         fields []]
    (if (zip/end? loc)
      fields
      (let [node (zip/node loc)]
        (recur (zip/next loc)
               (if (and (= :field (:tag node))
                        (:attrs node)
                        (get-in node [:attrs :number])
                        (get-in node [:attrs :type]))
                 (let [field {:number (get-in node [:attrs :number])
                              :name   (get-in node [:attrs :name])
                              :type   (get-in node [:attrs :type])
                              :values (map (fn [v]
                                             {:enum        (get-in v [:attrs :enum])
                                              :description (get-in v [:attrs :description])})
                                           (filter #(= :value (:tag %)) (:content node)))}]
                   (conj fields field))
                 fields))))))

;; Main function to parse and extract data
(defn extract-fix-data [file-path]
  (let [parsed-xml (parse-xml file-path)
        z (xml-zipper parsed-xml)]
    {:messages (extract-fix-message-types z)
     :fields   (extract-fix-fields z)}))

;; Example usage
(def fix-data (extract-fix-data "resources/fix-specs/ctrader/FIX44-CSERVER.xml"))

;; Print messages with ordered fields and groups
(println "FIX Messages with Ordered Fields and Groups:")
(doseq [{:keys [name msgtype category fields-groups]} (:messages fix-data)]
  (println (format "\nMessage: %s, MsgType: %s, Category: %s" name msgtype category))

  ;; Print ordered fields and groups
  (doseq [item fields-groups]
    (case (:type item)
      :field (println (format "  Field: %s, Required: %s" (:field-name item) (:required item)))
      :group (do
               (println (format "  Group: %s, Required: %s" (:group-name item) (:required item)))
               (doseq [{:keys [field-name required]} (:fields item)]
                 (println (format "    Field: %s, Required: %s" field-name required)))))))

;; Print global fields with value enums
(println "\nFIX Fields Vector:")
(doseq [{:keys [number name type values]} (:fields fix-data)]
  (println (format "Field: %s (%s), Type: %s" name number type))
  (when (seq values)
    (println "  Values:")
    (doseq [{:keys [enum description]} values]
      (println (format "    Enum: %s, Description: %s" enum description)))))



(spit-edn "ctrader.edn"  fix-data)