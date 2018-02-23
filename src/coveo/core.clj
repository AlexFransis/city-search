(ns coveo.core
  (:require [clojure.edn :as edn]
            [clojure.tools.nrepl.server :as nrepl]
            [coveo.handler :refer [app]]
            [coveo.loader :refer :all]
            [org.httpkit.server :refer :all])
  (:gen-class))

(defn load-config
  [file-name]
  (edn/read-string (slurp file-name)))

(defn load-data
  [config]
  (let [{:keys [city-file metadata-file]} config
        {:keys [city-rdr metadata-rdr]} (load-files city-file metadata-file)
        city-data (parse-city-file city-rdr)
        city-metadata (parse-metadata-file metadata-rdr)]
    (partition-data (merge-data city-data city-metadata))))

(defn start-repl
  [& {:keys [port handler]
      :or {port 7000}}]
  (try
    (println (str "starting nREPL server on port: " port))
    (nrepl/start-server :port port :handler handler)
    (catch Exception e
      (println "failed to start nRepl")
      (throw e))))

(defn start-http
  "Takes a ring handler to start the HTTP server.
  A handler is simply an http response to a request."
  [handler & {:keys [port]
              :or {port 3000}}]
  (try
    (println (str "starting HTTP server on port: " port))
    (run-server handler {:port port})
    (catch Exception e
      (println "failed to start http server")
      (throw e))))

(defn -main
  [& args]
  (let [config (load-config "config.edn")
        state (load-data config)
        http-port (Integer/valueOf (or (System/getenv "PORT") "3000"))
        nrepl-port (Integer/valueOf (or (System/getenv "NREPL_PORT") "7000"))]
    (start-repl :port nrepl-port)
    (start-http (app state) :port http-port)))
