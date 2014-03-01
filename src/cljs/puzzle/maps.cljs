(ns puzzle.maps
  (:require [puzzle.entities :as e]))

(defn point
  ":door? when true should be any xy to warp to."
  [& [options]]
  {:occupants (or (:occupants options) [])
   :blocked? (or (:blocked? options) false)
   :locked? (or (:locked? options) false)
   :door? (or (:door? options) false)})

(defn init-board [xy character]
  {xy (point {:occupants [character]})})

(defn wall-points [[x y] ori dis]
  (if (= ori :vertical)
    (for [i (range y (+ dis y))] [x i])
    (for [i (range x (+ dis x))] [i y])))

(defn wall [xy orientation distance]
  (into {}
        (for [wp (wall-points xy orientation distance)]
          [wp (point {:blocked? true
                      :occupants [e/wall]})])))

(defn door [xy ab & [{:keys [locked?] :as opts}]]
  {xy (point {:door? ab
              :occupants (remove nil? [(if locked?
                                         e/locked-door
                                         e/open-door)])
              :locked? locked?
              :blocked? locked?})
   ab (point {:door? xy
              :occupants (remove nil? [(if locked?
                                         e/locked-door
                                         e/open-door)])
              :locked? locked?
              :blocked? locked?})})
