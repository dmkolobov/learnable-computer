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

(defn restore-history [history t atime]
  (reduce t
          (:start-state history)
          (subvec (:log history) atime)))

(defn transition [process input]
  (let [{:keys [transition state history]} process]
    (assoc process
           :state (transition state input)
           :history (commit-history history input))))

(defn rewind [process atime]
  (let [{:keys [transition history]} process]
    (assoc process
           :state (restore-history history ptransition atime)
           :history (assoc history :now atime))))

(defn halt [process]
  (assoc process :status :halted))

(defn resume [process]
  (assoc process :status :running))

(defn halted? [process]
  (= :halted (:status process)))

(defn running? [this]
  (= :running (:status process)))
