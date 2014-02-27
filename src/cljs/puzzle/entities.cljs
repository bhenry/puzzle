(ns puzzle.entities
  (:require [puzzle.templates :as t]))

(def character
  {:type :man
   :id :user
   :zi 0})

(def room-key
  {:type :room-key
   :id :room-key
   :zi 100})

(defn render [entities]
  (let [entity (first (sort-by (fn [e] (or (:zi e) 10)) entities))]
    (condp = (:type entity)
      :man (man)
      :room-key (room-key)
      (blank))))
