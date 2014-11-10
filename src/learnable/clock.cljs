(ns learnable.clock
  (:require [learnable.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :as async-mac :refer [go]]))

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

(defn vtimer [hz owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (om/set-state!
        owner
        :timer
        (js/setInterval
              (fn []
                (put! (om/get-state owner :wire) "red")
                (js/setTimeout (fn []
                                 (put! om/get-state owner :wire "black"))
                               30))
              (* 1000 (/ 1.0 hz)))))

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :timer)))

    om/IRenderState
    (render-state [_ _] (dom/span nil))))

(defn vclock [computer owner]
  (reify
    om/IInitState
    (init-state [_]
      {:wire (chan)
       :indicator "black"})

    om/IWillMount
    (will-mount [_]
      (go
        (loop []
          (let [color (<! (om/get-state owner :wire))]
            (when (= color "red")
              (put! (om/get-state owner :input-queue) :clock-tick))
            (om/set-state! owner :indicator color)
            (recur)))))

    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className "computer-clock"}
               (dom/div #js {:className "clock-speed"} (:hz computer))
               (dom/div #js {:className (str "clock-bulb " (:indicator state))})
               (when (proc/running? (:process computer))
                 (om/build vtimer
                           (:hz computer)
                           {:init-state {:wire (:wire state)}}))))))







