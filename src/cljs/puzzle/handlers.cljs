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

(defn remove-entity [cell ent]
  (let [occs (:occupants cell)
        entity (first (filter #(= (:id ent)
                                  (:id %))
                              occs))]
    [(assoc cell
       :occupants (remove #{entity} (:occupants cell)))
     entity]))

(defn add-entity [cell entity]
  (assoc cell
    :occupants (conj (:occupants cell) entity)))

(defn outside? [[[a b] [c d]] [x y]]
  (js/console.log a b c d x y)
  (not (and (some #{x} (range b d))
            (some #{y} (range a c)))))

(defn move [board {:keys [coords direction entity]}]  
  (let [[f t] [coords (move* coords direction)]
        [from ent] (remove-entity (get board f) entity)
        to (add-entity (get board t) ent)
        updated (assoc board f from t to)]

    (when-not (get board t)
      (b/push (:add-grid board) t))
    (when (:bus from)
      (b/push (:bus from) (:occupants from)))
    (when (:bus to)
      (b/push (:bus to) (:occupants to)))
    
    updated))

(defn handle [board opts]
  (condp = (:action opts)
    :placement (place board opts)
    :movement (move board opts)
    board))
