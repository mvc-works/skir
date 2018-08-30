
(ns skir.client (:require ["http" :as http] [cljs.reader :refer [read-string]]))

(defn collect-response-data! [res cb!]
  (let [*raw-data (atom "")]
    (.setEncoding res "utf8")
    (.on res "data" (fn [chunk] (swap! *raw-data str chunk)))
    (.on res "end" (fn [] (cb! @*raw-data)))))

(defn delete! [url options cb] )

(defn get! [url options cb]
  (.get
   http
   url
   (fn [res]
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
              text)})))))))

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
