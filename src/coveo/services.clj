(ns coveo.services
  (:require [compojure.api.sweet :refer :all]
            [coveo.services.payload :as payload]
            [coveo.services.suggestions :refer [get-suggestions]]
            [ring.util.http-status :as status]))

(defn app-routes
  "Takes a state containing the application data."
  [state]
  (defapi service-routes
    {:swagger {:ui "/"
               :spec "/swagger.json"
               :data {:info {:version "1.0.0"
                             :title "Coveo Backend Project"
                             :description "Public API"
                             :tags [{:name "suggestions"}]}}}}

    (GET "/suggestions" []
         :tags ["suggestions"]
         :query-params [q :- (describe String "city name")
                        {longitude :- Double nil}
                        {latitude :- Double nil}]
         :return payload/SuggestionList
         :responses {status/bad-request {:description "Missing required parameter"}
                     status/internal-server-error {:description "Server error"
                                                   :schema payload/ErrorResponse}}
         :summary "Returns suggestions matching the request parameters"
         (get-suggestions {:q q :long longitude :lat latitude} state))))
