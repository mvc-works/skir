
(ns skir.core (:require ["http" :as http]))

(def default-options
  {:port 4000,
   :after-start (fn [options] (println (str "Server listening on " (:port options)))),
   :host "0.0.0.0"})

(defn req->edn [req]
  (.log js/console "URL:" (.-url req))
  {:method :get, :headers (js->clj (.-headers req) :keywordize-keys true), :body nil})

(defn write-response! [res edn-res]
  (println "write response" (pr-str edn-res))
  (set! (.-statusCode res) (:status edn-res))
  (doseq [[k v] (:headers edn-res)] (.setHeader (name k) v))
  (.end
   res
   (let [body (:body edn-res)]
     (cond
       (coll? body) (pr-str body)
       (nil? body) ""
       (string? body) body
       (.isArray js/Array body) (.stringify js/JSON body)
       :else (.stringify js/JSON body)))))

(defn create-server!
  ([handler] (create-server! handler nil))
  ([handler user-options]
   (let [options (merge default-options user-options)]
     (let [server (http/createServer
                   (fn [req res]
                     (let [edn-req (req->edn req), edn-res (handler edn-req)]
                       (write-response! res edn-res))))]
       (.listen server (:port options) (:host options) (:after-start options))))))
