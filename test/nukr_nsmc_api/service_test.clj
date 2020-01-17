(ns nukr-nsmc-api.service-test
  "Tests for the web service layer"
  (:require
    [clojure.data.json :as json]
    [io.pedestal.test :refer :all]
    [io.pedestal.http :as bootstrap]
    [midje.sweet :refer :all]
    [nukr-nsmc-api.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

;;Here, the network starts with six not connected people.
(def responses (->> (range 5)
                    (map #(response-for service
                                        :post "/profiles"
                                        :body (str "name=Friend" % "&privacy=false")
                                        :headers {"Content-Type" "application/x-www-form-urlencoded"}))
                    (map (fn [{:keys [body status]}]
                           {:id     (first (re-seq #"p[0-9]+" body))
                            :status status
                            :body   body}))
                    (into [])))

(facts "Profiles CRUD"
       (let [resp-new (first responses)
             resp-get (response-for service
                                    :get (str "/profiles/" (:id resp-new))
                                    :headers {"Content-Type" "application/x-www-form-urlencoded"})
             resp-put (response-for service
                                    :put (str "/profiles/" (:id resp-new))
                                    :body "name=Changed Name&privacy=false"
                                    :headers {"Content-Type" "application/x-www-form-urlencoded"})]
         (map :status [resp-new resp-get resp-put]) => '(201 200 200)
         (:body resp-new) => (contains "{\"name\":\"FRIEND0\",\"friends\":[],\"privacy\":false}")
         (:body resp-get) => (contains "{\"name\":\"FRIEND0\",\"friends\":[],\"privacy\":false}")
         (:body resp-put) => (contains "{\"name\":\"CHANGED NAME\",\"friends\":[],\"privacy\":false}")))

(facts "Connecting profiles"
       (let [[f1 f2 & _] responses
             resp-connect (response-for service
                                        :post (str "/profiles/" (:id f1) "/friends")
                                        :body (str "profile-friend-id=" (:id f2))
                                        :headers {"Content-Type" "application/x-www-form-urlencoded"})]
         (:status resp-connect) => 200))

(facts "Suggestion list"
       (let [[f1 f2 f3 f4 & _] responses
             resp-connect2->3 (response-for service
                                            :post (str "/profiles/" (:id f2) "/friends")
                                            :body (str "profile-friend-id=" (:id f3))
                                            :headers {"Content-Type" "application/x-www-form-urlencoded"})
             resp-connect2->4 (response-for service
                                            :post (str "/profiles/" (:id f2) "/friends")
                                            :body (str "profile-friend-id=" (:id f4))
                                            :headers {"Content-Type" "application/x-www-form-urlencoded"})
             resp-suggestions (response-for service
                                            :get (str "/profiles/" (:id f1) "/suggestions"))
             resp-inverted    (response-for service
                                            :get (str "/profiles/" (:id f3) "/suggestions"))]
         (map :status [resp-connect2->3 resp-connect2->4 resp-suggestions resp-inverted]) => '(200 200 200 200)
         (:body resp-suggestions) => (json/write-str [(:id f3) (:id f4)])
         (:body resp-inverted) => (json/write-str [(:id f4) (:id f1)])))

(facts "Suggestion list but with friend 3 better ranked"
       (let [[f1 _ f3 f4 f5] responses
             resp-connect1->5 (response-for service
                                            :post (str "/profiles/" (:id f1) "/friends")
                                            :body (str "profile-friend-id=" (:id f5))
                                            :headers {"Content-Type" "application/x-www-form-urlencoded"})
             resp-connect5->3 (response-for service
                                            :post (str "/profiles/" (:id f5) "/friends")
                                            :body (str "profile-friend-id=" (:id f3))
                                            :headers {"Content-Type" "application/x-www-form-urlencoded"})
             resp-suggestions (response-for service
                                            :get (str "/profiles/" (:id f1) "/suggestions"))]
         (map :status [resp-connect1->5 resp-connect5->3 resp-suggestions]) => '(200 200 200)
         (:body resp-suggestions) => (json/write-str [(:id f3) (:id f4)])))

(facts "Friend 3 now wants privacy"
       (let [[f1 _ f3 f4 _] responses
             resp-put         (response-for service
                                            :put (str "/profiles/" (:id f3))
                                            :body "name=Leave Me Alone&privacy=true"
                                            :headers {"Content-Type" "application/x-www-form-urlencoded"})
             resp-suggestions (response-for service
                                            :get (str "/profiles/" (:id f1) "/suggestions"))]
         (map :status [resp-put resp-suggestions]) => '(200 200)
         (:body resp-suggestions) => (json/write-str [(:id f4)])))