
Skir - an over-simplified HTTP Node.js server toolkit
----

> ...built for and with ClojureScript.

### Usage

WIP...

```clojure
(require '[skir.core :as skir])

(defn on-request! [req]
  {:status 200
   :headers {:Content-Type "application/edn"}
   :body {:message "Hello World!"}})

(skir/create-server! (#on-request!))
```

### License

MIT
