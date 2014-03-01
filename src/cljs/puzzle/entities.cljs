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
