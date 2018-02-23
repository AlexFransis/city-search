(ns coveo.handler
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [coveo.middleware :refer [wrap-internal-error]]
            [coveo.services :refer [app-routes]]))

;; Creates a ring handler by combining our service route
;; handlers and route not found handler
(defn app
  "Takes an initial state which is the data loaded from files."
  [state]
  (compojure/routes
   (compojure/wrap-routes (app-routes state) wrap-internal-error)
   (route/not-found "Oh noo")))
