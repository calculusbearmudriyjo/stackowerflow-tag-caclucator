(ns stackex.module.configuration
  (:require [config.core :refer [env]]))


(def ^:private config
  (atom env))


(defn get-param
  "try get param from config. if param not found throw exception, fast fail"
  [& path]
  (let [value (get-in @config path)]
    (when (nil? value)
      (throw (RuntimeException. (str "Config not found for key seq = " path))))
    value))


(defn get-logger "Get logger config" []
  (let [logger (get-param :application :logging)]
    {:log-full-path (str (logger :log-path) (logger :log-file-name))
     :log-level (logger :level)}))


(defn get-search-url "Get worker config" []
  (let [worker (get-param :application :searcher)]
    {:url (worker :url)
     :query (worker :query)}))


(defn get-num-connection "Get num connection config" []
  (get-param :application :http-connections))
