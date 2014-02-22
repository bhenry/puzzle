(ns puzzle.templates
  (:use-macros [dommy.macros :only [deftemplate]]))

;;KINDS OF SQUARES. these are rendered one at a time
(deftemplate blank [& opts]
  [:div.square])

(deftemplate man [& opts]
  [:i.fa.fa-male])

(deftemplate room-key [& opts]
  [:i.fa.fa-key])
;;END OF SQUARES

(defn render [entities]
  (let [entity (first (sort-by (fn [e] (or (:zi e) 10)) entities))]
    (condp = (:type entity)
      :man (man)
      :room-key (room-key)
      (blank))))

(defn find-corners [[x y] [h w]]
  (let [a (- x (rem x w))
        b (- y (rem y h))
        c (+ a w)
        d (+ b h)]
    [[a b] [c d]]))

(deftemplate gameboard [person dimensions board]
  (let [[[a b] [c d]] (find-corners person dimensions)]
    [:div#gameboard.noselect
     [:table {:border "1px" :border-collapse true}
      (for [i (range b d)]
        [:tr {:class (str i)}
         (for [j (range a c)]
           [:td {:class (str j)
                 :data-coords (str "[" j "," i "]")}
            (-> (get board [j i])
                :occupants
                render)])])]]))

(deftemplate layout [content]
  [:div#inner-content
   content])
