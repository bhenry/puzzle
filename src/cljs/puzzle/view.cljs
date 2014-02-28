(ns puzzle.view
  (:require [jayq.core :as j :refer [$]]
            [puzzle.templates :as t]
            [yolk.bacon :as b]))

(defn grab [$board [x y]]
  ($ (str "[data-coords='[" x "," y "]']") $board))

(defn visible-world [world]
  (let [[[a b] [c d]] @(:visible world)]
    (select-keys @(:points world)
                 (for [i (range a c)
                       j (range b d)]
                   [i j]))))

(defn find-corners [[x y] [h w]]
  (let [a (- x (rem x w))
        b (- y (rem y h))
        c (+ a w)
        d (+ b h)]
    [[a b] [c d]]))

(defn $gameboard [world]
  ($ (t/gameboard @(:visible world) (visible-world world))))

(defn $inventory [inventory]
  ($ (t/inventory inventory)))

(defn gam [$container]
  (fn [world]
    (j/remove ($ "#gameboard" $container))
    (j/append $container ($gameboard world))))

(defn inv [$container]
  (fn [inventory]
    (j/remove ($ "#inventory" $container))
    (j/prepend $container ($inventory inventory))))

(defn poi [$container]
  (fn [[xy point]]
    (let [$cell (grab $container xy)]
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

    (b/on-value inventory-bus (inv $container))
    (b/on-value gameboard-bus (gam $container))
    (b/on-value point-bus (poi $container))
    
    {:$container $container
     :$gameboard $gameboard
     :$inventory $inventory
     :redraw-inventory inventory-bus
     :redraw-gameboard gameboard-bus
     :redraw-point point-bus}))
