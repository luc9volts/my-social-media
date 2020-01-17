(ns nukr-nsmc-api.logic
  "Network functions"
  (:require
    [clojure.string :refer [upper-case trim]])
  (:gen-class))

(defn- private-rank-mutual-friends
  "Get from `network` mutual friends of `profile-id` friends and count the occurrences"
  [profile-id network]
  (let [actual-friends (get-in network [profile-id :friends])
        self?          #{profile-id}]
    (->> actual-friends
         (mapcat #(get-in network [% :friends]))
         (remove actual-friends)
         (remove self?)
         frequencies)))

(defn get-suggestions
  "Get from `network` a ranked list of friends of friends for `profile-name`"
  [profile-id network]
  (let [ranked-suggestions (private-rank-mutual-friends profile-id network)
        wants-privacy?     #(get-in network [(first %) :privacy])]
    (->> ranked-suggestions
         (remove wants-privacy?)
         (sort-by last >)
         (map first))))

(defn update-profile-details
  [old-profile name privacy]
  (assoc old-profile
    :name ((comp upper-case trim) name)
    :privacy (Boolean/parseBoolean privacy)))

(defn new-profile [name privacy]
  {:name    ((comp upper-case trim) name)
   :friends #{}
   :privacy (Boolean/parseBoolean privacy)})