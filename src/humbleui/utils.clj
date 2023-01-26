(ns humbleui.utils
  (:require [clojure.java.io :as io])
  (:import (java.text DateFormatSymbols)
           (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)
           (java.util Locale))
  (:gen-class))

(defn format-datetime [jtime]
  [(.format (DateTimeFormatter/ofPattern "dd. MMMM yyyy" (Locale/US))
            jtime)
   (.format (DateTimeFormatter/ofPattern "HH:mm" (Locale/US))
            jtime)])

(defn resource->data [path]
  (-> (io/resource path)
      slurp
      clojure.edn/read-string))

(def weekdays
  (let [short-names (-> (DateFormatSymbols. (Locale. "EN"))
                        .getShortWeekdays
                        vec)]
    (conj (subvec short-names 2)
          (short-names 1))))

