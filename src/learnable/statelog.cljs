(ns learnable.statelog
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(defn create [start-state]
  {:entries []
   :start-state start-state
   :now 0})

(defn commit [log entry]
  (let [{:keys [entries now]} log]
    (assoc log
           :entries (conj entries entry)
           :now (inc now))))

(defn trim [log]
  (let [{:keys [entries now]} log]
    (assoc log :entries (subvec entries 0 now))))

(defn restore-snapshot! [process atime]
  (om/transact!
    process
    (fn [process]
      (let [{:keys [transition log]} process]
        (assoc process
               :state
                 (reduce transition
                         (:start-state log)
                         (subvec (:entries log) 0 atime))
               :log
                 (assoc log :now atime))))))

;; [init-state] -> :tick -> :tick -> :key-up -> :tick
;;      0            1        2         3        4

(defn log-entry-component [process atime]
  (let [{:keys [entries now]} (:log process)]
    (dom/li
      #js {:className (if (= atime now) "highlighted" "")}
      (dom/a
        #js {:onClick (partial restore-snapshot! process atime)}
        (if (= atime 0)
          "0 : start-state"
          (str atime " : " (entries (dec atime))))))))

(defn log-list [log]
  (let [{:keys [entries]} log]
    (reduce (fn [timeline atime]
              (cons [atime (entries (dec atime))] timeline))
            (list [0 "start"])
            (range 1 (inc (count entries))))))

(defn log-component [process owner]
  (reify
    om/IRender
    (render [_]
      (let [log (:log process)]
        (dom/div
          #js {:className "screen inspector"}
          (dom/div nil (str "now: " (:now log)))
          (dom/hr nil)
            (apply
              dom/ul
              #js {:className "computer-history"}
              (map (fn [item]
                     (let [[atime label] item]
                       (if (= :halted (:status process))
                         (dom/li
                            nil
                            (dom/a
                              #js {:onClick (partial restore-snapshot! process atime)}
                                  (if (= (:now log) atime)
                                    (str "[X] - " atime " - " label)
                                    (str "[ ] - " atime " - " label))))
                         (dom/li
                            nil
                            (str atime " - " label)))))
                   (log-list log))))))))
