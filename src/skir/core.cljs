
(ns skir.core
  (:require ["http" :as http]
            [skir.util :refer [key->str chan? promise?]]
            [cljs.core.async :refer [chan <! >! put! timeout close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def default-options
  {:port 4000,
   :after-start (fn [options] (println (str "Server listening on " (:port options)))),
   :host "0.0.0.0"})

(defn req->edn [req]
  {:method :get,
   :url (.-url req),
   :headers (js->clj (.-headers req) :keywordize-keys true),
   :body nil})

(defn write-response! [^js res edn-res]
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
       (.isArray js/Array body) (.stringify js/JSON body)
       :else (.stringify js/JSON body)))))

(defn handle-request! [req res handler]
  (let [edn-req (req->edn req), response (handler edn-req)]
    (cond
      (map? response) (write-response! res response)
      (fn? response) (response (fn [response-data] (write-response! res response-data)))
      (promise? response) (.then response (fn [result] (write-response! res result)))
      (chan? response) (go (write-response! res (<! response)) (close! response))
      :else (do (println "Response:" response) (throw (js/Error. "Unrecognized response!"))))))

(defn create-server!
  ([handler] (create-server! handler nil))
  ([handler user-options]
   (let [options (merge default-options user-options)
         server (http/createServer (fn [req res] (handle-request! req res handler)))]
     (.listen server (:port options) (:host options) (fn [] ((:after-start options) options))))))
