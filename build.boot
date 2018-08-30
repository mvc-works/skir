
(def config {:clojars-user "jiyinyiyong"
             :package 'mvc-works/skir
             :version "0.0.2"
             :github-url "https://github.com/mvc-works/skir"
             :description "An over-simplified Node.js HTTP server"})

(defn read-password [guide]
  (String/valueOf (.readPassword (System/console) guide nil)))

(set-env!
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/core.async "0.4.474"]]
  :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"
                                     :username (:clojars-user config)
                                     :password (read-password "Clojars password: ")}]))

(deftask deploy []
  (comp
    (pom :project     (:package config)
         :version     (:version config)
         :description (:description config)
         :url         (:github-url config)
         :scm         {:url (:github-url config)}
         :license     {"MIT" "http://opensource.org/licenses/mit-license.php"})
    (jar)
    (install)
    (push :repo "clojars" :gpg-sign false)))
