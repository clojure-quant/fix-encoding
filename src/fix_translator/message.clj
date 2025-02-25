(ns fix-translator.message
  (:require
   [fix-translator.field :refer [to-keyword decode-fields]]))

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
                            ;(:name section)
                            (to-keyword (:name section))
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
  (println "read-vec: " name " nr: " nr)
  (let [;nr (parse-long nr)
        read-idx (fn [i]
                   (println "group idx: " i)
                   (read-map section item-reader))
        v (map read-idx (range nr))]
    (into [] v) ; this is crucial, as it gets eager
    ))


;{:keys [message items idx] :as _items}

(defn read-message [{:keys [header trailer messages] :as spec} items]
  (let [item-reader (create-reader items)
        header (read-map {:name :header :content header} item-reader)
        ;msg-type (get header "MsgType")
        msg-type (:msg-type header)
        payload-section (get messages msg-type)
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
        msg (read-message decoder fields)
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
