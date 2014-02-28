(ns puzzle.core
  (:require [dommy.core :as dommy]
            [jayq.core :as j :refer [$]]
            [puzzle.entities :as e]
            [puzzle.handlers :as h]
            [puzzle.input :as i]
            [puzzle.templates :as t]
            [puzzle.view :as v]
            [yolk.bacon :as b]))

(def board-dimensions [9 9]) ;;[h w]
(def user-start [1000 1000]) ;;[x y]

(defn default-point
  ":door? when true should be any xy to warp to."
  [& [options]]
  {:occupants (or (:occupants options) [])
   :blocked? (or (:blocked? options) false)
   :key-required? (or (:key-required? options) false)
   :door? (or (:door? options) false)})

(defn init-board [xy character]
  {xy (default-point {:occupants [character]})})

(def world-model
  {:visible (atom (t/find-corners user-start board-dimensions))
   :points (atom (init-board user-start e/character))
   :user-location (atom user-start)
   :user-inventory (atom {:life 3
                          :keys 0
                          :money 0})
   :user-movements (b/bus)
   :state-changed (b/bus)})

(defn main []
  (let [game (v/init-world-view world-model)]
    (j/inner ($ "#content") (:$container game))

    (b/push (:redraw-inventory game) {:keys 0
                                      :life 4
                                      :money 3}))
  
#_  (comment  (h/handle world-model
                      {:coords [1004 1003]
                       :action :place
                       :entity e/room-key})

            (h/handle world-model
                      {:coords [1006 1006]
                       :action :place
                       :entity e/money})))
