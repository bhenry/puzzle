(ns puzzle.handlers
  (:require [puzzle.entities :as e]
            [puzzle.maps :as m]
            [yolk.bacon :as b]))

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
    (assoc point
      :occupants (remove nil? (remove #{entity} occs)))))

(defn add-entity [point entity]
  (assoc point
    :occupants (->> (conj (:occupants point) entity)
                    (remove :pickup?)
                    (remove nil?))))

(defn check-access [to inv]
  (cond
   (:locked? to) (<= 1 (:keys inv))))

(defn validate-move? [to inv]
  (if (:blocked? to)
    (check-access to inv)
    true))

(defn update-inventory [item]
  (fn [old]
    (let [i (fn [o k] (assoc o k (-> o k inc)))
          iv (fn [o k v] (assoc o k (-> o k (+ v))))
          im (fn [o k m] (assoc o k (min m (-> o k inc))))]
      (condp = (:type item)
        :room-key (i old :keys)
        :money (iv old :money (:value item))
        :life (im old :life (:health old))
        :health (-> old
                    (i :health)
                    (i :life))
        old))))

(defn pickup-item [inventory item]
  (swap! inventory (update-inventory item)))

(defn handle-items [world point]
  (let [occs (:occupants point)
        inventory (:user-inventory world)]
    ;;pickup items on the floor
    (doseq [item (filter :pickup? occs)]
      (pickup-item inventory item))
    ;;register inventory changes
    (b/push (:inventory-changes world) @(:user-inventory world))))

(defn open-door [door]
  (merge door
         {:blocked? false
          :locked? false
          :occupants [e/open-door]}))

(defn handle-door [world door]
  (let [ps @(:points world)
        out door
        in (get ps (:door? door))]
    (when (:locked? in)
      (swap! (:user-inventory world) (fn [i] (merge-with - i {:keys 1})))
      (b/push (:inventory-changes world) @(:user-inventory world)))

    (when (every? :door? [in out])
      ;;by now we know that the move is valid
      (swap! (:points world)
             #(assoc %
                (:door? out) (open-door in)
                (:door? in) (open-door out))))))

(defn handle-user-input [world]
  (fn [direction]
    (let [points @(:points world)
          f @(:user-location world)
          from (get points f (m/point))
          t* (move* f direction)
          to* (get points t* (m/point))
          [t to] (if (and (:door? to*) (not= f (:door? to*)))
                   [(:door? to*) (get points (:door? to*))]
                   [t* to*])
          inventory @(:user-inventory world)
          user (->> (:occupants from)
                    (filter #(= :user (:id %)))
                    first)]
      (when (validate-move? to inventory)
        (reset! (:user-location world) t)
        (handle-items world to)
        (when (:door? to)
          (handle-door world to))
        
        (swap! (:points world)
               #(merge %
                       {f (remove-entity (get @(:points world) f) :user)
                        t (add-entity (get @(:points world) t) user)}))
        (b/push (:user-movements world) [t f t*])))))

(defn put [world xy entity]
  (swap! (:points world)
         (fn [p]
           (assoc p
             xy (merge-with concat
                            (get p xy (m/point xy))
                            {:occupants [entity]})))))
