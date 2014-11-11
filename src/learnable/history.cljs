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
        #js {:className "computer-history screen"}
        (reduce (fn [timeline atime]
                  (let [input (log atime)]
                    (cons (dom/li
                            nil
                            (dom/a #js {:onClick (fn [e] (put! control atime))}
                                   (str atime " : "input)))
                          timeline)))
                (list)
                (range (count log)))))))



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
                          nil
                          (dom/a #js {:onClick (fn [e] (put! control atime))
                                      :className (if (= atime now)
                                                   "highlighted"
                                                   "")}
                                 (str atime " : " entry)))
                        timeline)))
                  (list)
                  (range (count log))))))))
