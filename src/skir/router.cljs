
(ns skir.router (:require [clojure.string :as string]))

(defn expand-rule [rule-string]
  (map
   (fn [x] (if (string/starts-with? x ":") (keyword (subs x 1)) x))
   (string/split rule-string "/")))

(defn match-chunks
  ([segments rule]
   (match-chunks
    {:matches? false, :contains? false, :rest nil, :data {}, :message nil}
    segments
    (expand-rule rule)))
  ([result segments rule]
   (cond
     (and (empty? segments) (empty? rule)) (merge result {:matches? true})
     (and (empty? segments) (not (empty? rule))) (merge result {:result rule})
     (and (not (empty? segments)) (empty? rule))
       (merge result {:contains? true, :result segments})
     :else
       (let [s0 (first segments), r0 (first rule)]
         (if (keyword? r0)
           (recur (assoc-in result [:data r0] s0) (rest segments) (rest rule))
           (if (= s0 r0)
             (recur result (rest segments) (rest rule))
             (merge result {:message [s0 r0]})))))))

(defn match-path [real-path rule-path]
  (let [segments (filter #(not (string/blank? %)) (string/split real-path "/"))]
    (match-chunks segments rule-path)))
