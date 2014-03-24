(ns puzzle.input
  (:require [jayq.core :as j :refer [$]]
            [yolk.bacon :as b]))

(defn- read-key-input [e]
  (let [k (.-which e)]
    (condp = k
      38 :north
      40 :south
      37 :west
      39 :east
      :sit)))

(defn arrow-stream [$elem]
  (-> (.keydownE $elem)
      (b/filter (fn [e] (not= :sit (read-key-input e))))
      (b/do-action j/prevent)
      (b/map (fn [e] (read-key-input e)))))

(defn- target [e]
  (-> ($ (aget e "target"))
      (j/closest "td")))

(defn- direction [[x1 y1] [x2 y2]]
  (let [x (cond (< x1 x2) :east
                (> x1 x2) :west
                :equal nil)
        y (cond (< y1 y2) :south
                (> y1 y2) :north
                :equal nil)]
    (cond
     (and (= x :east) (= y :north)) :northeast
     (and (= x :east) (= y :south)) :southeast
     (and (= x :west) (= y :north)) :northwest
     (and (= x :west) (= y :south)) :southwest
     (and (not x) (= y :north)) :north
     (and (not x) (= y :south)) :south
     (and (not y) (= x :east)) :east
     (and (not y) (= x :west)) :west
     (and (not x) (not y)) :sit
     :else :sit)))

(defn- read-mouse-input [loc]
  (fn [$td]
    (if-let [coords (j/data $td "coords")]
      (direction @loc coords)
      :sit)))

(defn click-stream [loc $elem]
  (-> (.clickE $elem)
      (b/map target)
      (b/map (read-mouse-input loc))
      (b/filter #(not= :sit %))))
