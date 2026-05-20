(defproject io.github.possum/eigentrust "0.1.1-SNAPSHOT"
  :description "An Eigentrust library for Clojure"
  :url "https://github.com/Possum/eigentrust"
  :license {:name "MIT License"
            :url "https://opensource.org"}
  :dependencies [[org.clojure/clojure "1.12.2" :scope "provided"]]
  :repl-options {:init-ns possum.eigentrust}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--sign"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
