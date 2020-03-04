
(ns skir.app.main
  (:require [skir.core :as skir]
            [skir.schema :as schema]
            [skir.client :refer [fetch!]]
            [skir.util :refer [clear! delay!]]
            [respo-router.parser :refer [parse-address]]
            ["fs" :as fs]
            ["path" :as path]
            [cljs.core.async :refer [chan <! >! timeout]]
            [skir.router :refer [match-path]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def router-rules
  {"home" [], "callback" [], "html" [], "json" [], "edn" [], "promise" [], "channel" []})

(defn render! [req]
  (do
   (println)
   (comment println "Requests:" (pr-str req))
   (comment println "Url:" (:url req))
   (println req)
   (let [router (parse-address (:url req) router-rules)
         page (get-in router [:path 0])
         parse-result (match-path (:url req) "a/:b")]
     (println "Parsed:" parse-result)
     (case (:name page)
       "callback"
         (fn [send!]
           (delay! 3 #(send! {:code 200, :headers {}, :body "slow response finished!"})))
       "json"
         {:code 200,
          :headers {:Content-Type :application/json},
          :body (.stringify js/JSON (clj->js {:status :ok, :message "good"}))}
       "edn"
         {:code 200,
          :headers {:Content-Type :application/edn},
          :body (pr-str {:status :ok, :message "good"})}
       "html"
         {:code 200,
          :headers {:Content-Type :text/html},
          :body "<div><h2>Heading</h2> this is HTML</div>"}
       "promise"
         (js/Promise.
          (fn [resolve reject]
            (delay!
             3
             (fn [] (resolve {:code 200, :headers {}, :body "Message from promise"})))))
       "channel"
         (let [delayed-message (chan)]
           (go
            (<! (timeout 4000))
            (>! delayed-message {:code 200, :headers {}, :body "message from channel"}))
           delayed-message)
       nil {:code 200, :message "OK, default page", :headers {}, :body "Home page"}
       {:code 404,
        :message "Page not found",
        :headers {},
        :body (str "404 page for " (pr-str page))}))))

(defn try-request! []
  (fetch!
   "http://localhost:4000"
   (fn [response] (println) (println "Response:" (pr-str response))))
  (fetch!
   "http://localhost:4000/callback"
   (fn [response] (println) (println "Response:" (pr-str response))))
  (fetch!
   "http://localhost:4000/promise"
   (fn [response] (println) (println "Response:" (pr-str response))))
  (fetch!
   "http://localhost:4000/channel"
   (fn [response] (println) (println "Response:" (pr-str response)))))

(defn run-task! [] (comment try-request!))

(defn main! []
  (skir/create-server!
   #(render! %)
   (comment
    {}
    (:after-start (fn [options] (println "options" options) (comment run-task!))))))

(defn reload! [] (clear!) (println "Reload!") (run-task!))
