(ns puzzle.templates
  (:use-macros [dommy.macros :only [deftemplate]]))

(deftemplate blank [& [opts]]
  [:div.square])

(deftemplate render-entity [entity]
  [:div.square
   (cond
    (:icon entity) [:i.fa {:class (name (:icon entity))}]
    :default nil)])

(defn render [entities]
  (let [entity (first (sort-by (fn [e] (or (:zi e) 1000)) entities))]
    (if entity
      (render-entity entity)
      (blank))))

(deftemplate gameboard [[[a b] [c d]] board]
  [:div#gameboard.noselect
   [:table {:border "1px" :border-collapse true
            :data-constraints [a b c d]}
    (for [i (range b d)]
      [:tr {:class (str i)}
       (for [j (range a c)]
         [:td {:class (str j)
               :data-coords (str "[" j "," i "]")}
          (-> (get board [j i])
              :occupants
              render)])])]])

(deftemplate inventory [inventory]
  [:div#inventory.clearfix
   [:div.pull-left.keys.item
    [:i.fa.fa-key] " " [:span.key-count (:keys inventory)]]
   [:div.pull-left.money.item
    [:i.fa.fa-money] " " [:span.money-count (:money inventory)]]
   [:div.pull-right.health.span6
    (concat
     (repeat (:life inventory)
             [:i.fa.fa-heart])
     (repeat (- 5 (:life inventory))
             [:i.fa.fa-heart-o]))]])

(deftemplate game-container []
  [:div#game-container])
