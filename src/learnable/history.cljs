(ns learnable.history
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(defn vcomponent [log owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [control]}]
      (apply
        dom/ul
        #js {:className "computer-history"}
        (reduce (fn [timeline atime]
                  (let [input (log atime)]
                    (cons (dom/li
                            nil
                            (dom/a #js {:onClick (fn [e] (put! control atime))}
                                   (dom/span #js {:className "time-index"}
                                             atime)
                                   (str input)))
                          timeline)))
                (list)
                (range (count log)))))))

