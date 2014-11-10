(ns learnable.core
  (:require [learnable.computer :as computer]
            [learnable.snake :as snake]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def systemx (computer/assemble-grid-computer 16 16 2))
(def game snake/program)

(def app-state (atom (computer/run-program systemx game)))

(om/root
  computer/vcomponent
  app-state
  {:target (. js/document (getElementById "app"))})
