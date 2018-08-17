
(ns skir.client
  (:require ["http" :as http]
            ["https" :as https]
            [cljs.reader :refer [read-string]]
            [clojure.string :as string]))

(defn collect-response-data! [res cb!]
  (let [*raw-data (atom "")]
    (.setEncoding res "utf8")
    (.on
     res
     "data"
     (fn [chunk]
       (println "get data chunk of length" (count chunk))
       (swap! *raw-data str chunk)))
    (.on res "end" (fn [] (cb! @*raw-data)))))

(defn delete! [url options cb] )

(defn get! [url options cb]
  (let [handler (fn [res]
                  (collect-response-data!
                   res
                   (let [content-type (aget (.-headers res) "Content-Type")]
                     (fn [text]
                       (cb
                        {:status (.-statusCode res),
                         :headers {},
                         :body (case content-type
                           "application/edn" (read-string text)
                           "application/json" (.parse js/JSON text)
                           text)})))))
        task (if (string/starts-with? url "https://")
               (.get https url handler)
               (.get http url handler))]
    (.on
     task
     "error"
     (fn [error]
       (let [on-error (:on-error options)]
         (if (fn? on-error) (on-error error) (.error js/console error)))))))

(defn post! [url data options cb] )

(defn put! [url data options cb] )

(defn fetch!
  ([url cb] (fetch! :get url {} cb))
  ([url options cb] (fetch! :get url options cb))
  ([method url options cb]
   (case method
     :get (get! url options cb)
     :post (post! url (:data options) options cb)
     :put (put! url (:data options) options cb)
     :delete (delete! url options cb)
     (do (println "Unsupported method:" method)))))
