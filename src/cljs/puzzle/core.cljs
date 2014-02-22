(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [puzzle.handlers :as h]
            [puzzle.input :as i]
            [puzzle.templates :as t]
            [yolk.bacon :as b]))

(def board-dimensions [9 9]) ;;[h w]
(def user-start [1000 1000]) ;;[x y]

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
   :state-changed (b/bus)})

(defn visible-points [world]
  @(:visible world))

(defn init-board-display
  ([] (init-board-display world-model))
  ([world]
     (let [loc @(:user-location world)
           bd board-dimensions
           g (t/gameboard loc bd @(:points world))
           l (t/layout g)]
       (reset! (:visible world)
               (let [[[a b] [c d]] (t/find-corners loc bd)]
                 (for [i (range b d)
                       j (range a c)]
                   [i j])))
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
       (if (some #{@(:user-location world)} visible)
         (doseq [point visible]
           (render-point point $board world))
         (init-board-display world)))))

(defn main []
  (init-board-display world-model)

  (i/keyboard-control ($ "body") world-model)
  (-> (:user-movements world-model)
      (b/on-value
       (fn [dir]
         (js/console.log (pr-str @(:user-location world-model)
                                 dir
                                 @(:visible world-model)))
         (h/handle world-model
                   {:coords @(:user-location world-model)
                    :direction dir
                    :action :move
                    :entity {:id :user}}))))
  
  (-> (:state-changed world-model)
      (b/on-value
       (fn [points]
         (swap! (:points world-model)
                #(merge % points))
         (render-points points))))

#_  (h/handle world-model
            {:coords [1008 1003]
             :action :place
             :entity {:type :room-key
                      :id :room-key}})

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
