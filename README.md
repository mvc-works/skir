
Skir - an over-simplified HTTP Node.js server toolkit
----

> in ClojureScript.

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/mvc-works/skir.svg)](https://clojars.org/mvc-works/skir)

```edn
[mvc-works/skir "0.0.8-a1"]
```

WIP...

```clojure
(require '[skir.core :as skir])

(defn on-request! [req-edn res]
  {:code 200
   :message "OK"
   :headers {:Content-Type "application/edn"}
   :body {:message "Hello World!"}})

(skir/create-server! #(on-request! %1 %2))
```

Core logic:

```clojure
(cond
  (map? response) (write-response! res response)
  (fn? response) (response (fn [response-data] (write-response! res response-data)))
  (promise? response) (.then response (fn [result] (write-response! res result)))
  (chan? response) (go (write-response! res (<! response)) (close! response))
  (= response :effect) (comment "Done with effect")
  :else (do (println "Response:" response) (throw (js/Error. "Unrecognized response!"))))
```

### License

MIT
