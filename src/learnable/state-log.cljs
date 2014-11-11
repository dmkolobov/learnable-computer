(ns learnable.statelog
  (:require [learnable.process :as proc]
            [om.core :as om :include-macros true]
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
          "start-state"
          (str atime " : " (entries (dec atime))))))))

(defn log-component [process owner]
  (reify
    om/IRender
    (render [_]
      (let [log (:log process)]
        (dom/div
          #js {:className "screen"}
          (dom/div nil (str "now: " (:now log)))
          (dom/hr nil)
          (when (proc/running? process)
            (apply
              dom/ul
              #js {:className "computer-history"}
              (reduce (fn [timeline atime]
                        (cons (log-entry-component process atime) timeline))
                      (list (log-entry-component process 0))
                      (range 1 (inc (count log)))))))))))

