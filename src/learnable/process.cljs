(ns learnable.process
  (:require [learnable.machine :as machine]))

(defn commit [history input]
  (let [{:keys [log now]} history]
    (assoc history
           :log (conj log input)
           :now (inc now))))

(defn restore [history t atime]
  (reduce t
          (:start-state history)
          (subvec (:log history) atime)))

(defrecord Process [status state ptransition history]
  machine/StateMachine
  (transition [this input]
    (assoc this
           :state (ptransition state input)
           :history (commit history input)))

  (rewind [this atime]
    (assoc this
           :state (restore history ptransition atime)
           :history (assoc history :now atime)))

  (halt [this]
    (assoc this :status :halted))

  (resume [this]
    (assoc this
           :status :running
           :history (let [{:keys [log now]} history]
                      (assoc history :log (subvec log (inc now))))))

  (halted? [this]
    (= status :halted))

  (running? [this]
    (= status :running)))

(defn launch [program screen]
  (let [start-state ((:boot program) screen)
        {:keys [on-clock on-keyboard]} (:transitions program)]
    (Process. :halted
              start-state
              (fn [state input]
                (let [t (if (= input :clock-tick)
                          (fn [_] on-clock)
                          on-keyboard)]
                  (t state input)))
              {:log [] :start-state start-state :now 0})))
