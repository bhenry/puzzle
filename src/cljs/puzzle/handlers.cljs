(ns puzzle.handlers
  (:require [yolk.bacon :as b]))

(defn place [world {:keys [coords entity]}]
  (let [points @(:points world)
        {:keys [occupants] :as point} (get points coords)
        occs (conj occupants entity)]
    (assoc-in points coords (assoc point :occupants occs))))

(defn move* [[x y] dir]
  (let [dist 1]
    (condp = dir
      :north [x (- y dist)]
      :south [x (+ y dist)]
      :west  [(- x dist) y]
      :east  [(+ x dist) y])))

(defn remove-entity [point ent]
  (let [occs (:occupants point)
        entity (first (filter #(= (:id ent) (:id %)) occs))]
    [(assoc point :occupants (remove #{entity} occs)) entity]))

(defn add-entity [point entity]
  (assoc point
    :occupants (conj (:occupants point) entity)))

(defn move [world {:keys [coords direction entity]}]  
  (let [points @(:points world)
        [f t] [coords (move* coords direction)]
        [from ent] (remove-entity (get points f) entity)
        to (add-entity (get points t) ent)]
    (if (= :user (:id entity))
      (reset! (:user-location world) t))
    (assoc points f from t to)))

(defn handle [world event]
  (let [new (condp = (:action event)
              :move (move world event)
              :place (place world event)
              nil)]
    (when new
      (b/push (:state-changed world) new))))
