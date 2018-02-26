# City Search API Documentation

* __URL__
  * `/suggestions`

* __Method__
  * `GET`

* __Query Parameters__

  **Required:**
   * `q`

  **Optional:**
  * `latitude`
  * `longitude`

* __Success Response__
  * __Code:__ 200
  * __Content:__

  ```json
  {
  result: "ok",
  suggestions: [{
  name: string,
  latitude: double,
  longitude: double,
  population: integer,
  score: double}]}
  ```
* __Error Response__
  * __Code:__ 500
  * __Content:__

  ```json
  {
  result: "error",
  message: "string"
  }
  ```

OR

  * __Code:__ 400
  * __Content:__

  ```json
  {
  result: "error",
  message: "string"
  }
  ```

* __Example call__
  * `/suggestions?q=montre&latitude=45.5088&longitude=-73.587`

  ```json
  {
  "result": "ok",
  "suggestions": [
  {
  "name": "montréal, quebec, ca",
  "latitude": 45.50883865356445,
  "longitude": -73.58780670166016,
  "population": 3268513,
  "score": 0.875
  },
  {
  "name": "montréal-ouest, quebec, ca",
  "latitude": 45.45286178588867,
  "longitude": -73.64917755126953,
  "population": 5184,
  "score": 0.7137
  }
  ]
  }
  ```
