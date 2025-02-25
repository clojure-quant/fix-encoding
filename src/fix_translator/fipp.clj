(ns fix-translator.fipp
  (:require
   ;[clojure.java.io :as io]
   [fipp.clojure]
   [clojure.edn])
  (:import (java.io StringWriter)))


(defn pprint-str [data]
  (let [sw (StringWriter.)]
    (fipp.clojure/pprint data {:width 60 :writer sw :print-meta true})
    (str sw)))

(defn spit-edn [file-name data]
  (let [sedn (pprint-str data)]
    (spit file-name sedn)
    data  
    ))