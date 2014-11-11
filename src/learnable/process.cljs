(ns learnable.process)

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
     :history {:log [] :start-state start-state :now 0}}))

(defn commit-history [history input]
  (let [{:keys [log now]} history]
    (assoc history
           :log (conj log input)
           :now (inc now))))

(defn transition [process input]
  (let [{:keys [transition state history]} process]
    (assoc process
           :state (transition state input)
           :history (commit-history history input))))

(defn rewind [process atime]
  (let [{:keys [transition history]} process]
    (assoc process
           :state (reduce transition
                          (:start-state history)
                          (subvec (:log history) 0 (inc atime)))
           :history (assoc history :now atime))))

(defn halt [process]
  (assoc process :status :halted))

(defn resume [process]
  (let [history (:history process)
        {:keys [log now]} history]
    (assoc process
           :status :running
           :history (assoc history
                           :log
                           (vec (subvec log 0 (inc now)))))))

(defn halted? [process]
  (= :halted (:status process)))

(defn running? [process]
  (= :running (:status process)))
