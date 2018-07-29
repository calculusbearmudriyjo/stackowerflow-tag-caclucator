(ns stackex.module.worker
  (:require [org.httpkit.client :as client]
            [cheshire.core :as cheshire]
            [stackex.module.logger :as logger]
            [stackex.module.configuration :as config]
            [clojure.core.async :as async :refer [>! <! go-loop]]))


(defn url-creator [tag] (let [{url :url query :query} (config/get-search-url)]
                          (str url "?" query "&tagged=" tag)))


(defn- get! [endpoint] @(client/get endpoint {:content-type :json}))


(defn- resp->data [{:keys [status headers body error]}]
  (if-not (and (nil? error) (> status 300))
    (cheshire/parse-string body true)
    (do
      (logger/error error
                    {:error body}
                    "Bad request")
      {:error "error response"})))


(defn worker-factory
  "that get search message and write result into out-chan"
  [in-chan out-chan]
  (go-loop []
    (let [msg (<! in-chan)
          resp-msg (dissoc msg :tag)
          rq-url (url-creator (:tag msg))
          data (get! rq-url)
          searched-data (resp->data data)]
      (>! out-chan (assoc resp-msg :search searched-data)))
    (recur)))
