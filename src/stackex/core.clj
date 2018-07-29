(ns stackex.core
  (:gen-class)
  (:require [immutant.web :as immutant]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [stackex.module.configuration :as config]
            [stackex.module.logger :as logger]
            [stackex.module.search :as search]
            [stackex.module.analize :as analize]
            [clojure.spec.alpha :as s]
            [cheshire.core :as cheshire]
            [clojure.core.async :as async :refer [<!! chan buffer]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-body]])
  (:import[java.lang.management ManagementFactory]))


(s/def ::validation-predicat (s/cat :tag (s/* #(re-matches #"^[a-zA-Zа-яА-Я0-9]+$" %))))

(def ^:private not-found {:status 404})

(def ^:private unprocessable-entity {:status 422})

(def ^:private ok {:status 200})

(def ^:private id (atom 0))


(defn- param->vec
  "coerce param into vector or stay it sequential"
  [param]
  (if (sequential? param) param (vector param)))


(defn- vec-of-param->messages
  "coerce param vector into messages with id cnt"
  [pack-id params]
  (let [cnt (count params)
        res-ch (chan (buffer 1))]
    (map #(hash-map :id pack-id :count cnt :tag % :res-ch res-ch) params)))


(defn- get-pid "Gets this process' PID." []
  (let [pid (.getName (ManagementFactory/getRuntimeMXBean))]
    (first (re-seq #"[0-9]+" pid))))


(defn- params-valid?
  "validate request param ASCII with Number & UTF-8 without slashes"
  [params] (s/valid? ::validation-predicat params))


(defn wrap-content-json "wrap header -> application/json" [h]
  (fn [req] (assoc-in (h req) [:headers "Content-Type"] "application/json")))


(defroutes app-routes
  (GET "/search" {params :params}
       (let [coerced-param (param->vec (get params "tag"))]
         (if (params-valid? coerced-param)
           (let [pack-id (swap! id inc)
                 msgs (vec-of-param->messages pack-id coerced-param)
                 res-ch (:res-ch (first msgs))]
             (search/search-params msgs)
             (cheshire/generate-string (<!! res-ch) {:pretty true}))
           unprocessable-entity)))
  
  (route/not-found not-found))


(defn- print-start-info
  "pretty print starting server param and PID for JProfiler or etc"
  [pid server-info]
  (println)
  (println (str "Process PID: " pid))
  (println (str "HTTP: " server-info))
  (println "Server starting!")
  (println))


(defn -main
  "Starting server"
  [& args]
  ;; INITIALIZE BLOCK
  (logger/initialize (config/get-logger))
  (search/initialize (config/get-num-connection))
  (analize/initialize)
  ;; INITIALIZE BLOCK
  (let [{host :host port :port} (config/get-param :application :server)]
    (immutant/run (-> app-routes
                      (wrap-params)
                      (wrap-json-body)
                      (wrap-content-json)
                      )
      {:host host :port port :path "/"}))
  (print-start-info (get-pid) (config/get-param :application :server)))
