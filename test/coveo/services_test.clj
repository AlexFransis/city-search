(ns coveo.services-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [coveo.core :refer [load-config load-data]]
            [coveo.handler :refer [app]]
            [coveo.services.payload :refer :all]
            [ring.mock.request :as mock]
            [schema.core :as s]))

(defn parse-body
  "Helper function to parse the JSON encoded string
  to a proper Clojure object i.e. a map with keywords"
  [body]
  (cheshire/parse-string-strict (slurp body) true))

(def test-db (-> "config.edn"
                 load-config
                 load-data))

(deftest service-routes-test

  (testing "Test GET request to /suggestions with query paramaeters returns expected response"
    (let [query-params {:q "montreal" :latitude 0.0 :longitude 0.0}
          request      (mock/request :get "/suggestions" query-params)
          response     ((app test-db) request)]
      (is (= 200 (:status response)))
      (is (s/validate SuggestionList (parse-body (:body response))))))

  (testing "Test GET request to /suggestions without query parameters returns expected response"
    (let [request  (mock/request :get "/suggestions")
          response ((app test-db) request)]
      (is (= 500 (:status response)))
      (is (s/validate ErrorResponse (parse-body (:body response))))))

  (testing "Test GET request to a wrong path returns expected response"
    (let [request  (mock/request :get "/wrong-path")
          response ((app test-db) request)]
      (is (= 404 (:status response)))))

  (testing "Test server errors returns expected response"
    (let [empty-state ""
          query-params {:q "montreal"}
          request (mock/request :get "/suggestions")
          response ((app empty-state) request)]
      (is (= 500 (:status response)))
      (is (s/validate ErrorResponse (parse-body (:body response)))))))
