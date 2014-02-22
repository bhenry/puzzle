(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [yolk.bacon :as b]
            [puzzle.input :as i]
            [puzzle.handlers :as h]
            [puzzle.templates :as t]))

(def board-dimensions [9 9]) ;;[h w]

(def user-state
  {:loc (atom [10 15])
   :movements (b/bus)})

(defn init-board-state [[[a b] [c d]]]
  (into {}
        (for [i (range a c)
              j (range b d)]
          [[i j] {:bus (b/bus)
                  :occupants []
                  :blocked? false
                  :key-required? false
                  :door? false}])))

(def board-state (atom (init-board-state
                        (t/find-corners @(:loc user-state)
                                        board-dimensions))))

(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn change! [opts]
  (swap! board-state
         (fn [bs]
           (h/handle bs opts))))

(defn bind-board! [$board board]
  (doseq [[coords {:keys [bus]}] @board]
    (-> bus
        (b/on-value
         (fn [entities]
           (let [$sq (grab $board coords)]
             (j/inner $sq (t/render entities))))))))

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

(defn main []
  (let [g (t/gameboard @(:loc user-state) board-dimensions)
        l (t/layout g)
        k (i/keyboard-control ($ "body"))]

    (-> ($ "#content") (j/inner l))
    (bind-board! ($ g) board-state)

    (change! {:coords @(:loc user-state)
              :action :placement
              :entity {:type :man
                       :id :user
                       :zi 0}})

    (handle-user-movements! user-state)

    (b/plug (:movements user-state) k)
    
#_    (change! {:coords [6 4]
              :action :placement
              :entity {:type :room-key}})))
