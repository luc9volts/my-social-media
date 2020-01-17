(ns nukr-nsmc-api.controller
  "Manages the other layers."
  (:require
    [nukr-nsmc-api.logic :as logic]
    [nukr-nsmc-api.db.storage-access :as db])
  (:gen-class))

(defn save-profile
  ([name privacy storage]
   (let [profile (logic/new-profile name privacy)]
     (last (db/save-profile! nil profile storage))))

  ([profile-id name privacy storage]
   (if-let [old-profile (db/get-profile profile-id storage)]
     ((db/save-profile! profile-id (logic/update-profile-details old-profile name privacy) storage) profile-id))))

(defn get-profile
  [profile-id storage]
  (db/get-profile profile-id storage))

(defn add-profile-friend
  [profile-id profile-friend-id storage]
  "Each friendship must be mutual and not circular"
  (doseq [[x y] (filter #(apply not= %) (map list [profile-id profile-friend-id] [profile-friend-id profile-id]))]
    (db/add-profile-friend! x y storage)))

(defn get-suggestion-list
  [profile-id storage]
  (logic/get-suggestions profile-id @storage))