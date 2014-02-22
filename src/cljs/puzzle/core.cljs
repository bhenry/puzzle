(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [puzzle.handlers :as h]
            [puzzle.input :as i]
            [puzzle.templates :as t]
            [yolk.bacon :as b]))

(def board-dimensions [9 9]) ;;[h w]
(def user-start [0 0]) ;;[x y]

(def character
  {:type :man
   :id :user
   :zi 0})

(def room-key
  {:type :room-key
   :id :room-key
   :zi 100})

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
   :points (atom (init-board user-start character))
   :user-location (atom user-start)
   :user-movements (b/bus)
   :state-changed (b/bus)
   :grid-chanded (b/bus)})

(defn init-board-display
  ([] (init-board-display world-model))
  ([world]
     (let [g (t/gameboard @(:user-location world)
                          board-dimensions
                          @(:points world))
           l (t/layout g)]
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

(defn within?
  "returns true if x is in (range a b)"
  [x a b]
  (boolean (some #{x} (range a b))))

(defn within-grid?
  "returns true when [x y] is within [a b] [c d]"
  [[[a b] [c d]] [x y]]
  (and (within? x a c)
       (within? y b d)))

(defn visible-points [world points]
  (filter (partial within-grid? @(:visible world))
          (map first points)))

(defn render-points
  ([points] (render-points points ($ "#gameboard")))
  ([points $board] (render-points points $board world-model))
  ([points $board world]
     (let [visible (visible-points world points)]
       (if (some #{@(:user-location world)} visible)
         (doseq [point visible]
           (render-point point $board world))
         (init-board-display world)))))

(defn main []
  (init-board-display world-model)

  (i/keyboard-control ($ "body") world-model)
  (-> (:user-movements world-model)
      (b/on-value
       (fn [[xy dir]]
         (h/handle world-model
                   {:coords xy
                    :direction dir
                    :action :move
                    :entity {:id :user}}))))
  
  (-> (:state-changed world-model)
      (b/on-value
       (fn [points]
         (swap! (:points world-model)
                #(merge % points))
         (render-points points))))

  ;;begin of (comment...
  (comment
    (reset! (:points world-model) [])

    (render-board)

    (reset!
     (:points world-model)
     {[15 16] (default-point {:occupants [room-key]})
      [13 17] (default-point {:occupants [character]})})

    (render-board)

    (swap!
     (:points world-model)
     (fn [m]
       (merge m
              {[12 12] (default-point {:occupants [room-key]})})))

    (render-point [12 12])

    
    );;end of (comment...
  )
