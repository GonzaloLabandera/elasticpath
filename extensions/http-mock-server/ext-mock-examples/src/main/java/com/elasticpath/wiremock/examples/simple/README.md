# DynamicSimpleResponseDefinitionTransformer

Package: `com.elasticpath.wiremock.examples.simple`

This is an example ResponseDefinitionTransformer that takes a JSON request and uses the 
Jackson library to parse the request.  

The value in the request for the service element is populated into to the matching field in the response.  

HTTP Verb: `POST`

Content-Type: `application/json`

Response Code: `200` if successful, `400` if the request cannot be parsed


## Sample Request

	{ 
		"service": "dynamicInputValue"
	}

## Sample Response

	{ 
		"service": "dynamicInputValue"
	}


