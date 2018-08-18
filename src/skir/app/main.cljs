
(ns skir.app.main
  (:require [skir.core :as skir]
            [skir.schema :as schema]
            [skir.client :refer [fetch!]]
            [skir.util :refer [clear! delay!]]
            [respo-router.parser :refer [parse-address]]
            ["fs" :as fs]
            ["path" :as path]
            ["cheerio" :default cheerio]))

(def router-rules {"home" [], "async" [], "html" [], "json" [], "edn" []})

(defn render! [req]
  (do
   (println)
   (println "Requests:" req)
   (let [router (parse-address (:url req) router-rules), page (get-in router [:path 0])]
     (case (:name page)
       "async"
         (fn [send!]
           (delay! 3 #(send! {:status 200, :headers {}, :body "slow response finished!"})))
       "json"
         {:status 200,
          :headers {:Content-Type :application/json},
          :body (.stringify js/JSON (clj->js {:status :ok, :message "good"}))}
       "edn"
         {:status 200,
          :headers {:Content-Type :application/edn},
          :body (pr-str {:status :ok, :message "good"})}
       "html"
         {:status 200,
          :headers {:Content-Type :text/html},
          :body "<div><h2>Heading</h2> this is HTML</div>"}
       {:status 200, :headers {}, :body "hello developer!"}))))

(defn try-request! []
  (fetch!
   "http://localhost:4000"
   (fn [response] (println) (println "Response:" (pr-str response))))
  (fetch!
   "http://localhost:4000/async"
   (fn [response] (println) (println "Response:" (pr-str response)))))

(defn run-task! []
  (comment skir/create-server! #(render! %) {:after-start (fn [] (try-request!))})
  (comment try-request!)
  (let [html (fs/readFileSync (path/join js/__dirname "hn.html") "utf8")]
    (.log js/console cheerio)
    (.load cheerio html)
    (.log js/console (cheerio ".comment"))))

(defn main! [] (run-task!))

(defn reload! [] (clear!) (println "Reload!") (run-task!))
