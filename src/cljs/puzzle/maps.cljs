(ns puzzle.maps)

(defn point
  ":door? when true should be any xy to warp to."
  [& [options]]
  {:occupants (or (:occupants options) [])
   :blocked? (or (:blocked? options) false)
   :key-required? (or (:key-required? options) false)
   :door? (or (:door? options) false)})

(defn init-board [xy character]
  {xy (point {:occupants [character]})})

