(ns coveo.services.payload
  "Namespace to define contract for payload objects"
  (:require [schema.core :as s]))

(s/defschema Suggestion
  {:name       String
   :latitude   Double
   :longitude  Double
   :population Integer
   :score      Double})

(s/defschema SuggestionList
  {:result      String
   :suggestions [Suggestion]})

(s/defschema ErrorResponse
  {:result   String
   :message  String})
