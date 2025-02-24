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

;; Extract fields from a specific message
(defn extract-message-fields [message-node]
  (let [content (:content message-node)]
    (->> content
         (filter #(= :field (:tag %)))
         (map (fn [field]
                {:field-name (get-in field [:attrs :name])
                 :required   (get-in field [:attrs :required])})))))

;; Fixed extraction of global fields
(defn extract-fix-fields [z]
  (loop [loc z
         fields []]
    (if (zip/end? loc)
      fields
      (let [node (zip/node loc)]
        (recur (zip/next loc)
               (if (and (= :field (:tag node))
                        (:attrs node)
                        (get-in node [:attrs :number])  ;; Ensure field has a number
                        (get-in node [:attrs :type]))   ;; Ensure field has a type
                 (let [field {:number (get-in node [:attrs :number])
                              :name   (get-in node [:attrs :name])
                              :type   (get-in node [:attrs :type])
                              :values (map #(get-in % [:attrs :description])
                                           (filter #(= :value (:tag %)) (:content node)))}]
                   (conj fields field))
                 fields))))))

;; Extract all message types with their fields
(defn extract-fix-message-types [z]
  (loop [loc z
         msg-types []]
    (if (zip/end? loc)
      msg-types
      (let [node (zip/node loc)]
        (recur (zip/next loc)
               (if (and (= :message (:tag node))
                        (:attrs node))
                 (conj msg-types {:name     (get-in node [:attrs :name])
                                  :msgtype  (get-in node [:attrs :msgtype])
                                  :category (get-in node [:attrs :msgcat])
                                  :fields   (extract-message-fields node)})
                 msg-types))))))

;; Main function to parse and extract data
(defn extract-fix-data [file-path]
  (let [parsed-xml (parse-xml file-path)
        z (xml-zipper parsed-xml)]
    {:messages (extract-fix-message-types z)
     :fields   (extract-fix-fields z)}))

;; Example usage
(def fix-data (extract-fix-data "resources/fix-specs/ctrader/FIX44-CSERVER.xml"))

;; Print messages with their fields
(println "FIX Messages with Fields:")
(doseq [{:keys [name msgtype category fields]} (:messages fix-data)]
  (println (format "\nMessage: %s, MsgType: %s, Category: %s" name msgtype category))
  (doseq [{:keys [field-name required]} fields]
    (println (format "  Field: %s, Required: %s" field-name required))))

;; Print global fields
(println "\nFIX Fields Vector:")
(doseq [{:keys [number name type values]} (:fields fix-data)]
  (println (format "Field: %s (%s), Type: %s, Values: %s"
                   name number type (if (seq values) (clojure.string/join ", " values) "N/A"))))


(spit-edn "ctrader.edn"  fix-data)