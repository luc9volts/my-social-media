(ns nukr-nsmc-api.interceptors.storage
  "Intercept the call and attach the storage."
  (:gen-class))

(defonce memory-storage (atom {}))

(def storage-interceptor
  {:enter
   (fn [context]
     (update context :request assoc :storage memory-storage))})