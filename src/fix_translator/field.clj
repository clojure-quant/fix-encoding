(ns fix-translator.field
  (:require
   [clojure.string :as s]
   [tick.core :as t]
   [cljc.java-time.local-date-time :as ldt]))

(defn to-keyword [s]
  (let [k (-> s
              (s/replace #"(?<=[a-z0-9])([A-Z])" "-$1") ;; Insert hyphen before uppercase if preceded by lowercase/number
              s/lower-case                              ;; Convert to lowercase
              keyword            ;; Convert to keyword
              )]
    ;(println "s: " s " kw: " k)
    k))


;; tag=value pairs separated by \u0001 (SOH),  tag can be 1-2 alphanumeric characters
(def tag-value-regex #"([A-Za-z0-9]{1,4})=([^\u0001]+)")

;; Parse FIX message into list of tag-value maps
(defn ->tag-value-pairs [msg]
  (map (fn [[_ tag value]]
         {:tag tag
          :value value})
       (re-seq tag-value-regex msg)))

(defn parse-utc-timestamp [s]
  (let [;cleaned (s/replace s #"\.(\d{1,3})$" (fn [[_ ms]] (format ".%03d" (Integer/parseInt ms)))) ;; Normalize milliseconds
        cleaned s
        formats ["yyyyMMdd-HH:mm:ss.SSS" "yyyyMMdd-HH:mm:ss"]
        ldt (some (fn [fmt]
                    (try
                      (println "fmt: " fmt)
                      (ldt/parse cleaned (t/formatter fmt))
                      (catch Exception _ nil)))
                  formats)]
    (-> ldt
        (t/in "UTC")              ;; Convert to UTC timezone
        t/instant)))

;; Example usage:
;(parse-utc-timestamp "20250224-21:13:01.525") ;; => #inst "2025-02-24T21:13:01.525Z"
;(parse-utc-timestamp "20250224-21:13:01")     ;; => #inst "2025-02-24T21:13:01Z"


;(to-keyword "BodyLength")     ;; => :body-length
;(to-keyword "SenderCompID")   ;; => :sender-comp-id

(defn decode-value [{:keys [name type values] :as _field} value]
  ;(println "converting tag: " name  "type: " type " value: " value "values: " values)
  (let [parser {"LOCALMKTDATE" identity ; todo
                "SEQNUM" parse-long
                "LENGTH" parse-long
                "QTY" bigdec
                "STRING" identity
                "INT" parse-long
                "PRICE" bigdec
                "CHAR" identity
                "NUMINGROUP" parse-long
                "DATA" identity ; todo
                "BOOLEAN" identity ; todo
                "UTCTIMESTAMP" parse-utc-timestamp ; identity ; todo
                }]
    (cond
      ; do not change msgtype
      (= name "MsgType")
      value
      ; enums
      (seq values)
      (some #(when (= (:enum %) value)
               (to-keyword (:description %))) values)
      ; parse
      :else
      (if-let [parse-fn (get parser type)]
        (parse-fn value)
        value))))

(defn decode-fields [{:keys [tag->field]} fix-msg-str]
  (->> fix-msg-str
       (->tag-value-pairs)
       (map (fn [{:keys [tag value] :as entry}]
              (let [{:keys [name _values type] :as field} (get tag->field tag {:name "Unknown"})]
           ;(println "field: " field)
                (assoc entry
                       :name name
                       :type type
                       :value-str value
                       :value (decode-value field value)))))))


