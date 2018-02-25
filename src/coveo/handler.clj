(ns coveo.handler
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [coveo.middleware :refer [wrap-internal-error]]
            [coveo.services :refer [app-routes]]))

(defn app
  "Application handler that takes an initial state containing data to feed the application.
  Contains handlers for service routes and not-found route"
  [state]
  (compojure/routes
   (app-routes state)
   (route/not-found "Oh noo")))
