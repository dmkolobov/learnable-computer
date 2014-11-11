(ns learnable.history
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(defn vcomponent [process owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [control]}]
      (apply
        dom/ul
        #js {:className "computer-history screen"}
        (let [{:keys [log now]} (:history process)]
          (reduce (fn [timeline atime]
                    (let [entry (log atime)]
                      (cons
                        (dom/li
                          {:className (if (= atime now)
                                                   "highlighted"
                                                   "")}
                          (dom/a #js {:onClick (fn [e] (put! control atime))}
                                 (str atime " : " entry)))
                        timeline)))
                  (list)
                  (range (count log))))))))
