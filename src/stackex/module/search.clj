(ns stackex.module.search
  (:require [stackex.module.analize :as analize]
            [stackex.module.worker :as worker]
            [clojure.core.async :as async :refer [>! go chan buffer]]))


(def ^:private search-ch
  (chan (buffer 10000)))

(def ^:private result-ch
  (chan (buffer 10000)))

(def ^:private workers (atom []))


(defn initialize [num-connection]
  (doseq [_ (range num-connection)] (swap! workers #(conj % (worker/worker-factory search-ch analize/accumulated-ch)))))


(defn search-params
  [messages]
  (doseq [msg messages]
    (go [] (>! search-ch msg))))
