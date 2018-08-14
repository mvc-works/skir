
(ns skir.util (:require [cljs.tools.reader :refer [read-string]]))

(defn clear! [] (-> js/process .-stdout (.write (read-string "\"\\033c\""))))
