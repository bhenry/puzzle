(ns puzzle.view
  (:require [jayq.core :as j :refer [$]]
            [puzzle.templates :as t]
            [yolk.bacon :as b]))

(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn visible-points [world]
  (let [[[a b] [c d]] @(:visible world)]
    (select-keys @(:points world)
                 (for [i (range a c)
                       j (range b d)]
                   [i j]))))

(defn $gameboard [world]
  ($ (t/gameboard @(:visible world) (visible-points world))))

(defn $inventory [inventory]
  ($ (t/inventory inventory)))

(defn gam [$container $old-gameboard]
  (fn [world]
    (j/remove $old-gameboard)
    (j/append $container ($gameboard world))))

(defn inv [$container $old-inventory]
  (fn [inventory]
    (j/remove $old-inventory)
    (j/prepend $container ($inventory inventory))))

(defn poi [$gameboard]
  (fn [[xy point]]
    (let [$cell (grab $gameboard xy)]
      (j/inner $cell (-> point :occupants t/render)))))

(defn init-world-view [world]
  (let [$container ($ (t/game-container))
        $inv ($inventory @(:user-inventory world))
        $gmb ($gameboard world)
        inventory-bus (b/bus)
        gameboard-bus (b/bus)
        point-bus (b/bus)]
    (j/prepend $container $inv)
    (j/append $container $gmb)

    (b/on-value inventory-bus (inv $container $inv))
    (b/on-value gameboard-bus (gam $container $gmb))
    (b/on-value point-bus (poi $gmb))
    
    {:$container $container
     :$gameboard $gameboard
     :$inventory $inventory
     :redraw-inventory inventory-bus
     :redraw-gameboard gameboard-bus
     :redraw-point point-bus}))
