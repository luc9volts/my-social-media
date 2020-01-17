(ns nukr-nsmc-api.db.storage-access
  "Deal with the storage. It has side effects."
  (:require
    [clojure.string :refer [upper-case trim]])
  (:gen-class))

(defn save-profile!
  [profile-id profile storage]
  (let [id (or profile-id (str (gensym "p")))]
    (swap! storage assoc id profile)))

(defn get-profile
  [profile-id storage]
  (@storage profile-id))

(defn add-profile-friend!
  [profile-id profile-friend-id storage]
  (swap! storage update-in [profile-id :friends] #(conj % profile-friend-id)))