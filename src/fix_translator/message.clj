(ns fix-translator.message
  (:require
   [fix-translator.field :refer [decode-fields encode-field]]
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

(declare linearize-map)

(defn linearize-vec [decoder vec-name fields vec-value item-writer]
  (let [; fields: [{:name :mdentry-type :required true}]
           ; example of vec item: [{:mdentry-type :bid} {:mdentry-type :offer}]
        size (count vec-value)
        section {:name vec-name :content fields}]
      ;(println "vector field: " vec-value)
      ; encode size
    (write item-writer (encode-field decoder {:name vec-name
                                              :value size}))
       ; encode each map in vec-value
    (->>  vec-value
          (map #(linearize-map decoder section % item-writer))
          doall)))

(defn linearize-map [decoder {:keys [name content] :as _section} m item-writer]
    ;(println "linearizing " name " spec-items: " (count content))
    ;(println "m: " m)
    ;(println "spec-items:" content)
  (doall
   (map (fn [{:keys [name required type fields] :as section-field}]
            ;(println "processing field: " section-field)
          (if-let [item (get m name)]
            (do
                ;(println "=" section-field item)
              (if (= type :group)
                  ; vector field
                (linearize-vec decoder name fields item item-writer)
                  ; simple field
                (write item-writer (encode-field decoder {:name name
                                                          :value item}))))
            (do
                ;(println "x" section-field "no item")
              (when required
                (throw (ex-info "fix-encode-payload-missing" {:msg-type name
                                                              :missing-field section-field
                                                              :payload-map m}))))))
        content)))

(defn write-fields2 [fields]
  (->> fields
       (map (fn [{:keys [tag value-str]}]
              [tag value-str]))))

(defn acc-size-field [r [t v]]
  (+ r 2 (count t) (count v)))

(defn fix-body-length [fields]
  (reduce acc-size-field 0 fields))

;(fix-body-length [["A" "123"] ["B" "0"]])

(defn byte-count [s]
  (reduce + (.getBytes s)))

(defn acc-checksum-field [r [t v]]
  (+ r
     (byte-count "=")
     (byte-count "")
     (byte-count t)
     (byte-count v)))

(defn checksum2
  "Returns a 3-character string (left-padded with zeroes) representing the
     checksum of msg calculated according to the FIX protocol."
  [fields]
  (let [bc (reduce acc-checksum-field 0 fields)
        cs (mod bc 256)]
    (format "%03d" cs)))

(defn encode-fix-body [decoder [msg-type body]]
  (let [writer (create-writer)
        message-spec (get-msg-type decoder msg-type)
        _ (linearize-map decoder message-spec body writer)
        payload (pop-items writer)]
    (->> (write-fields2 payload)
         (into []))))

(defn encode-header [decoder header]
  (let [writer (create-writer)
        header-decoder (-> (:header decoder)
                           rest rest) ;without begin-string and message-size
        _ (linearize-map decoder {:name :header :content header-decoder}
                         header writer)
        header (pop-items writer)]
    (write-fields2 header)))

(defn insert-begin-size [fix-version header-vec body-vec]
  (let [header-length (fix-body-length header-vec)
        body-length (fix-body-length body-vec)
        header-body-length (+ header-length body-length)]
    (concat
     [["8" fix-version]
      ["9" (str header-body-length)]]
     header-vec
     body-vec)))

(defn add-checksum [fix-vec]
  (let [checksum (checksum2 fix-vec)]
    (->> (concat fix-vec
                 [["10" checksum]])
         (into []))))

(defn encode-fix-msg [decoder fix-msg]
  (let [msg-type (get-in fix-msg [:header :msg-type])
        fix-version (get-in fix-msg [:header :begin-string])
        header-vec (encode-header decoder (:header fix-msg))
        body-vec (encode-fix-body decoder [msg-type (:payload fix-msg)])
        fix-vec (insert-begin-size fix-version header-vec body-vec)]
    (add-checksum fix-vec)))

(defn encode-fix-msg2 [{:keys [header trailer messages] :as decoder}
                       fix-msg]
  (let [msg-type (get-in fix-msg [:header :msg-type])

        writer (create-writer)
        _ (linearize-map decoder {:name :header :content header} (:header fix-msg) writer)
        header (pop-items writer)

        payload-wire (encode-fix-body decoder [msg-type (:payload fix-msg)])
          ; fix-str
        [field-begin field-body-len & header-body] header
        wire-body (concat
                   (write-fields2 header-body)
                   payload-wire)
          ; body length
        body-length (fix-body-length wire-body)
        field-body-len (assoc field-body-len
                              :value body-length
                              :value-str (str body-length))
        wire-type-length (write-fields2 [field-begin field-body-len])
        wire (concat wire-type-length wire-body)
          ; checksum
        checksum (checksum2 wire)
        trailer-section {:check-sum checksum}
        _ (linearize-map decoder {:name :trailer :content trailer} trailer-section writer)
        trailer-payload (pop-items writer)
        wire-trailer (write-fields2 trailer-payload)
        wire-full (into [] (concat wire wire-trailer))]
    {:header (concat [field-begin field-body-len] header-body)
     :trailer trailer-payload
     :msg-type msg-type
     :wire wire-full
     :body-length body-length
     :checksum checksum}))




