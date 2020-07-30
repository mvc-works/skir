
(ns skir.core
  (:require ["http" :as http]
            [skir.util :refer [key->str chan? promise?]]
            [cljs.core.async :refer [chan <! >! put! timeout close!]]
            [lilac.core
             :refer
             [dev-check record+ number+ string+ any+ keyword+ map+ optional+ or+]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def default-options
  {:port 4000,
   :after-start (fn [options] (println (str "Server listening on " (:port options)))),
   :host "0.0.0.0"})

(def lilac-res
  (record+
   {:code (number+),
    :message (optional+ (string+)),
    :headers (optional+ (map+ (or+ [(keyword+) (string+)]) (or+ [(keyword+) (string+)]))),
    :body (any+)}
   {:check-keys? true}))

(defn req->edn [req]
  {:method (case (.-method req)
     "GET" :get
     "HEAD" :head
     "POST" :post
     "PUT" :put
     "DELETE" :delete
     "CONNECT" :connect
     "OPTIONS" :options
     "TRACE" :trace
     "PATCH" :patch
     (.-method req)),
   :url (.-url req),
   :headers (js->clj (.-headers req) :keywordize-keys true),
   :body nil,
   :original-request req})

(defn write-response! [^js res edn-res]
  (dev-check edn-res lilac-res)
  (set! (.-statusCode res) (:code edn-res))
  (set! (.-statusMessage res) (:message edn-res))
  (doseq [[k v] (:headers edn-res)] (.setHeader res (key->str k) (key->str v)))
  (.end
   res
   (let [body (:body edn-res)]
     (cond
       (coll? body) (pr-str body)
       (nil? body) ""
       (string? body) body
       (js/Array.isArray body) (js/JSON.stringify body)
       :else (js/JSON.stringify body)))))

(defn handle-request! [req res handler]
  (let [edn-req (req->edn req), response (handler edn-req res)]
    (cond
      (map? response) (write-response! res response)
      (fn? response) (response (fn [response-data] (write-response! res response-data)))
      (promise? response) (.then response (fn [result] (write-response! res result)))
      (chan? response) (go (write-response! res (<! response)) (close! response))
      (= response :effect) (comment "Done with effect")
      :else (do (println "Response:" response) (throw (js/Error. "Unknown response!"))))))

(defn create-server!
  ([handler] (create-server! handler nil))
  ([handler user-options]
   (let [options (merge default-options user-options)
         server (http/createServer (fn [req res] (handle-request! req res handler)))]
     (.listen server (:port options) (:host options) (fn [] ((:after-start options) options))))))
