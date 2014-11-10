(ns learnable.display
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn screen-dispatch [x & rest] (:screen-type x))

(defmulti width screen-dispatch)
(defmulti height screen-dispatch)
(defmulti draw-pixel screen-dispatch)
(defmulti vcomponent screen-dispatch)

(defn grid-screen [width height pixel]
  {:screen-type :grid-screen
   :matrix (vec (repeat height (vec (repeat width pixel))))})


(defmethod width :grid-screen [screen]
  (count (first (:matrix screen))))

(defmethod height :grid-screen [screen]
  (count (:matrix screen)))

(defmethod draw-pixel :grid-screen [screen point value]
  (let [[x y] point]
    (assoc-in screen [:matrix y x] value)))

(defn parse-color [pixel]
  (condp = pixel
         :red "red"
         :green "green"
         :blue "blue"
         :black "black"
         :yellow "yellow"))

(defn render-pixel [pixel]
  (dom/div #js {:className (str "pixel " (parse-color pixel))} " "))

(defmethod vcomponent :grid-screen [screen owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/div
             #js {:className "computer-screen screen"}
             (map (fn [line]
                    (apply dom/div
                           #js {:className "scan-line"}
                           (map render-pixel line)))
                  (:matrix screen))))))

