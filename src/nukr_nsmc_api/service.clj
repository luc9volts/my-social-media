(ns nukr-nsmc-api.service
  "Endpoints of the application"
  (:require
    [io.pedestal.http :as http]
    [io.pedestal.http.body-params :as body-params]
    [io.pedestal.http.route :as route]
    [nukr-nsmc-api.controller :as controller]
    [nukr-nsmc-api.interceptors.error-handling :as error-info]
    [nukr-nsmc-api.interceptors.storage :as storage-info]
    [ring.util.response :as ring-resp]))

(def create-profile
  {:name :create-profile
   :enter
         (fn [{request :request :as ctx}]
           (let [{storage                :storage
                  {:keys [name privacy]} :form-params} request
                 profile        (controller/save-profile name privacy storage)
                 new-profile-id (first profile)
                 url            (route/url-for :profile-view :params {:profile-id new-profile-id})]
             (assoc ctx :response (ring-resp/created url profile))))})

(def change-profile
  {:name :change-profile
   :enter
         (fn [{request :request :as ctx}]
           (let [{storage                :storage
                  {:keys [profile-id]}   :path-params
                  {:keys [name privacy]} :form-params} request]
             (assoc ctx
               :response (if-let [profile (controller/save-profile profile-id name privacy storage)]
                           (ring-resp/response profile)
                           (ring-resp/not-found (str "there's no such profile " profile-id))))))})

(def get-profile
  {:name :get-profile
   :enter
         (fn [{request :request :as ctx}]
           (let [{storage              :storage
                  {:keys [profile-id]} :path-params} request]
             (assoc ctx
               :response (if-let [profile (controller/get-profile profile-id storage)]
                           (ring-resp/response profile)
                           (ring-resp/not-found "there's no such profile")))))})

(def add-profile-friend
  {:name :add-profile-friend
   :enter
         (fn [{request :request :as ctx}]
           (let [{storage                     :storage
                  {:keys [profile-id]}        :path-params
                  {:keys [profile-friend-id]} :form-params} request
                 profile        (controller/get-profile profile-id storage)
                 profile-friend (controller/get-profile profile-friend-id storage)]
             (assoc ctx
               :response (if (some nil? [profile profile-friend])
                           (ring-resp/not-found (if profile profile-friend-id profile-id))
                           (ring-resp/response (controller/add-profile-friend profile-id profile-friend-id storage))))))})

(def get-suggestion-list
  {:name :get-suggestion-list
   :enter
         (fn [{request :request :as ctx}]
           (let [{storage              :storage
                  {:keys [profile-id]} :path-params} request]
             (assoc ctx
               :response (ring-resp/response (controller/get-suggestion-list profile-id storage)))))})

(def common-interceptors [(body-params/body-params)
                          http/json-body
                          error-info/error-interceptor
                          storage-info/storage-interceptor])

(def routes #{["/profiles" :post (conj common-interceptors `create-profile)]
              ["/profiles/:profile-id" :put (conj common-interceptors `change-profile)]
              ["/profiles/:profile-id" :get (conj common-interceptors `get-profile) :route-name :profile-view]
              ["/profiles/:profile-id/friends" :post (conj common-interceptors `add-profile-friend)]
              ["/profiles/:profile-id/suggestions" :get (conj common-interceptors `get-suggestion-list)]})

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})