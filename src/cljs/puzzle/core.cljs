(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [yolk.bacon :as b]
            [puzzle.templates :as t]
            [puzzle.handlers :as h]))

(def board-dimensions [9 9]) ;;[h w]

(def init-board-state
  (into {}
        (for [i (range (first board-dimensions))
              j (range (second board-dimensions))]
          [[i j] {:bus (b/bus)
                  :occupants []
                  :blocked? false
                  :key-required? false
                  :door? false}])))

(def board-state (atom init-board-state))

(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn change-state! [opts]
  (swap! board-state
         (fn [bs]
           (h/handle bs opts))))

(defn bind-board! [$board]
  (doseq [[coords {:keys [bus]}] @board-state]
    (-> bus
        (b/on-value
         (fn [entities]
           (js/console.log (pr-str entities))
           (let [$sq (grab $board coords)]
             (j/inner $sq (t/render (last entities)))))))))

(defn change [opts]
  (change-state! opts))

(defn main []
  (let [g (t/gameboard board-dimensions)
        l (t/layout g)]
    (-> ($ "#content") (j/inner l))
    (bind-board! ($ g))

    (change {:coords [3 6]
             :action :placement
             :entity {:type :man
                      :id :user}})
    
    (change {:coords [6 4]
             :action :placement
             :entity {:type :room-key}})

    (change {:coords [3 6]
             :action :movement
             :direction :north
             :entity {:id :user}})))
