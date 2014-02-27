(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [puzzle.entities :as e]
            [puzzle.handlers :as h]
            [puzzle.input :as i]
            [puzzle.templates :as t]
            [yolk.bacon :as b]))

(def board-dimensions [9 9]) ;;[h w]
(def user-start [1000 1000]) ;;[x y]

(defn default-point
  ":door? when true should be any xy to warp to."
  [& [options]]
  {:bus (b/bus)
   :occupants (or (:occupants options) [])
   :blocked? (or (:blocked? options) false)
   :key-required? (or (:key-required? options) false)
   :door? (or (:door? options) false)})

(defn init-board [xy character]
  {xy (default-point {:occupants [character]})})

(def world-model
  {:visible (atom (t/find-corners user-start board-dimensions))
   :points (atom (init-board user-start e/character))
   :user-location (atom user-start)
   :user-inventory (atom {:keys []
                          :life []
                          :money 0})
   :user-movements (b/bus)
   :state-changed (b/bus)})

(defn visible-points [world]
  @(:visible world))

(defn user-location [world]
  @(:user-location world))

(defn points-of-interest [world]
  @(:points world))

(defn init-board-display
  ([] (init-board-display world-model))
  ([world]
     (let [loc (user-location world)
           bd board-dimensions
           points (points-of-interest world)
           [[a b] [c d]] (t/find-corners loc bd)
           relevant-points (for [i (range a c)
                                 j (range b d)]
                             [i j])
           g (t/gameboard [[a b] [c d]]
                          (select-keys points relevant-points))
           l (t/layout g)]
       (reset! (:visible world) relevant-points)
       (-> ($ "#content") (j/inner l)))))

(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn render-point
  ([xy] (render-point xy ($ "#gameboard")))
  ([xy $board] (render-point xy $board world-model))
  ([xy $board world]
     (let [$point (grab $board xy)
           entity (-> (get @(:points world) xy)
                      :occupants
                      t/render)]
       (j/inner $point entity))))

(defn render-points
  ([points] (render-points points ($ "#gameboard")))
  ([points $board] (render-points points $board world-model))
  ([points $board world]
     (let [visible (visible-points world)]
       (if (some #{(user-location world)} visible)
         (doseq [point visible]
           (render-point point $board world))
         (init-board-display world)))))

(defn main []
  (init-board-display world-model)

  (-> (:state-changed world-model)
      (b/on-value
       (fn [points]
         (swap! (:points world-model)
                #(merge % points))
         (render-points points))))

  (-> (:user-movements world-model)
      (b/on-value
       (fn [dir]
         (h/handle world-model
                   {:coords (user-location world-model)
                    :direction dir
                    :action :move
                    :entity {:id :user}}))))
  
  ;;keyboard input
  (-> (i/arrow-stream ($ "body"))
      (b/on-value (fn [dir] (b/push (:user-movements world-model) dir))))
  
  (h/handle world-model
            {:coords [1004 1003]
             :action :place
             :entity e/room-key}))
