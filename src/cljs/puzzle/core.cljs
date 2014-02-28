(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [puzzle.entities :as e]
            [puzzle.handlers :as h]
            [puzzle.input :as i]
            [puzzle.view :as v]
            [yolk.bacon :as b]))

(def board-dimensions [9 9]) ;;[h w]
(def user-start [1000 1000]) ;;[x y]

(defn default-point
  ":door? when true should be any xy to warp to."
  [& [options]]
  {:occupants (or (:occupants options) [])
   :blocked? (or (:blocked? options) false)
   :key-required? (or (:key-required? options) false)
   :door? (or (:door? options) false)})

(defn init-board [xy character]
  {xy (default-point {:occupants [character]})})

(def world-model
  {:visible (atom (v/find-corners user-start board-dimensions))
   :points (atom (init-board user-start e/character))
   :user-location (atom user-start)
   :user-inventory (atom {:life 3
                          :keys 0
                          :money 0})
   :user-movements (b/bus)
   :inventory-changes (b/bus)})

(defn point [points xy]
  (get points xy (default-point)))

(defn main []
  ;;playground
  (h/put world-model [1006 1007] e/room-key)
  (h/put world-model [1004 1003] (e/money 10))

  (let [game (v/init-world-view world-model)]
    ;;handle input
    (-> (i/arrow-stream ($ "body"))
        (b/on-value
         (h/handle-user-input world-model)))

    (-> (:user-movements world-model)
        (b/on-value
         (fn [[xyf xyt]]
           (if (some #{xyt} (map first (v/visible-world world-model)))
             (let [points @(:points world-model)]
               (b/push (:redraw-point game) [xyf (point points xyf)])
               (b/push (:redraw-point game) [xyt (point points xyt)]))
             (do (reset! (:visible world-model)
                         (v/find-corners @(:user-location world-model)
                                         board-dimensions))
                 (b/push (:redraw-gameboard game) world-model))))))

    (-> (:inventory-changes world-model)
        (b/on-value
         (fn [inventory]
           (b/push (:redraw-inventory game) inventory))))
    ;;end input handling
    
    (j/inner ($ "#content") (:$container game))))
