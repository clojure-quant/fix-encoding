(ns fix-translator.xml-convert
  (:require
   [clojure.data.xml :as xml]
   [clojure.zip :as zip]
   [clojure.java.io :as io]))

;; Parse XML file
(defn parse-xml [file-path]
  (xml/parse (io/input-stream file-path)))

;; Create zipper for XML traversal
(defn xml-zipper [xml-doc]
  (zip/xml-zip xml-doc))

;; Extract fields from header, trailer, or group
(defn extract-fields [nodes]
  (->> nodes
       (filter #(= :field (:tag %)))
       (map (fn [field]
              {:name (get-in field [:attrs :name])
               :required   (get-in field [:attrs :required])}))))

;; Extract header fields
(defn extract-header [z]
  (loop [loc z]
    (if (zip/end? loc)
      []
      (let [node (zip/node loc)]
        (if (= :header (:tag node))
          (extract-fields (:content node))
          (recur (zip/next loc)))))))

;; Extract trailer fields
(defn extract-trailer [z]
  (loop [loc z]
    (if (zip/end? loc)
      []
      (let [node (zip/node loc)]
        (if (= :trailer (:tag node))
          (extract-fields (:content node))
          (recur (zip/next loc)))))))

;; Extract fields and groups in order for a message
(defn extract-fields-groups [nodes]
  (map (fn [node]
         (case (:tag node)
           :field {:type        :field
                   :name  (get-in node [:attrs :name])
                   :required    (get-in node [:attrs :required])}
           :group {:type        :group
                   :name  (get-in node [:attrs :name])
                   :required    (get-in node [:attrs :required])
                   :fields      (extract-fields (:content node))}
           nil))
        ;; Skip nodes that are neither field nor group
       (filter #(or (= :field (:tag %)) (= :group (:tag %))) nodes)))

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
                                  :content (extract-fields-groups (:content node))})
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

;; Main function to parse and extract all data
(defn extract-fix-data [file-path]
  (let [parsed-xml (parse-xml file-path)
        z (xml-zipper parsed-xml)]
    {:header   (extract-header z)
     :trailer  (extract-trailer z)
     :messages (extract-fix-message-types z)
     :fields   (extract-fix-fields z)}))


