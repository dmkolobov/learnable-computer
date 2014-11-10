(ns learnable.display
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defprotocol Screen
  "A screen abstraction that provides basic drawing facilities."
  (width [this] "Find the width of the screen.")
  (height [this] "Find the height of the screen.")
  (draw-pixel [this point color] "Draw a pixel on the screen."))

(defrecord GridScreen [matrix]
  Screen
  (width [this]
    (count (first matrix)))

  (height [this]
    (count matrix))

  (draw-pixel [this point color]
    (let [[x y] point]
      (assoc matrix [y x] color))))

(defn grid-screen [width height pixel]
  (vec (repeat height (vec (repeat width pixel)))))

(defn parse-color [pixel]
  (condp = pixel
         :red "red"
         :green "green"
         :blue "blue"
         :black "black"
         :yellow "yellow"))

(defn render-pixel [pixel]
  (dom/div #js {:className (str "pixel " (parse-color pixel))} " "))

(defn vcomponent [screen owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/div
             #js {:className "computer-screen"}
             (map (fn [line]
                    (apply dom/div
                           #js {:className "scan-line"}
                           (map render-pixel line))))))))

