(ns puzzle.input
  (:require [jayq.core :as j]
            [yolk.bacon :as b]))

(defn read-key-input [e]
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
      (b/throttle 200)
      (b/map (fn [e] (read-key-input e)))))

(defn keyboard-control [$body world]
  (-> (arrow-stream $body)
      (b/on-value
       (fn [dir]
         (b/push (:user-movements world) dir)))))
