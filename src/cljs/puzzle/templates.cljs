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
  (let [a (- x (rem x h))
        b (- y (rem y w))
        c (+ a h)
        d (+ b w)]
    [[a b] [c d]]))

(deftemplate gameboard [person dimensions]
  (let [[[a b] [c d]] (find-corners person dimensions)]
    [:div#gameboard.noselect
     [:table {:border "1px" :border-collapse true}
      (for [i (range a c)]
        [:tr {:class (str i)}
         (for [j (range b d)]
           [:td {:class (str j)
                 :data-coords (str "[" j "," i "]")}
            (blank)])])]]))

(deftemplate layout [content]
  [:div#inner-content
   content])
