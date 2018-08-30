
Skir - an over-simplified HTTP Node.js server toolkit
----

> in ClojureScript.

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/mvc-works/skir.svg)](https://clojars.org/mvc-works/skir)

```edn
[mvc-works/skir "0.0.2"]
```

WIP...

```clojure
(require '[skir.core :as skir])

(defn on-request! [req]
  {:code 200
   :headers {:Content-Type "application/edn"}
   :body {:message "Hello World!"}})

(skir/create-server! #(on-request! %))
```

### License

MIT
