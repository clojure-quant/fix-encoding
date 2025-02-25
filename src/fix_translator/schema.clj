(ns fix-translator.schema
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]))

#_(defn load-schema [filepath]
  (with-open [rdr (io/reader filepath)]
    (edn/read (java.io.PushbackReader. rdr))))

(defn load-schema [resource-path]
  (with-open [rdr (io/reader (io/resource resource-path))]
    (edn/read (java.io.PushbackReader. rdr))))

(defn build-tag->field [schema]
  (->> (:fields schema)
       (map (juxt :tag identity))
       (into {})))
  
(defn build-name->field [schema]
  (->> (:fields schema)
       (map (juxt :name identity))
       (into {})))
  

(defn msg-type->message [schema]
  (->> (:messages schema )
       (map (juxt :msgtype identity))
       ;(map (juxt :name identity))
       (into {})))

(defn create-decoder [resource-path]
  (let [schema (load-schema resource-path)]
    {:tag->field (build-tag->field schema)
     :name->field (build-name->field schema)
     :header (:header schema)
     :trailer (:trailer schema)
     :messages (msg-type->message schema)
     }))

(defn get-field [{:keys [tag->field] :as _decoder} tag] 
  (if-let [field (get tag->field tag)]
    field
    (throw (ex-info "fix-encoding - unknown field-tag" {:tag tag}))))

(defn get-field-by-name [{:keys [name->field] :as _decoder} field-name]
  (if-let [field (get name->field field-name)]
    field
    (throw (ex-info "fix-encoding - unknown field-name" {:field-name field-name}))))

(defn get-msg-type [{:keys [messages] :as _decoder} msg-type] 
  (if-let [msg (get messages msg-type)]
     msg
     (throw (ex-info "fix-encoding - unknown message-type" {:msg-type msg-type}))))

(defn types-in-spec [decoder]
  (->> decoder
       :tag->field
       vals
       (map :type)
       (into #{})))
