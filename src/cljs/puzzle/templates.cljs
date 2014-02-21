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

(defn render [opts]
  (condp = (:type opts)
    :man (man)
    :room-key (room-key)
    (blank)))

(deftemplate gameboard [[h w]]
  [:div#gameboard.noselect
   [:table {:border "1px" :border-collapse true}
    (for [i (range h)]
      [:tr {:class (str i)}
       (for [j (range w)]
         [:td {:class (str j)
               :data-coords (str "[" j "," i "]")}
          (blank)])])]])

(deftemplate layout [content]
  [:div#inner-content
   content])
