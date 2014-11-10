(ns learnable.keyboard
  (:require [cljs.core.async :as async :refer [put!]]))

(defn parse-keystroke [e]
  (condp = (.-keyCode e)
         0 :key-up
         0 :key-right
         0 :key-down
         0 :key-left
         0 :key-esc
         0 :key-space
         0 :key-plus
         0 :key-minus))

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
