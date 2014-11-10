(ns learnable.core
  (:require [learnable.vcomputer :as vcomputer]
            [learnable.snake :as snake]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def systemx (vcomputer/assemble-grid-computer 16 16 2))

(def app-state (atom (vcomputer/run-program systemx snake/snake-program)))

(om/root
  vcomputer/vcomponent
  app-state
  {:target (. js/document (getElementById "app"))})
