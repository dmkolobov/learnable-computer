(ns learnable.machine)

(defprotocol StateMachine
  ""
  (transition [this input] "")
  (rewind [this input] "")
  (halt [this] "")
  (resume [this] "")
  (halted? [this] "")
  (running? [this] ""))
