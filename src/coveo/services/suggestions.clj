(ns coveo.services.suggestions
  (:require [clojure.string :as str]
            [coveo.services.scoring :refer [get-score adjust-score-for-population]]
            [ring.util.http-response :as response]))

(defn create-payload-object
  "Creates a payload object to be returned.

  Takes:
  - `query-params` map containing keys `q`, `lat`, `long`

  Returns a function to be applied to a collection of city records via reduce.
  "
  [{:keys [q lat long] :as query-params}]
  (fn [coll {:keys [name latitude longitude score population admin1 country] :as record}]
    (conj coll {:name (str name ", " admin1 ", " country)
                :latitude latitude
                :longitude longitude
                :population population
                :score (get-score query-params record)})))

(defn get-suggestions
  "Returns a vector of city suggestions with a score in the body of an HTTP response.

  Takes:
  - `query-params` map containing keys `q`, `lat`, `long`
  - `cities` collection containing data on cities
  "
  [{:keys [q long lat] :as query-params} cities]
  (if-not (seq cities)
    (response/internal-server-error {:result "error"
                                     :message "Failed to load data"})
    (->> (get cities (keyword (str (first q))))
         (filter (fn [record] (str/starts-with? (:ascii record) q)))
         (reduce (create-payload-object query-params) [])
         (adjust-score-for-population query-params)
         (sort-by :score >)
         (assoc {:result "ok"} :suggestions)
         response/ok)))
