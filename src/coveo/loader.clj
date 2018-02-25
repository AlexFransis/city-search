(ns coveo.loader
  "Loads and formats the data for retrieval via REST api."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn parse-city-file
  "Takes a java.io.BufferedReader on the file path and returns a list
  containing maps with keys corresponding to column names and their values."
  [rdr]
  (with-open [reader rdr]
    (line-seq reader) ;; skip headers
    ;; TODO: create keys dynamically without hardcoding indexes to avoid IndexOutOfBound Exception
    (into '() (for [row (line-seq reader)]
                (let [fields (-> row
                                 str/lower-case
                                 (str/split #"\t"))]
                  {:id          (fields 0)
                   :name        (fields 1)
                   :ascii       (fields 2)
                   :alt-name    (fields 3)
                   :latitude    (Float/valueOf (fields 4))
                   :longitude   (Float/valueOf (fields 5))
                   :feat-class  (fields 6)
                   :feat-code   (fields 7)
                   :country     (fields 8)
                   :cc2         (fields 9)
                   :admin1      (fields 10)
                   :admin2      (fields 11)
                   :admin3      (fields 12)
                   :admin4      (fields 13)
                   :population  (Integer/valueOf (fields 14))
                   :elevation   (fields 15)
                   :dem         (fields 16)
                   :tz          (fields 17)
                   :modified-at (fields 18)})))))

(defn parse-metadata-file
  "Takes a java.io.BufferedReader on the file path and returns a map
  with key and value corresponding to country code and province/state
  as strings from Canada and USA only."
  [rdr]
  (with-open [reader rdr]
    (reduce (fn [m fields]
              (assoc m (keyword (fields 0)) (fields 1)))
            {}
            (for [row (line-seq reader)
                  :when (or (str/starts-with? row "CA")
                            (str/starts-with? row "US"))]
              (-> row
                  str/lower-case
                  (str/split #"\t"))))))

(defn- add-location
  "Takes a map containing metadata about cities and returns
  a function that updates the `:admin1` key in the city record to match
  the province or state from the metadata file."
  [metadata]
  (fn [{:keys [country admin1] :as city}]
    (update city :admin1 (fn [admin1]
                           (get metadata (keyword (str country "." admin1)))))))

(defn partition-data
  "Partitions the data alphabetically creating a map of keys corresponding to letters
  of the alphabet and values corresponding to a collection of records of cities that
  start with the letter of the key.

  Example: {:m ({:name 'montreal'})
            :t ({:name 'toronto'})
            :v ({:name 'vancouver'})}
  "
  [city-data]
  (try
    (reduce (fn [m {:keys [name] :as city}]
              (update m (keyword (str (first name))) conj city))
            {}
            city-data)
    (catch Exception e
      (println (str "Error paritioning data: " (.getMessage e))))))

(defn merge-data
  "Add metadata to cities."
  [city-data city-metadata]
  (map (add-location city-metadata) city-data))

(defn load-files
  "Opens a java.io.BufferedReader on the files provided and returns them in a map. "
  [city-file metadata-file]
  (let [city     (io/reader city-file)
        metadata (io/reader metadata-file)]
    {:city-rdr city :metadata-rdr metadata}))
