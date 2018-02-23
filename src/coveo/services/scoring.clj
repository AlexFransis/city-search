(ns coveo.services.scoring
  (:require [clj-fuzzy.metrics :as metrics]))

(defn- deg->rad
  [deg]
  (/ (* Math/PI deg) 180))

(defn- rad->deg
  [rad]
  (* rad (/ 180 Math/PI)))

(defn- round-with-precision
  "Rounds a `Double` to the given precision."
  [precision double]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* double factor))
       factor)))


(defn calculate-distance
  "Calculates the distance in KM between two GPS coordinates.
  Uses Haversine formula. "
  [[lat1 long1 :as geo1] [lat2 long2 :as geo2]]
  (when (not-any? nil? [lat1 lat2 long1 long2 geo1 geo2])
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
           (* 1.609344)
           ;; Keep only 2 decimal places when computing distance
           (round-with-precision 2)))))

(defn get-distance-score
  "Returns a score between 0 and 1 based on the distance between two points.
  It will compute the distance and compare it to the longest distance
  between two points on earth. It will apply a negative exponential slope to
  calculate the score

  Will not compute the score if `lat1` and `long1` are nil.
  Will add a default value of 0 in case either is missing but not both.

  Takes:
  - `geo1` a vector containing latitude and longitude as `Double`
  - `geo2` a vector containing latitude and longitude as `Double`

  Returns:
  - `Double` score value from 0 to 1.0

  "
  [[lat1 long1 :as geo1] [lat2 long2 :as geo2]]
  (if (not-any? nil? [lat1 long1 geo1])
    (let [distance          (calculate-distance geo1 geo2)
          slope             1.50
          longest-distance  20000.0
          adjusted-distance (Math/pow distance slope)]
      (if (>= adjusted-distance longest-distance)
        0
        (- 1.0 (/ adjusted-distance
                  longest-distance))))
    0))

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
  - `Double` score value from 0.0 to 1.0
  - `nil` if arguments are not strings
  "
  [& {:keys [str1 str2]
      :or {str1 "" str2 ""}}]
  (if-not (and (instance? String str1) (instance? String str2))
    nil
    (let [longest-string (max (count str1) (count str2))]
      (if (= 0 longest-string)
        1.0 ;; perfect score if both strings are empty
        (- 1.0 (/ (metrics/levenshtein str1 str2)
                  longest-string))))))

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
        city-score (get-city-name-score :str1 q :str2 ascii)
        distance-score (get-distance-score [lat long] [latitude longitude])]
    (round-with-precision 4 (+ (* weight city-score) (* weight distance-score)))))
