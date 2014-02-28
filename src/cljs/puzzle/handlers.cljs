(ns puzzle.handlers
  (:require [yolk.bacon :as b]))

(defn move* [[x y] dir]
  (let [dist 1]
    (condp = dir
      :north [x (- y dist)]
      :south [x (+ y dist)]
      :west  [(- x dist) y]
      :east  [(+ x dist) y])))

(defn remove-entity [point id]
  (let [occs (:occupants point)
        entity (first (filter #(= id (:id %)) occs))]
    (assoc point :occupants (remove #{entity} occs))))

(defn add-entity [point entity]
  (assoc point
    :occupants (remove :pickup? (conj (:occupants point) entity))))

(defn valid-move? [to]
  (not (:blocked? to)))

(defn update-inventory [item]
  (fn [old]
    (merge-with + old
                (condp = (:type item)
                  :room-key {:keys 1}
                  :money {:money (:value item)}
                  :life {:life 1}
                  {}))))

(defn pickup-item [inventory item]
  (swap! inventory (update-inventory item)))

(defn handle-items [world point]
  (let [occs (:occupants point)]
    (doseq [item (filter :pickup? occs)]
      (pickup-item (:user-inventory world) item))
    (b/push (:inventory-changes world) @(:user-inventory world))))

(defn handle-user-input [world]
  (fn [direction]
    (let [f @(:user-location world)
          t (move* f direction)
          points @(:points world)
          from (get points f)
          to (get points t)
          entity (->> (:occupants from)
                      (filter #(= :user (:id %)))
                      first)]
      (when (valid-move? to)
        (handle-items world to)
        (reset! (:user-location world) t)
        (swap! (:points world)
               #(merge % {f (remove-entity from :user)
                          t (add-entity to entity)}))
        (b/push (:user-movements world) [f t])))))

(defn put [world xy entity]
  (swap! (:points world)
         (fn [p]
           (assoc p
             xy (merge-with concat
                            (get p xy)
                            {:occupants [entity]})))))
