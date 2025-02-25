(ns fix-translator.schema
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]))

(defn load-schema [filepath]
  (with-open [rdr (io/reader filepath)]
    (edn/read (java.io.PushbackReader. rdr))))


(defn build-tag->field [schema]
  (->> (:fields schema)
       (map (juxt :tag identity))
       (into {})))
  

(defn msg-type->message [schema]
  (->> (:messages schema )
       (map (juxt :msgtype identity))
       ;(map (juxt :name identity))
       (into {})))

(defn create-decoder [filepath]
  (let [schema (load-schema filepath)]
    {:tag->field (build-tag->field schema)
     :header (:header schema)
     :trailer (:trailer schema)
     :messages (msg-type->message schema)
     }))

(defn get-field [{:keys [tag->field] :as _decoder} tag] 
  (if-let [field (get tag->field tag)]
    field
    (throw (ex-info "fix-encoding - unknown field-tag" {:tag tag}))))

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
