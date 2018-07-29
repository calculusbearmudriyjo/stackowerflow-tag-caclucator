(ns stackex.module.logger
  (:require
    [taoensso.timbre :as log]
    [clojure.string :as string]
    [taoensso.timbre.appenders.3rd-party.rolling :as rolling]
    [clojure.pprint :refer [pprint]]))


(defn initialize
  "Initialize logger module. log only in file"
  [{log-level :log-level
    log-full-path :log-full-path}]
  (log/merge-config! {:level log-level
                      :async? true
                      :output-fn (partial log/default-output-fn {:stacktrace-fonts {}})
                      :appenders {:rolling (rolling/rolling-appender {:path log-full-path :pattern :daily})}}))


(defn- pretty-print[coll]
  (with-out-str (pprint coll)))


(defn error
  "Log error with pretty printing"
  ([exc msg]
   (log/error exc msg))
  ([exc coll & msg] (error exc (pretty-print {:msg msg :coll coll}))))
