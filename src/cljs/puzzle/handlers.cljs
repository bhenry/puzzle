(ns puzzle.handlers
  (:require [yolk.bacon :as b]))

(defn place [board {:keys [coords entity]}]
  (let [{:keys [occupants bus] :as cell} (get board coords)
        occupants (conj occupants entity)]

    (if bus
      (b/push bus occupants)
      (b/push (:add-grid board) coords))

    (assoc board
      coords
      (assoc cell
        :occupants occupants))))

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
    (reset! (:user-location world) t)
    (assoc points f from t to)))

(defn handle [world event]
  (let [new (condp = (:action event)
              :move (move world event)
              nil)]
    (when new
      (b/push (:state-changed world) new))))
