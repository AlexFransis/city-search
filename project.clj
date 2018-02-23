(defproject coveo "0.1.0-SNAPSHOT"
  :description "Coveo Backend Project"
  :dependencies [[ring "1.6.3"]
                 [http-kit "2.2.0"]
                 [org.clojure/clojure "1.9.0"]
                 [metosin/compojure-api "1.1.11"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [ring/ring-mock "0.3.2"]
                 [cheshire "5.8.0"]
                 [cider/cider-nrepl "0.17.0-SNAPSHOT"]
                 [clj-fuzzy "0.4.1"]]
  :main coveo.core)
