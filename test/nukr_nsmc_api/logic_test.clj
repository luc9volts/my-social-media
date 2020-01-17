(ns nukr-nsmc-api.logic_test
  "Tests of the logic layer"
  (:require
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [nukr-nsmc-api.logic :as logic]))

(testable-privates nukr-nsmc-api.logic private-rank-mutual-friends)

(defn- private-update-item-network
  [network [p k v]]
  (update-in network [p k] v))

(defn- private-update-network
  [profile1-name profile1-privacy profile2-name profile2-privacy network]
  (let [coll [[profile1-name :friends #(conj (set %) profile2-name)]
              [profile1-name :privacy (fn [_] (Boolean/parseBoolean profile1-privacy))]
              [profile2-name :friends #(conj (set %) profile1-name)]
              [profile2-name :privacy (fn [_] (Boolean/parseBoolean profile2-privacy))]]]
    (reduce private-update-item-network network coll)))

(defn- private-gen-net-test
  "generate a network for test for a given `coll`"
  [coll]
  (reduce (fn [network [p1 pp1 p2 pp2]]
            (private-update-network p1 pp1 p2 pp2 network)) {} coll))

(fact "New profile generation"
      (logic/new-profile "Rodion Raskolnikov" nil) => (just {:name    "RODION RASKOLNIKOV"
                                                             :friends #{}
                                                             :privacy false})
      (logic/new-profile "Zodiac Killer" "true") => (just {:name    "ZODIAC KILLER"
                                                           :friends #{}
                                                           :privacy true}))

(fact "`private-rank-mutual-friends` finds and ranks mutual friends of a given network and profile-name"
      (private-rank-mutual-friends "" {}) => {}
      (private-rank-mutual-friends "Dantes" {}) => {}
      (private-rank-mutual-friends "Dantes" (private-gen-net-test [["Dantes" "false" "Mercedes" "false"]
                                                                   ["Mercedes" "false" "Mondego" "false"]])) => {"Mondego" 1}
      (private-rank-mutual-friends "Dantes" (private-gen-net-test [["Dantes" "false" "Mercedes" "false"]
                                                                   ["Dantes" "false" "Danglars" "false"]
                                                                   ["Mercedes" "false" "Mondego" "false"]
                                                                   ["Danglars" "false" "Mondego" "false"]
                                                                   ["Danglars" "false" "Caderousse" "false"]])) => {"Mondego" 2 "Caderousse" 1})

(fact "`suggestions` get a list of suggestions in rank order except ones who wants privacy"
      (logic/get-suggestions "Dantes" (private-gen-net-test [["Dantes" "" "Mercedes" ""]
                                                             ["Dantes" "" "Danglars" ""]
                                                             ["Mercedes" "" "Mondego" ""]
                                                             ["Danglars" "" "Mondego" ""]
                                                             ["Danglars" "" "Caderousse" ""]])) => '("Mondego" "Caderousse")
      (logic/get-suggestions "Dantes" (private-gen-net-test [["Dantes" "" "Mercedes" ""]
                                                             ["Dantes" "" "Danglars" ""]
                                                             ["Mercedes" "" "Mondego" ""]
                                                             ["Danglars" "" "Mondego" ""]
                                                             ["Danglars" "" "Caderousse" ""]
                                                             ["Mondego" "true" "" ""]])) => '("Caderousse"))

