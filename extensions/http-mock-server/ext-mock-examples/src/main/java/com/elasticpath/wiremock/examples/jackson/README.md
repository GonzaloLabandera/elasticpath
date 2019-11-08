# DynamicJacksonResponseDefinitionTransformer

Package: `com.elasticpath.wiremock.examples.jackson`

This is an example ResponseDefinitionTransformer that takes a JSON request and uses the 
Jackson library to deserialize the request into a set of DTO/POJO objects.  The request
is then used to build and populate a set of response DTO/POJO objects.  Jackson is also
used to serialize the response objects into JSON.

The DTO/POJO objects are in the `com.elasticpath.wiremock.examples.jackson.dto` package.

The status of the response will always be "Complete".  The number of response lines will be
identical to the number of request lines.

HTTP Verb: `POST`

Content-Type: `application/json`

Response Code: `200` if successful, `400` if the request cannot be deserialized


## Sample Request

	{
		"status": "P",
		"code": "ABC",
		"submitDate": "2019-03-28",
		"requestLines": [
			{
				"id" : "a1",
				"optionA": 1,
				"isValid": true
			},
			{
				"id" : "a2",
				"optionA": 2,
				"isValid": false
			}
	
		]
	
	}

## Sample Response

	{
	  "status": "Complete",
	  "createDate": "2019-04-01T15:12:19.439+0000",
	  "responseLines": [
		{
		  "id": "a1",
		  "doubleVal": 1
		},
		{
		  "id": "a2",
		  "doubleVal": 1
		}
	  ]
	}

