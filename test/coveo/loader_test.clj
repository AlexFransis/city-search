(ns coveo.loader-test
  (:require [coveo.loader :refer :all]
            [coveo.core :as core]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

(def test-city-file "resources/testfile1.txt")
(def test-metadata-file "resources/testfile2.txt")
(def config-file-path (core/load-config "config.edn"))

(defn create-files-fixture
  [test]
  (let [{:keys [city-file metadata-file]} config-file-path]
    (io/copy (io/reader city-file) (io/file test-city-file))
    (io/copy (io/reader metadata-file) (io/file test-metadata-file)))
  (test)
  (io/delete-file test-city-file)
  (io/delete-file test-metadata-file))

(use-fixtures :once create-files-fixture)

(deftest loader-test

  (testing "Passing valid file paths to load-files returns expected results"
    (let [{:keys [city-rdr metadata-rdr]} (load-files test-city-file test-metadata-file)]
      (is (instance? java.io.BufferedReader city-rdr))
      (is (instance? java.io.BufferedReader metadata-rdr))))

  (testing "Passing invalid file paths to load-files returns expected results"
    (let [invalid-file1 "invalid/file1.txt"
          invalid-file2 "invalid/file2.txt"]
      (is (thrown? java.io.FileNotFoundException (load-files invalid-file1 invalid-file2)))))

  (testing "Passing BufferedReader to parse-city-file returns expected results"
    (let [{:keys [city-rdr metadata-rdr]} (load-files test-city-file test-metadata-file)]
      (is seq? (parse-city-file city-rdr))
      (is seq? (parse-metadata-file metadata-rdr)))))
