### H2O.ai MOJO Scorer

Deploy MOJO models as REST APIs easily using Javalin.io

###### Inspired by
* https://github.com/rubuntu/H2O_Mojo_Model_Scorer


##### TODOS
* Add users authentication
* Add JWT
* Support Clustering models
* Support predict over CSV files

#### Usage

* Clone this repo and build the executable `jar` using:
`mvn clean install`

* Copy the `h2o_app-0.0.1.jar` at `target/`

* Run the server using:
`java -jar h2o_app-0.0.1.jar`

##### REST API

| Method  | Endpoint | Description | JWT protected
| ------------- | ------------- | ------------- |------------- |
| POST  | /model/:model/predict | Make a single prediction from the specified model name | True
| PUT  | /upload/:model | Upload a MOJO and set name to the model | True
| GET  | /models/list | List all available models name | True
| POST  | /users/get_token | Returns a valid JWT Token | False

#### Important 
* The `data` must be send as `form-data`, specifying the variable names and values.
* `MOJOs` are send in a `form-data` too, the `key` must be called `model`and the value is the `MOJO` zip file.
* By default creates a username and password called `admin` and `pass`, those are stored in a `List`, you can change the code and add a database connection or whatever to get your users.

#### Response example:
```
{
    "prediction": "68.13734189777028",
    "params": {
        "carbo": "5",
        "fiber": "10",
        "sugars": "6",
        "weight": "1",
        ...
    }
}
```