(ns learnable.computer
  (:require [learnable.display :as display]
            [learnable.machine :as machine]
            [learnable.clock :as clock]
            [learnable.process :as proc]
            [learnable.keyboard :as keyboard]
            [learnable.history :as history]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [chan put! <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn assemble-grid-computer [screen-width screen-height hz]
  {:screen (display/grid-screen screen-width screen-height :black)
   :hz hz})

(defn run-program [computer program]
  (assoc computer :process (proc/launch program (:screen computer))))

(defn get-frame [computer]
  (let [{:keys [screen process]} computer]
    ((:draw process) screen (:state process))))

(defn on-input! [computer! input]
  (when (= :running (deref (get-in computer! [:process :status])))
    (om/transact! computer!
                  :process
                  (fn [process] (machine/transition process input)))))

(defn on-signal! [computer! signal]
  (condp = signal
         :halt (om/transact! computer! :process machine/halt)
         :resume (om/transact! computer! :process machine/resume)
         :overclock (om/transact! computer! :hz clock/overclock))
         :throttle (om/transact! computer! :hz clock/throttle))

(defn vcomponent [computer owner]
  (reify
    om/IInitState
    (init-state [_]
      {:input-queue (chan)
       :interrupt (chan)
       :control (chan)})

    om/IWillMount
    (will-mount [_]
      (go (loop []
        (let [signal (<! (om/get-state owner :interrupt))]
          (on-signal! computer signal)
          (recur))))
      (go (loop []
        (let [input (<! (om/get-state owner :input-queue))]
          (on-input! computer input)
          (recur))))
      (go (loop []
        (let [atime (<! (om/get-state owner :control))]
          (om/transact! computer :process (fn [p] (machine/rewind p atime)))
          (recur))))
      (js/setTimeout
        (fn []
          (put! (om/get-state owner :interrupt) :resume))
        1000))

    om/IRenderState
    (render-state [_ {:keys [input-queue interrupt control]}]

      (dom/div #js {}
        (when (machine/running? (:process computer))
              (om/build clock/vcomponent
                        (:hz computer)
                        {:init-state {:input-queue input-queue}}))

        (dom/div #js {:onKeyDown (keyboard/controller computer)
                      :tabStop 0
                      :className "computer"}
          (om/build display/vcomponent (get-frame computer)))

        (when (machine/halted? (:process computer))
          (om/build history/vcomponent
                    (get-in computer [:process :history :log])
                    {:init-state {:control control}}))))))
