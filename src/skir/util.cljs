
(ns skir.util
  (:require [cljs.tools.reader :refer [read-string]]
            [cljs.core.async.impl.protocols :as async-protocol]))

(defn chan? [x] (satisfies? async-protocol/ReadPort x))

(defn clear! []
  (.clear js/console)
  (comment -> js/process .-stdout (.write (read-string "\"\\033c\""))))

(defn delay! [duration task] (js/setTimeout task (* 1000 duration)))

(defn key->str [v]
  (cond (keyword? v) (name v) (string? v) v (coll? v) (pr-str v) :else (str v)))

(defn promise? [x] (= x (.resolve js/Promise x)))
