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


(deftest scoring-test

  (testing "get-city-name-score returns expected result based on strings passed"
    (let [perfect-match-name  (:q perfect-match)
          match-name          (:q match)
          city-name           (:ascii city)
          match-score         (get-city-name-score :str1 match-name :str2 city-name)
          perfect-match-score (get-city-name-score :str1 perfect-match-name :str2 city-name)]

      (is (= 1.0 perfect-match-score))
      (is (and (>= match-score 0) (<= match-score 1)))
      (is (= 1.0 (get-city-name-score :str1 "" :str2 "")))
      (is (= 1.0 (get-city-name-score)))
      (is (= 0.0 (get-city-name-score :str1 "" :str2 city-name)))
      (is (= 0.0 (get-city-name-score :str1 "laertnom" :str2 city-name)))
      (is (= nil (get-city-name-score :str1 nil :str2 nil)))
      (is (= nil (get-city-name-score :str1 match-name :str2 nil)))
      (is (= nil (get-city-name-score :str1 nil :str2 match-name)))
      (is (= nil (get-city-name-score :str1 0 :str2 0)))
      (is (= nil (get-city-name-score :str1 \a :str2 \a)))
      (is (= nil (get-city-name-score :str1 [] :str2 []))))))
