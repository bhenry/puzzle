(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [yolk.bacon :as b]
            [puzzle.input :as i]
            [puzzle.handlers :as h]
            [puzzle.templates :as t]))

(declare render-board)

(def board-dimensions [9 9]) ;;[h w]

(def user-state
  {:loc (atom [10 15])
   :movements (b/bus)})


(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn wrap [bus i j]
  (-> bus
      (b/on-value
       (fn [entities]
         (let [$sq (grab ($ "#gameboard") [i j])]
           (j/inner $sq (t/render entities))))))
  bus)

(defn init-board-state [[[a b] [c d]]]
  (into {}
        (for [i (range a c)
              j (range b d)
              :let [bus (b/bus)]]
          [[i j] {:bus (wrap bus i j)
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
  (atom (add-to-board @(:loc user-state)
                      {:change-grid (b/bus)
                       :add-grid (b/bus)})))

(-> @board-state
    :add-grid
    (b/on-value
     (fn [new-coords]
       (swap! board-state
              (partial add-to-board new-coords)))))

(-> @board-state
    :change-grid
    (b/on-value
     (fn [coords]
       (render-board coords))))

(defn change! [{:keys [coords] :as opts}]
  (swap! board-state
         (fn [bs]
           (when-not (get bs coords)
             (b/push (:add-grid bs) coords))
           (h/handle bs opts))))

(defn handle-user-movements! [u]
  (-> (:movements u)
      (b/on-value
       (fn [dir]
         (let [loc @(:loc u)]
           (change! {:coords loc
                     :action :movement
                     :direction dir
                     :entity {:id :user}})
           (reset! (:loc u) (h/move* loc dir)))))))

(defn render-board [coords]
  (let [g (t/gameboard coords board-dimensions @board-state)
        l (t/layout g)]
    (-> ($ "#content") (j/inner l))))

(defn main []
  (let [k (i/keyboard-control ($ "body"))]

    (render-board @(:loc user-state))

    (handle-user-movements! user-state)

    (b/plug (:movements user-state) k)
    
    (change! {:coords [16 14]
              :action :placement
              :entity {:type :room-key}})

    (change! {:coords @(:loc user-state)
              :action :placement
              :entity {:type :man
                       :id :user
                       :zi 0}})))
