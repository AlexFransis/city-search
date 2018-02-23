(ns coveo.middleware
  (:require [ring.util.http-response :refer :all]))


(defn wrap-internal-error
  "Centralize internal server error responses as a middleware
  applied to all routes."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (internal-server-error {:result "error"
                                :message (.getMessage e)})))))
