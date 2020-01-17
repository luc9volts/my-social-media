(ns nukr-nsmc-api.interceptors.error-handling
  "Intercept all errors generated in other interceptors of the stack"
  (:require
    [io.pedestal.interceptor.error :as interceptor.error]
    [ring.util.response :as ring-resp])
  (:gen-class))

(def error-interceptor
  (interceptor.error/error-dispatch
    [ctx ex]
    :else
    (assoc ctx
      :response (-> {"error" {"message" (.getMessage ex)}}
                    (ring-resp/response)
                    (ring-resp/status 500)))))