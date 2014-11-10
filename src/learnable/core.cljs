(ns learnable.core
  (:require [learnable.computer :as computer]
            [learnable.snake :as snake]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def systemx (computer/assemble-grid-computer 16 16 2))

(def app-state (atom (computer/run-program systemx snake/snake-program)))

(om/root
  computer/vcomponent
  app-state
  {:target (. js/document (getElementById "app"))})
