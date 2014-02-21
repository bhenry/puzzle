(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [yolk.bacon :as b]
            [cljs.reader :refer [read-string]]
            [puzzle.templates :as t]))

(def board-dimensions [9 9])

(def init-board-state
  (into {}
        (for [i (range (first board-dimensions))
              j (range (second board-dimensions))]
          [[i j] (b/bus)])))

(def board-state (atom init-board-state))

(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn coords [$cell]
  [(first (j/data $cell :coords))
   (last (j/data $cell :coords))])

(defn bind-board! [$board]
  (doseq [[coords change-bus] @board-state]
    (-> change-bus
        (b/on-value
         (fn [new-state]
           (let [$sq (grab $board coords)]
             (j/inner $sq (t/render new-state))))))))

(defn main []
  (let [g (t/gameboard board-dimensions)
        l (t/layout g)]
    (-> ($ "#content") (j/inner l))
    (bind-board! ($ g))
    (-> (get @board-state [3 5])
        (b/push {:type :room-key}))))
