(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [puzzle.handlers :as h]
            [puzzle.input :as i]
            [puzzle.templates :as t]
            [yolk.bacon :as b]))

(def board-dimensions [9 9]) ;;[h w]
(def user-start [14 15]) ;;[x y]

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
   :add-point (b/bus)
   :change-view (b/bus)})

(defn render-board
  ([] (render-board world-model))
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

(defn main []
  (render-board)

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

  (render-point [12 12]))

#_
(comment

(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn wrap [bus i j]
  (-> bus
      (b/on-value #(render-board)))
  bus)

(defn init-board-state [[[a b] [c d]]]
  (into {}
        (for [i (range a c)
              j (range b d)
              :let [bus (wrap (b/bus) i j)]]
          [[i j] {:bus bus
                  :occupants []
                  :blocked? false
                  :key-required? false
                  :door? false}])))

(defn add-to-board [coords board]
  (merge board
         (init-board-state
          (t/find-corners coords
                          board-dimensions))))

(def board-state
  (let [loc @(:loc user-state)]
    (atom (add-to-board loc
                        {:current-grid (t/find-corners loc
                                                       board-dimensions)
                         :change-grid (b/bus)
                         :add-grid (b/bus)}))))

(-> @board-state
    :add-grid
    (b/on-value
     (fn [new-coords]
       (swap! board-state
              (partial add-to-board new-coords)))))

(-> @board-state
    :change-grid
    (b/on-value #(render-board)))

(defn change! [{:keys [coords] :as opts}]
  (swap! board-state
         (fn [bs]
           (when-not (get bs coords)
             (b/push (:add-grid bs) coords))
           (h/handle bs opts))))

(defn handle-move [dir]
  (let [loc @(:loc user-state)]
    (change! {:coords loc
              :action :movement
              :direction dir
              :entity {:id :user}})
    (swap! (:loc user-state)
           #(h/move* % dir))))

(defn handle-user-movements! [u]
  (-> (:movements u)
      (b/on-value handle-move)))



(defn main []
  (let [k (i/keyboard-control ($ "body"))]

    (handle-user-movements! user-state)

    (b/plug (:movements user-state) k)
    
    (change! {:coords [16 14]
              :action :placement
              :entity {:type :room-key}})

    (change! {:coords @(:loc user-state)
              :action :placement
              :entity {:type :man
                       :id :user
                       :zi 0}})

    (b/push (:change-grid @board-state) :render)))

  )
