(ns learnable.clock
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(defn overclock [hz]
  (if (< hz 1)
    (* 2 hz)
    (inc hz)))

(defn throttle [hz]
  (if (> hz 1)
    (dec hz)
    (/ hz 2)))

(defn vcomponent [hz owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [input-queue (om/get-state owner :input-queue)]
        (om/update-state!
              owner
              :timer
              (fn []
                (js/setInterval (fn [] (put! input-queue :clock-tick))
                                (* 1000 (/ 1.0 hz)))))))

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :timer)))

    om/IRenderState
    (render-state [_ state]
      (dom/span #js {:className "computer-clock"}))))
