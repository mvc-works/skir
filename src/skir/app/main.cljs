
(ns skir.app.main
  (:require [skir.core :as skir]
            [skir.schema :as schema]
            [skir.client :refer [fetch!]]
            [skir.util :refer [clear! delay!]]
            [respo-router.parser :refer [parse-address]]))

(def router-rules {"home" [], "async" []})

(defn render! [req]
  (do
   (println)
   (println "Requests:" req)
   (let [router (parse-address (:url req) router-rules), page (get-in router [:path 0])]
     (case (:name page)
       "async"
         (fn [send!]
           (delay! 3 #(send! {:status 200, :headers {}, :body "slow response finished!"})))
       {:status 200, :headers {}, :body "hello developer!"}))))

(defn try-request! []
  (fetch!
   "http://localhost:4000"
   (fn [response] (println) (println "Response:" (pr-str response))))
  (fetch!
   "http://localhost:4000/async"
   (fn [response] (println) (println "Response:" (pr-str response)))))

(defn main! [] (skir/create-server! #(render! %) {:after-start (fn [] (try-request!))}))

(defn reload! [] (clear!) (println "Reload!") (try-request!))
