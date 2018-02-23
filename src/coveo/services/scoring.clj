(ns coveo.services.scoring
  (:require [clj-fuzzy.metrics :as metrics]))

(defn- deg->rad
  [deg]
  (/ (* Math/PI deg) 180))

(defn- rad->deg
  [rad]
  (* rad (/ 180.0 Math/PI)))


(defn- calculate-distance
  "Calculates the distance in KM between two GPS coordinates.
  Uses Haversine formula. "
  [[lat1 long1 :as geo1] [lat2 long2 :as geo2]]
  (let [theta (- long1 long2)
        dist (+ (* (Math/sin (deg->rad lat1))
                   (Math/sin (deg->rad lat2)))
                (* (Math/cos (deg->rad lat1))
                   (Math/cos (deg->rad lat2))
                   (Math/cos (deg->rad theta))))]
    (->> dist
         Math/acos
         rad->deg
         (* 60 1.1515)
         ;; Convert miles to kms
         (* 1.609344))))

(defn get-distance-score
  "Returns a score between 0 and 1 based on the distance between two points.
  It will compute the distance and compare it to the longest distance
  between two points on earth. It will apply a negative exponential slope to
  calculate the score

  Will not compute the score if `lat1` and `long1` are nil.
  Will add a default value of 0 in case either is missing but not both.

  Takes:
  - `:lat1`  key with a `Double` value
  - `:long1` key with a `Double` value
  - `:lat2`  key with a `Double` value
  - `:long2` key with a `Double` value

  Returns:
  - `Double` score value from 0 to 1.0

  "
  [& {:keys [lat1 long1 lat2 long2]
      :or {lat1 0 long1 0}}]
  (if (and (nil? lat1) (nil? long1))
    0
    (let [distance          (calculate-distance [lat1 long1] [lat2 long2])
          slope             1.50
          longest-distance  20000.0
          adjusted-distance (Math/pow distance slope)]
      (if (>= adjusted-distance longest-distance)
        0
        (- 1 (/ adjusted-distance
                longest-distance))))))

(defn get-city-name-score
  "Returns a score based on string similarity using Levenshtein's distance algorithm.
  Compute the number of single character edits required to change `city1` to `city2`
  then return a score from 0 to 1 being the most confident of a match by dividing
  the number of edits required by the upper bound which is at most the length of the
  longest string of the two.

  Takes:
  - `city1` key with a `String` value
  - `city2` key with a `String` value

  Defaults to an empty string if value is not provided.

  Returns:
  - `Double` score value from 0 to 1.0
  "
  [& {:keys [city1 city2]
      :or {city1 "" city2 ""}}]
  (let [longest-string (max (count city1) (count city2))]
    (- 1.0 (/ (metrics/levenshtein city1 city2)
              longest-string))))

(defn get-score
  "Returns a score between 0 and 1.0 to determine the confidence of
  the match with 1 being the most confident.

  If no values for the keys `lat` and `long` are provided, the weight
  of the score will be solely dependent on string comparison of the
  query parameter `q` and the name of the match `name`. The algorithm
  used for string comparison will be Jaro-Walker's algorithm.

  If values for the keys `lat` and `long` are provided, half of the weight
  will be given to the string comparison of `q` and `name` and the other
  half will be given based on the distance between the coordinates [`lat` `long`]
  and [`latitude` `longitude`].

  Takes:
  - `query-params` map containing the keys `q`, `long`, `lat`
  - `match`        map containing the keys `name`, `longitude`, `latitude`

  Returns:
  - `Double` score value from 0 to 1.0
  "
  [{:keys [q long lat] :as query-params}
   {:keys [ascii longitude latitude] :as match}]
  (let [weight (if (and (nil? long) (nil? lat)) 1.0 0.5)
        city-score (get-city-name-score :city1 q :city2 ascii)
        distance-score (get-distance-score :lat1 lat :long1 long
                                           :lat2 latitude :long2 longitude)]
    (Double. (format "%.4f" (+ (* weight city-score) (* weight distance-score))))))
