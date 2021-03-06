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
  (println "### Loading data ###")
  (let [{:keys [city-file metadata-file]} config
        {:keys [city-rdr metadata-rdr]} (load-files city-file metadata-file)
        city-data (parse-city-file city-rdr)
        city-metadata (parse-metadata-file metadata-rdr)]
    (->> (merge-data city-data city-metadata)
         partition-data)))

(defn start-repl
  [& {:keys [port handler]
      :or {port 7000}}]
  (try
    (println (str "### Starting nREPL server on port: " port " ###"))
    (nrepl/start-server :port port :handler handler)
    (catch Exception e
      (println (str "### Failed to start nREPL ###\n"
                    (.getMessage e))))))

(defn start-http
  "Takes a ring handler to start the HTTP server."
  [handler & {:keys [port]
              :or {port 3000}}]
  (try
    (println (str "### Starting HTTP server on port: " port " ###"))
    (run-server handler {:port port})
    (catch Exception e
      (println "### Failed to start http server ###")
      (throw e))))

(defn -main
  [& args]
  (let [config (load-config "config.edn")
        state (load-data config)
        http-port (Integer/valueOf (or (System/getenv "PORT") "3000"))
        nrepl-port (Integer/valueOf (or (System/getenv "NREPL_PORT") "7000"))]
    (start-repl :port nrepl-port)
    (start-http (app state) :port http-port)))
