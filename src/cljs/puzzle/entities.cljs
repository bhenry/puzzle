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
   :zi 200})

(defn money [val]
  {:type :money
   :icon :fa-money
   :id :money
   :value val
   :pickup? true
   :zi 100})
