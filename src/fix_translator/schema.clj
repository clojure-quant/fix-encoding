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
  

(defn message-dict [messages]
  (let [msg-type (fn [message]
                   (:msgtype message)
                   ;(-> message :name to-keyword)
                   )]
    (into {}
          (map (juxt msg-type identity) messages))))

(defn create-decoder [filepath]
  (let [schema (load-schema filepath)]
    {:tag->field (build-tag->field schema)
     :header (:header schema)
     :trailer (:trailer schema)
     :messages (-> schema :messages message-dict) ;(:messages schema)
     }))

(defn types-in-spec [decoder]
  (->> decoder
       :tag->field
       vals
       (map :type)
       (into #{})))
