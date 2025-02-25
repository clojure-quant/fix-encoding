(ns fix-translator.schema
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]))


(defn load-schema [filepath]
  (with-open [rdr (io/reader filepath)]
    (edn/read (java.io.PushbackReader. rdr))))


(defn build-tag-map [schema]
  (into {} (map (fn [{:keys [tag name type values]}]
                  [tag {:name name :type type :values values}])
                (:fields schema))))

(defn message-dict [messages]
  (let [msg-type (fn [message]
                   (:msgtype message)
                   ;(-> message :name to-keyword)
                   )]
    (into {}
          (map (juxt msg-type identity) messages))))

(defn create-decoder [filepath]
  (let [schema (load-schema filepath)
        tag-map (build-tag-map schema)]
    {:fields tag-map
     :header (:header schema)
     :trailer (:trailer schema)
     :messages (-> schema :messages message-dict) ;(:messages schema)
     }))

(defn types-in-spec [decoder]
  (->> decoder
       :fields
       vals
       (map :type)
       (into #{})))
