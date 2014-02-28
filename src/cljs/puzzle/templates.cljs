(ns puzzle.templates
  (:use-macros [dommy.macros :only [deftemplate]]))

;;KINDS OF SQUARES. these are rendered one at a time
(deftemplate blank [& [opts]]
  [:div.square])

(deftemplate render-entity [entity]
  [:div.square
   (cond
    (:icon entity) [:i.fa {:class (name (:icon entity))}]
    :default nil)])
;;END OF SQUARES

(defn render [entities]
  (let [entity (first (sort-by (fn [e] (or (:zi e) 1000)) entities))]
    (if entity
      (render-entity entity)
      (blank))))

(defn find-corners [[x y] [h w]]
  (let [a (- x (rem x w))
        b (- y (rem y h))
        c (+ a w)
        d (+ b h)]
    [[a b] [c d]]))

(deftemplate gameboard [[[a b] [c d]] board]
  (js/console.log (pr-str board))
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
  [:div#inventory
   [:div.pull-left.keys.item
    [:i.fa.fa-key] " " [:span.key-count 0]]
   [:div.pull-left.money.item
    [:i.fa.fa-money] " " [:span.money-count 0]]
   [:div.pull-right.health.span6
    (repeat 3 [:i.fa.fa-heart])]])

(deftemplate layout [content]
  [:div#inner-content content])
