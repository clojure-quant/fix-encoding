(ns fix-translator.xml-convert
  (:require
   [clojure.string :as str]
   [clojure.data.xml :as xml]
   [clojure.zip :as zip]
   [clojure.java.io :as io]))

(defn to-keyword [s]
  (let [k (-> s
              (str/replace #"(?<=[a-z0-9])([A-Z])" "-$1") ;; Insert hyphen before uppercase if preceded by lowercase/number
              str/lower-case                              ;; Convert to lowercase
              (str/replace #"_" "-") ;for enums like "PARTIALLY_FILLED"
              keyword            ;; Convert to keyword
              )]
    ;(println "s: " s " kw: " k)
    k))

;(to-keyword "BodyLength")     ;; => :body-length
;(to-keyword "SenderCompID")   ;; => :sender-comp-id
;(to-keyword "PARTIALLY_FILLED")


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
               :required  (= "Y" (get-in field [:attrs :required]))}))))

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
                 (let [field {:tag (get-in node [:attrs :number])
                              :name   (get-in node [:attrs :name])
                              :type   (get-in node [:attrs :type])
                              :values (map (fn [v]
                                             {:enum        (get-in v [:attrs :enum])
                                              :description (get-in v [:attrs :description])})
                                           (filter #(= :value (:tag %)) (:content node)))}]
                   (conj fields field))
                 fields))))))

;; Main function to parse and extract all data

(defn keywordize-name [item]
  (update item :name to-keyword))

(defn keywordize-names [items]
  (map keywordize-name items))

(defn sanitize-message-group-fields [content]
  ;(println "content: " content)
  (->> content
       (map (fn [{:keys [fields] :as item}]
              ;(println "item: " item)
              (if fields
                (let [;_ (println "group fields: " fields)
                      new-fields (keywordize-names fields)
                      new-fields (into [] new-fields)
                      ]
                  ;(println "converted fields: " new-fields)
                  (assoc item :fields new-fields))
                  ; dont replace
                item)))
       (into [])))

(defn sanitize-message [{:keys [name content] :as msg-type}]
  (-> msg-type
      (update :content keywordize-names)
      (update :content sanitize-message-group-fields)
      keywordize-name))

(defn keywordize-description [item]
  (update item :description to-keyword))

(defn keywordize-descriptions [items]
  (map keywordize-description items))

 ;:values ({:enum "0", :description "HEARTBEAT"}

(defn sanitize-field [{:keys [name content] :as field-type}]
  (-> field-type
      (update :values keywordize-descriptions)
      keywordize-name))

(defn extract-fix-data [file-path]
  (let [parsed-xml (parse-xml file-path)
        z (xml-zipper parsed-xml)
        header (extract-header z)
        trailer (extract-trailer z)
        messages (extract-fix-message-types z)
        fields (extract-fix-fields z)]
    {:header   (keywordize-names header)
     :trailer (keywordize-names trailer)
     :messages (map sanitize-message messages)
     :fields   (map sanitize-field fields)}))



(map keywordize-name
     [{:name "BeginString", :required true}
      {:name "BodyLength", :required true}])

