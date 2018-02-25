(ns coveo.utils.helper)

(defn deg->rad
  [deg]
  (/ (* Math/PI deg) 180))

(defn rad->deg
  [rad]
  (* rad (/ 180 Math/PI)))

(defn round-with-precision
  "Rounds a double to the given precision."
  [precision dbl]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* dbl factor))
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
           ;; Keep only 2 decimal places when returning distance
           (round-with-precision 2)))))
