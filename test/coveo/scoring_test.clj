(ns coveo.scoring-test
  (:require [coveo.services.scoring :refer :all]
            [clojure.test :refer :all]))

(def perfect-match {:q "montreal"
                   :long 40.000
                   :lat  40.000})

(def match {:q "mont"
            :lat 39.999
            :long 30.000})

(def city {:ascii "montreal"
           :latitude  40.000
           :longitude 40.000})

(def valid-geo-distance {:montreal [45.5016889 -73.56725599999999]
                         :toronto  [43.653226 -79.38318429999998]
                         :distance 504.24})


(deftest scoring-test

  (testing "get-city-name-score returns expected result based on valid inputs"
    (let [perfect-match-name  (:q perfect-match)
          match-name          (:q match)
          city-name           (:ascii city)]

      (is (= 1.0 (get-city-name-score :str1 perfect-match-name :str2 city-name)))
      (is (= 0.5 (get-city-name-score :str1 match-name :str2 city-name)))
      (is (= 1.0 (get-city-name-score :str1 "" :str2 "")))
      (is (= 1.0 (get-city-name-score)))
      (is (zero? (get-city-name-score :str1 "" :str2 city-name)))
      (is (zero? (get-city-name-score :str1 "laertnom" :str2 city-name)))))

  (testing "get-city-name-score returns expected result based on invalid inputs"
    (let [match-name (:q match)]

      (is (nil? (get-city-name-score :str1 nil :str2 nil)))
      (is (nil? (get-city-name-score :str1 match-name :str2 nil)))
      (is (nil? (get-city-name-score :str1 nil :str2 match-name)))
      (is (nil? (get-city-name-score :str1 0 :str2 0)))
      (is (nil? (get-city-name-score :str1 \a :str2 \a)))
      (is (nil? (get-city-name-score :str1 [] :str2 '())))))

  (testing "calculate-distance returns expected result based on valid inputs"
    (let [{:keys [montreal toronto distance]} valid-geo-distance]
      (is (zero? (calculate-distance [0 0] [0 0])))
      (is (pos? (calculate-distance [35 36] [40 80])))
      (is (double? (calculate-distance [0 0] [0 0])))
      (is (= distance (calculate-distance montreal toronto)))))

  (testing "calculate-distance returns expected result based on invalid inputs"
    (is (nil? (calculate-distance nil nil)))
    (is (nil? (calculate-distance [nil nil] [nil nil])))
    (is (nil? (calculate-distance [nil 0] [nil 1]))))

  (testing "get-distance-score returns expected resuld based on valid inputs"
    (let [geo-match         [(:lat match) (:long match)]
          geo-perfect-match [(:lat perfect-match) (:long perfect-match)]
          city-geo          [(:latitude city) (:longitude city)]]

      (is (= 1.0 (get-distance-score geo-perfect-match city-geo))))))
