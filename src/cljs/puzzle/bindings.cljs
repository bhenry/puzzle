(ns puzzle.bindings
  (:require [jayq.core :refer [$] :as j]
            [yolk.bacon :as b]
            [yolk.ui :as ui]))

(defn model
  ([]
   (js/Bacon.$.Model))
  ([v]
   (js/Bacon.$.Model (clj->js v))))

(defn on-values
  ([a b fn]
   (js/Bacon.onValues a b fn))
  ([a b c fn]
   (js/Bacon.onValues a b c fn))
  ([a b c d fn]
   (js/Bacon.onValues a b c d fn))
  ([a b c d e fn]
   (js/Bacon.onValues a b c d e fn))
  ([a b c d e f fn]
   (js/Bacon.onValues a b c d e f fn)))

(defn combine-with [& streams-with-fn]
  (let [f (last streams-with-fn)
        streams (butlast streams-with-fn)]
    (apply js/Bacon.combineWith f streams)))

(defn zip-with [& streams-with-fn]
  (let [f (last streams-with-fn)
        streams (butlast streams-with-fn)]
    (apply js/Bacon.zipWith f streams)))

(def combine-model (comp js/Bacon.$.Model.combine clj->js))

(defn ->clj [obs]
  (-> obs
      (b/map #(js->clj % :keywordize-keys true))))

(defn no-dups [m]
  (-> m
      ->clj
      (b/skip-duplicates =)))

(defn log-pr [m]
  (-> m
      (b/map pr-str)
      b/log))

(defn lens [model path]
  (.lens model path))

(defn modify [model f]
  (.modify model (comp clj->js f)))

(defn add-source [model stream]
  (.addSource model stream))

(defn apply-functions [model fn-stream]
  (.apply model fn-stream))

(defn bind
  "left will take the value of right"
  [left right]
  (.bind left right))

(defn set-value [model value]
  (.set model (clj->js value)))

(def text-field-value js/Bacon.$.textFieldValue)

(defn $text-field-value
  ([sel]
   (text-field-value ($ sel)))
  ([sel default]
   (text-field-value ($ sel) default)))

(def select-value js/Bacon.$.selectValue)

(defn assign [obs el attr]
  (.assign obs el attr))

(defn clickE
  ([$elem]
   (.clickE $elem))
  ([$elem selector]
   (.clickE $elem selector)))

(defn clickE-prevent [$elem]
  (-> (clickE $elem)
      (b/do-action j/prevent)))

(defn clickE-enabled [$elem]
  (-> $elem
      clickE-prevent
      (b/filter #(not (j/has-class $elem "disabled")))))

(defn keyupE
  ([$elem]
   (.keyupE $elem))
  ([$elem selector]
   (.keyupE $elem selector)))

(defn keypressE [$elem]
  (.keypressE $elem))

(defn keydownE [$elem]
  (.keydownE $elem))

(defn changeE [$elem]
  (.changeE $elem))

(defn mousemoveE [$elem]
  (.mousemoveE $elem))

(defn mouseupE [$elem]
  (.mouseupE $elem))

(defn mousedownE [$elem]
  (.mousedownE $elem))

(defn bind-click [$elem f]
  (-> (clickE $elem)
      (b/do-action j/prevent)
      (b/on-value f)))

(defn key-stream [$elem key-code]
  (-> (keyupE $elem)
      (b/filter #(= (.-keyCode %) key-code))))
