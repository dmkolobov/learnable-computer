(ns learnable.history
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(defn vcomponent [log owner]
  (reify
    IRenderState
    (render-state [_ {:keys [control]}]
      (apply
        dom/ul
        #js {:className "computer-history"}
        (reduce-kv (fn [timeline atime input]
                     (cons (dom/li
                              #js {:onClick (fn [e] (put! control atime))}
                              input)
                           timeline))
                   (list)
                   log)))))
