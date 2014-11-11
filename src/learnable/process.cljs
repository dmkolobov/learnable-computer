(ns learnable.process
  (:require [learnable.statelog :as statelog]))

(defn launch [program screen]
  (let [start-state ((:boot program) screen)
        {:keys [on-clock on-keyboard]} (:transitions program)]
    {:status :halted
     :draw (:draw program)
     :state start-state
     :transition (fn [state input]
                   (let [t (if (= :clock-tick input)
                             (fn [state _] (on-clock state))
                             on-keyboard)]
                     (t state input)))
     :log (statelog/create start-state)}))

(defn transition [process input]
  (let [{:keys [transition state log]} process]
    (assoc process
           :state
             (transition state input)
           :log
             (statelog/commit log input))))

(defn halt [process]
  (assoc process :status :halted))

(defn resume [process]
  (let [log (:log process)]
    (assoc process
           :status
             :running
           :log
             (statelog/trim log))))

(defn halted? [process]
  (= :halted (:status process)))

(defn running? [process]
  (= :running (:status process)))
