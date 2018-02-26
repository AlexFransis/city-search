(ns coveo.services.scoring
  (:require [clj-fuzzy.metrics :as metrics]
            [coveo.utils.helper :as help]))

(defn adjust-score-for-population
  "Adjust the scores of suggestions.
  A better score will be given to the city with the largest population in the
  pool of suggestions.

  Will adjust the score only if geo points were not given in the original query.

  Takes:

  - `query-params` map containing `lat` `long`
  - `suggestions` coll of suggestion

  Returns coll of suggestions with new scores
  "
  [{:keys [lat long] :as query-params} suggestions]
  (if (some nil? [lat long])
    (let [score-weight      0.4
          population-weight 0.6
          total-population  (reduce (fn [total city]
                                      (+ total (:population city))) 0 suggestions)]
      (map (fn [suggestion]
             (let [population (:population suggestion)]
               (update suggestion :score
                       (fn [score]
                         (help/round-with-precision
                          4
                          (+ (* score-weight score)
                             (* population-weight (/ population
                                                     total-population))))))))
           suggestions))
    suggestions))

(defn get-distance-score
  "Returns a score between 0 and 1 based on the distance between two points.
  It will compute the distance and compare it to the longest distance
  between two points on earth. It will apply a negative exponential slope to
  calculate the score

  Takes:
  - `geo1` a vector containing latitude and longitude as `Double`
  - `geo2` a vector containing latitude and longitude as `Double`

  Returns:
  - `Double` score value from 0 to 1.0
  - `nil`    when any of the values passed is nil

  "
  [[lat1 long1 :as geo1] [lat2 long2 :as geo2]]
  (when (not-any? nil? [lat1 long1 geo1])
    (let [distance          (help/calculate-distance geo1 geo2)
          slope             1.50
          longest-distance  20000.0
          adjusted-distance (Math/pow distance slope)]
      (if (>= adjusted-distance longest-distance)
        0
        (- 1.0 (/ adjusted-distance longest-distance))))))

(defn get-city-name-score
  "Returns a score based on string similarity using Levenshtein's distance algorithm.
  Compute the number of single character edits required to change `str1` to `str2`
  then return a score from 0 to 1 being the most confident of a match by dividing
  the number of edits required by the upper bound which is at most the length of the
  longest string of the two.

  Takes:
  - `str1` key with a `String` value
  - `str2` key with a `String` value

  Returns:
  - `Double` score value from 0.0 to 1.0
  - `nil` if arguments are not strings
  "
  [& {:keys [str1 str2]
      :or {str1 "" str2 ""}}]
  (if-not (and (string? str1) (string? str2))
    nil
    (let [longest-string (max (count str1) (count str2))]
      (if (zero? longest-string)
        1.0 ;; perfect score if both strings are empty
        (- 1.0 (/ (metrics/levenshtein str1 str2)
                  longest-string))))))

(defn- contains-geo?
  "Returns true if both geo coordinate value were provided in the query parameters"
  [{:keys [lat long] :as query-params} _]
  (not-any? nil? [lat long]))

(defn name-only-algo
  "Algorithm to compute score when there is no geo coordinates provided"
  [{:keys [q] :as query-params}
   {:keys [ascii] :as match}]
  (help/round-with-precision 4 (get-city-name-score :str1 q :str2 ascii)))

(defn name-and-geo-algo
  "Algorithm to compute score when geo coordinates are provided."
  [{:keys [q lat long] :as query-params}
   {:keys [ascii latitude longitude] :as match}]
  (let [weight         0.5
        name-score     (get-city-name-score :str1 q :str2 ascii)
        distance-score (get-distance-score [lat long] [latitude longitude])]
    (help/round-with-precision 4 (* (+ name-score distance-score) weight))))

(defmulti get-score
  "Returns a score between 0 and 1.0 to determine the confidence of
  the match with 1 being the most confident.

  If no values for the keys `lat` and `long` are provided, the weight
  of the score will be solely dependent on string comparison of the
  query parameter `q` and the name of the match `name`. The algorithm
  used for string comparison will be Levenshtein's distance algorithm.

  If values for the keys `lat` and `long` are provided, half of the weight
  will be given to the string comparison of `q` and `ascii` and the other
  half will be given based on the distance between the coordinates given in the
  query parameters and the geo points of the match.

  Takes:
  - `query-params` map containing the keys `q`, `long`, `lat`
  - `match`        map containing the keys `ascii`, `longitude`, `latitude`

  Returns:
  - `Double` score value from 0 to 1.0
  "
  contains-geo?)

(defmethod get-score true [query-params match] (name-and-geo-algo query-params match))
(defmethod get-score false [query-params match] (name-only-algo query-params match))
