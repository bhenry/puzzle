(ns puzzle.entities)

(def character
  {:type :man
   :icon :fa-male
   :id :user
   :zi 0})

(def room-key
  {:type :room-key
   :icon :fa-key
   :id :room-key
   :pickup? true
   :zi 300})

(defn money [val]
  {:type :money
   :icon :fa-money
   :id :money
   :value val
   :pickup? true
   :zi 300})

(def heart
  {:type :life
   :icon :fa-heart
   :id :life
   :pickup? true
   :zi 300})

(def heart-container
  {:type :health
   :icon :fa-heart-o
   :id :health
   :pickup? true
   :zi 300})

(def wall
  {:type :wall
   :class "wall"
   :id :wall
   :zi 501})

(def locked-door
  {:type :door
   :class "door"
   :icon :fa-lock
   :id :door
   :zi 500})

(def open-door
  {:type :door
   :class "door"
   :icon :fa-building-o
   :id :door
   :zi 500})
