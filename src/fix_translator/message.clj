(ns fix-translator.message
  (:require
   [fix-translator.field :refer [decode-fields encode-fields]]
   [fix-translator.schema :refer [get-msg-type]])
  (:import [java.io StringWriter]))

(defn checksum
  "Returns a 3-character string (left-padded with zeroes) representing the
   checksum of msg calculated according to the FIX protocol."
  [msg]
  (format "%03d" (mod (reduce + (.getBytes msg)) 256)))

; item-reader

(defn create-reader [items]
  {:items-count (count items)
   :items (into [] items)
   :item-idx (atom 0)})

(defn get-current [reader] (get (:items reader) @(:item-idx reader)))
(defn more? [reader] (< @(:item-idx reader) (:items-count reader)))
(defn move-next [reader] (swap! (:item-idx reader) inc))


(declare read-vec)

(defn read-map [{:keys [name content] :as _section} item-reader]
  (println "reading " name " spec-items: " (count content))
  (let [section-reader (create-reader content)]
    (loop [data {}]
      (let [section (get-current section-reader)
            item (get-current item-reader)
            match? (= (:name section) (:name item))
            group? (= (:type section) :group)]
        (println (if match? "=" "x") section item)
        (if match?
          ; match
          (let [_ (move-next item-reader)
                _ (move-next section-reader)
                val (if group?
                      (read-vec {:name (:name section)
                                 :content (:fields section)
                                 :nr (:value item)}
                                item-reader)
                      (:value item))
                data (assoc data
                            (:name section)
                            val)]
            (if (and (more? item-reader)
                     (more? section-reader))
              (recur data)
              data))
          ; no match
          (do (move-next section-reader)
              (if (more? section-reader)
                (recur data)
                data)))))))

(defn read-vec [{:keys [name _content nr] :as section} item-reader]
  (println "read-vec: " name " nr: " nr "section: " section)
  (let [;nr (parse-long nr)
        read-idx (fn [i]
                   (println "group idx: " i)
                   (read-map section item-reader))
        v (map read-idx (range nr))]
    (into [] v) ; this is crucial, as it gets eager
    ))


;{:keys [message items idx] :as _items}

(defn decode-message [{:keys [header trailer messages] :as decoder} items]
  (let [item-reader (create-reader items)
        header (read-map {:name :header :content header} item-reader)
        ;msg-type (get header "MsgType")
        msg-type (:msg-type header)
        payload-section (get-msg-type decoder msg-type)
        payload (read-map payload-section item-reader)
        trailer (read-map {:name :trailer :content trailer} item-reader)]
    ;(assoc data :type msg-type :payload payload-section)
    {:header header
     :payload payload
     :trailer trailer}))


(def checksum-count (count "10=080"))
(def begin-string-count (count "8=FIX.4.4"))
(def body-length-count (count "9=146"))

(def body-length-exclude (+ checksum-count begin-string-count body-length-count))

; body-length-exclude


(defn decode-fix-msg [decoder fix-msg-str]
  (let [fields (decode-fields decoder fix-msg-str)
        msg (decode-message decoder fields)
        ;BodyLength after the BodyLength field  and before the CheckSum field.
        ; exclude BeginString + bodylength + checksum
        fix-count (count fix-msg-str)
        checksum-length (- fix-count checksum-count)
        fix-msg-no-checksum (subs fix-msg-str 0 checksum-length)
        body-length (- fix-count body-length-exclude)]
    (assoc msg
           :wire fix-msg-str
           ;:wire-no-c fix-msg-no-checksum
           :checksum (checksum fix-msg-no-checksum)
           :body-length body-length)))



;; encode

(defn create-writer []
  {:items (atom [])})

(defn write [writer field] (swap! (:items writer) conj field))

(defn pop-items  [writer]
  (let [items @(:items writer)]
    (reset! (:items writer) [])
    items))

(defn linearize-map [{:keys [name content] :as _section} m item-writer]
  (println "linearizing " name " spec-items: " (count content))
  (println "m: " m)
  (println "spec-items:" content)
  (doall 
   (map (fn [{:keys [name required] :as section-field} ]
          (println "processing field: " section-field)
          (if-let [item (get m name)]
            (do 
              (println "=" section-field item) 
              (write item-writer {:name name 
                                  :value item}))
            (do 
              (println "x" section-field "no item")
              (when required 
                (throw (ex-info "fix-encode-payload-missing" {:name name}))
                )
              )
            ))
        content
        )))


(defn encode-fix-msg [{:keys [header trailer messages] :as decoder}
                      fix-msg]
  (let [writer (create-writer)
        _ (linearize-map {:name :header :content header} (:header fix-msg) writer)
        header (->> (pop-items writer)
                    (encode-fields decoder))
        msg-type (get-in fix-msg [:header :msg-type])
        message-spec (get-msg-type decoder msg-type)
        _ (linearize-map message-spec (:payload fix-msg) writer)
        payload (->> (pop-items writer)
                     (encode-fields decoder))
        ]
      {:header header
       :msg-type msg-type
       :payload payload
       }    
    
    
    ))


 

(defn write-to-string []
  (let [sw (StringWriter.)]
    (.write sw "Hello, ")
    (.write sw "World!")
    (.toString sw)))