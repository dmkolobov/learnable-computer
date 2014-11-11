(ns learnable.process
  (:require [learnable.state-log :as state-log]))

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
     :log (state-log/create start-state)}))

(defn transition [process input]
  (let [{:keys [transition state log]} process]
    (assoc process
           :state
             (transition state input)
           :log
             (state-log/commit log input))))

(defn halt [process]
  (assoc process :status :halted))

(defn resume [process]
  (let [log (:log process)]
    (assoc process
           :status
             :running
           :log
             (state-log/trim log))))

(defn halted? [process]
  (= :halted (:status process)))

(defn running? [process]
  (= :running (:status process)))
