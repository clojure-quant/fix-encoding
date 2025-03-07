(ns fix-translator.field
  (:require
   [tick.core :as t]
   [cljc.java-time.local-date-time :as ldt]
   [fix-translator.schema :refer [get-field get-field-by-name]]))

(defn parse-utc-timestamp [s]
  (let [;cleaned (str/replace s #"\.(\d{1,3})$" (fn [[_ ms]] (format ".%03d" (Integer/parseInt ms)))) ;; Normalize milliseconds
        cleaned s
        formats ["yyyyMMdd-HH:mm:ss.SSS" "yyyyMMdd-HH:mm:ss"]
        ldt (some (fn [fmt]
                    (try
                      ;(println "fmt: " fmt)
                      (ldt/parse cleaned (t/formatter fmt))
                      (catch Exception _ nil)))
                  formats)]
    (-> ldt
        (t/in "UTC")              ;; Convert to UTC timezone
        t/instant)))

;; Example usage:
;(parse-utc-timestamp "20250224-21:13:01.525") ;; => #inst "2025-02-24T21:13:01.525Z"
;(parse-utc-timestamp "20250224-21:13:01")     ;; => #inst "2025-02-24T21:13:01Z"

(defn format-utc-timestamp
  "Returns a UTC timestamp in a specified format."
  [inst]
  (let [;format "yyyyMMdd-HH:mm:ss.SSS"
        format "yyyyMMdd-HH:mm:ss"]
    ; instant cannot be formatted
    (t/format (t/formatter format) (t/date-time inst))))

;(format-utc-timestamp (t/instant))

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
      ;(= name "MsgType")
      (= name :msg-type)
      value
      ; enums
      (seq values)
      (some #(when (= (:enum %) value)
               (:description %)) values)
      ; parse
      :else
      (if-let [parse-fn (get parser type)]
        (parse-fn value)
        value))))

(defn decode-fields [decoder fix-msg-vec]
  (->> fix-msg-vec
       (map (fn [[tag value]]
              ;(println "decoding tag: " tag  "value:" value)
              (let [{:keys [name _values type] :as field} (get-field decoder tag)]
                {:tag tag
                 :value-str value
                 :name name
                 :type type
                 :value (decode-value field value)})))))


; encode

(defn encode-value [{:keys [name type values] :as _field} value]
  ;(println "converting tag: " name  "type: " type " value: " value "values: " values)
  (let [parser {"LOCALMKTDATE" str; todo
                "SEQNUM" str
                "LENGTH" str
                "QTY" str
                "STRING" identity
                "INT" str
                "PRICE" str
                "CHAR" str
                "NUMINGROUP" str
                "DATA" str ; todo
                "BOOLEAN" str ; todo
                "UTCTIMESTAMP" format-utc-timestamp}]
    (cond
      ; do not change msgtype
      ;(= name "MsgType")
      (= name :msg-type)
      value
      ; enums
      (seq values)
      (some #(when (= (:description %) value)
               (:enum %)) values)
      ; parse
      :else
      (if-let [encode-fn (get parser type)]
        (encode-fn value)
        value))))

(defn encode-field [decoder {:keys [name value] :as fix-field}]
  (let [{:keys [tag _values type] :as field} (get-field-by-name decoder name)]
             ;(println "field: " field)
    (assoc fix-field
           :tag tag
           :type type
           :value-str (encode-value field value))))

#_(defn encode-fields [decoder fix-field-seq]
    (->> fix-field-seq
         (map #(encode-field decoder %))))
