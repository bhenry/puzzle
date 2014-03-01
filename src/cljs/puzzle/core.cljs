(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [puzzle.entities :as e]
            [puzzle.handlers :as h]
            [puzzle.input :as i]
            [puzzle.maps :as m]
            [puzzle.view :as v]
            [yolk.bacon :as b]))

(def board-dimensions [9 9]) ;;[h w]
(def user-start [1000 1000]) ;;[x y]

(def world-model
  {:visible (atom (v/find-corners user-start board-dimensions))
   :points (atom (m/init-board user-start e/character))
   :user-location (atom user-start)
   :user-inventory (atom {:health 2 ;;heart containers
                          :life 1 ;;filled hearts
                          :keys 0
                          :money 0})
   :user-movements (b/bus)
   :inventory-changes (b/bus)})

(defn point [points xy]
  (get points xy (m/point xy)))

(defn main []
  ;;playground
  (h/put world-model [1006 1006] e/room-key)
  (h/put world-model [1004 1001] (e/money 10))
  (h/put world-model [1005 1003] e/heart)
  (h/put world-model [1002 1003] e/heart-container)

  (swap! (:points world-model)
         (fn [wm]
           (merge wm
                  (m/wall [999 999] :vertical 9)
                  (m/wall [999 999] :horizontal 9)
                  (m/wall [1007 999] :vertical 9)
                  (m/wall [999 1007] :horizontal 9)
                  (m/door [1003 1007] [1003 1008] {:locked? true}))))
  
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
