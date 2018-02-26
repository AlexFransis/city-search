# City Search API Documentation

* __ URL __
  * `/suggestions`

* __ Method __
  * `GET`

* __Query Parameters__

  **Required:**
   * `q`

  **Optional:**
  * `latitude`
  * `longitude`

* __ Success Response __
  * __Code:__ 200
  * __Content:__
                  {
                    result: "ok",
                    suggestions: [
                      {
                        name: string,
                        latitude: double,
                        longitude: double,
                        population: integer,
                        score: double
                      }
                    ]
                  }

* __ Error Response __
  * __Code:__ 500
  * __Content:__
                  {
                    result: "error",
                    message: "string"
                  }
  OR

  * __Code:__ 400
  * __Content:__
                  {
                    result: "error",
                    message: "string"
                  }

* __ Example call __
  * `/suggestions?q=montre&latitude=45.5088&longitude=-73.587`'
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
