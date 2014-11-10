(ns learnable.keyboard
  (:require [cljs.core.async :as async :refer [put!]]))

(defn parse-keystroke [e]
  (condp = (.-keyCode e)
         0 :key-up
         1 :key-right
         2 :key-down
         3 :key-left
         4 :key-esc
         5 :key-space
         6 :key-plus
         7 :key-minus))

(def key-bindings
  {:key-esc :halt
   :key-space :run
   :key-plus :overclock
   :key-minus :throttle})

(defn is-key-binding? [ks]
  (not= nil (find key-bindings ks)))

(defn load-key-binding [ks]
  (last (find key-bindings ks)))

(defn controller [computer]
  (let [{:keys [input-queue interrupt]} computer]
    (fn [e]
      (let [ks (parse-keystroke e)]
        (if (is-key-binding? ks)
          (put! interrupt (load-key-binding ks))
          (put! input-queue ks))))))
