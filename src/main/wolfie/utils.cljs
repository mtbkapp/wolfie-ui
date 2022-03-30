(ns wolfie.utils
  (:require [clojure.string :as string])
  )


(defn goto!
  [& url-segments]
  (.assign js/window.location (str "#/" (string/join "/" url-segments))))

