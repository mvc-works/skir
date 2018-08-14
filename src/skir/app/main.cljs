
(ns skir.app.main
  (:require [skir.core :as skir]
            [skir.schema :as schema]
            [skir.client :refer [fetch!]]
            [skir.util :refer [clear!]]))

(defn on-request! [req]
  (do (println) (println "Request:" req) {:status 200, :headers {}, :body "hello developer3"}))

(defn try-request! []
  (fetch!
   "http://localhost:4000"
   (fn [response] (println) (println "Response:" (pr-str response)))))

(defn main! [] (skir/create-server! #(on-request! %) {:after-start (fn [] (try-request!))}))

(defn reload! [] (clear!) (println "Reload!") (try-request!))
