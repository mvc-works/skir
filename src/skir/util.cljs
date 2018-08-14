
(ns skir.util (:require [cljs.tools.reader :refer [read-string]]))

(defn clear! []
  (.clear js/console)
  (comment -> js/process .-stdout (.write (read-string "\"\\033c\""))))

(defn delay! [duration task] (js/setTimeout task (* 1000 duration)))
