(ns stackex.module.analize
  (:require [clojure.core.async :as async :refer [>! <! go-loop chan buffer]]))


(def ^:private state (atom {}))

(def accumulated-ch
  (chan (buffer 10000)))


(defn- create-pack-id-key "generate key by seach pack id" [id] (keyword (str "pack-" id)))


(defn- clear-message-state! "reove message by key from state" [key-id] (swap! state dissoc key))


(defn- add-message-state! "add message to state" [message]
  (swap! state (fn [st] (update-in st [(create-pack-id-key (:id message))] #(conj % message)))))


(defn- get-message-state! "get message by key" [key-id] (get @state key-id))


(defn- extract-tag
  "prepare data to calculate, extract all tags and if it's answered set 1 else 0"
  [message-seq]
  (flatten
   (map
    #(reduce
      (fn [acc {:keys [is_answered tags]}]
        (concat acc (map (fn [el] (hash-map :tag el :answered (if is_answered 1 0))) tags)))
      []
      (get-in % [:search :items]))
    message-seq)))
  

(defn process [coll]
  "calculate prepared data, and return hash-map key searched total searched and total answered"
  (reduce #(let [{:keys [tag answered]} %2]
             (if (contains? %1 tag)
               (update %1 tag
                       (fn [st]
                         (hash-map
                           :total (inc (get st :total))
                           :answered (+ (get st :answered) answered)
                           )))
               (assoc %1 tag {:total 1 :answered answered}))) {} coll))


(defn initialize []
  (go-loop []
    (let [msg (<! accumulated-ch)
          {id :id cnt :count res-ch :res-ch} msg
          pack-key (create-pack-id-key id)
          _ (add-message-state! msg)
          pack-messages (get-message-state! pack-key)]
      (when (>= (count pack-messages) cnt)
        (do
          (clear-message-state! pack-key)
          (>! res-ch (process (extract-tag pack-messages))))
        ))
    (recur)))
