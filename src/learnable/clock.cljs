(ns learnable.clock
  (:require [learnable.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(enable-console-print!)

(defn overclock [hz]
  (println (str "overclocking! hz: " hz))
  (if (< hz 1)
    (* 2 hz)
    (inc hz)))

(defn throttle [hz]
  (println (str "throttling! hz: " hz))
  (if (> hz 1)
    (dec hz)
    (/ hz 2)))

(defn set-indicator-color! [owner color]
  (om/set-state! owner :indicator "red"))

(defn vtimer [hz owner]
  (reify
    om/IInitState
    (init-state [_]
      {:indicator ""})

    om/IWillMount
    (will-mount [_]
      (let [input-queue (om/get-state owner :input-queue)]
        (om/update-state!
              owner
              :timer
              (fn []
                (js/setInterval
                  (fn []
                    (put! input-queue :clock-tick)
                    (set-indicator-color! owner "red")
                    (js/setTimeout (fn [e] set-indicator-color! owner "black") 5))
                  (* 1000 (/ 1.0 hz)))))))

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :timer)))

    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className (str "clock-indicator "
                                    (:indicator state))}))))

(defn vcomponent [clock owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className "computer-clock"}
               (dom/div #js {:className "clock-speed"} (:hz clock))
               (dom/div #js {:className "clock-bulb"}
                  (when (proc/running? (:process clock))
                    (om/build vtimer (:hz clock) {:init-state state})))))))
