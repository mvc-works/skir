
(ns skir.util (:require [cljs.tools.reader :refer [read-string]]))

(defn clear! []
  (.clear js/console)
  (comment -> js/process .-stdout (.write (read-string "\"\\033c\""))))
