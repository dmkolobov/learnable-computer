(ns learnable.clock
  (:require [learnable.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :as async-mac :refer [go]]))

(enable-console-print!)

(def max-hertz 20)

(defn overclock [hz]
  (if (<= hz max-hertz)
    (if (< hz 1)
      (* 2 hz)
      (inc hz))
    hz))

(defn throttle [hz]
  (if (> hz 1)
    (dec hz)
    (/ hz 2)))

(defn vtimer [hz owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [wavelength (* 1000 (/ 1.0 hz))]
        (om/set-state!
          owner
          :timer
          (js/setInterval
                (fn []
                  (put! (om/get-state owner :wire) 1)
                  (js/setTimeout (fn []
                                   (put! (om/get-state owner :wire) 0))
                                 (/ wavelength 2)))
                wavelength))))

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
          (let [bit (<! (om/get-state owner :wire))]
            (when (= bit 1)
              (put! (om/get-state owner :input-queue) :clock-tick))
            (om/set-state! owner
                           :indicator
                           (if (= bit 1)
                             "red"
                             "black"))
            (recur)))))

    om/IRenderState
    (render-state [_ state]
      (dom/div #js {:className "computer-clock"}
               (dom/div #js {:className "clock-speed screen"} (str (:hz computer) "Hz"))
               (dom/div #js {:className (str "clock-bulb " (:indicator state))})
               (when (proc/running? (:process computer))
                 (om/build vtimer
                           (:hz computer)
                           {:init-state {:wire (:wire state)}}))))))







