## Running Wiremock as a Webapp

From the `ext-mock-webapp` module, run the following command to start up a standalone wiremock server:

`mvn clean tomcat8:run-war`

## Running Wiremock from Tests (JUnit, Cucumber, etc)

For an example of how to setup wiremock to run with a standalone unit test case, see the following test in the `ext-mock-examples` module:

`test/java/com.elasticpath.wiremock.examples.WireMockExamplesServicesTest`

This JUnit test executes tests against the example services using an in-memory WireMock server.

For an example of how to write cucumber integration tests that can be executed remotely, see the cucumber tests in the `system-tests/cucumber` 
module.

## Adding New Mock Services

There are several different implementation patterns for new mock services.  This section details some of them.  There are several examples 
provided in the `ext-mock-examples` module.  New mock services should be created by copying the most relevant example from this module into the 
`ext-mock-services` module and modifying it as necessary.

All new services require a new service mapping file be created.  Mappings for new mock services must be placed at this location:

`ext-mock-services/src/main/resources/wiremock/mappings/`

in a directory matching the service name.  

### Static Mappings

The quickest and easiest way to configure a mock service is to provide static response files. For an example of how to configure
a static mapping see the `ext-mock-examples/src/main/resources/wiremock/mappings/static` directory.

#### Static Responses

Static response files for services should be placed in :

`ext-mock-services/src/main/resources/wiremock/__files`

in a directory matching the service name.  See `ext-mock-examples/src/main/resouces/wiremock/__files/static` for an example of this type of mapping.

These response files can be dynamically selected based upon request criteria using templating, if needed.  See the 
`ext-mock-examples/src/main/resources/wiremock/mappings/staticByPath` directory for an example that serves different static responses based upon 
elements in the request URL.  Values from the request can also be dynamically injected into the response body (see 
`ext-mock-examples/src/main/resources/wiremock/__files/staticByPath/response_body_3.json`)

### Dynamic Transformer Mappings

Additional dynamic mappings require the use of a transformer, see http://wiremock.org/docs/extending-wiremock/.  The transformer provides a hook
to inject any logic to create the response.

To create and configure a new dynamic mapping :

1. Create a class that extends one of the following in a new package in `ext-mock-services/src/main/java`:
	* com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
	* com.github.tomakehurst.wiremock.extension.ResponseTransformer
1. Add a README.md file to the package that details how your new mock service works and what it does.
1. Add the fully qualified name of the class to the `transformerClasses` servlet context param in `ext-mock-webapp/src/main/webapp/WEB-INF/web
.xml`.  This field contains a comma-delimited list of all configured transformer extensions.
1. Create a service mapping for the new service in `ext-mock-services/src/main/resources/wiremock/mappings/`.  The mapping
must contain a reference to your new transformer class.  See the `simple`,  `jackson` and `soap` directories for examples of this type of 
implementation in the `ext-mock-examples/src/main/resources/wiremock/mappings` directory.
1. For an example of how to create test cases to test your new mock service, see `com.elasticpath.wiremock.examples.WireMockExamplesServicesTest` 
in the `ext-mock-examples` module.

## Configure Mock URL

For integration testing against WireMock, you will need to update your service outbound endpoints to match your service mappings.

By default, Wiremock runs on port 8080.  When using the ep-developer profile, the default port is 8086.  This behavior can be changed by
modifying the ep.mock.tomcat.port.http property in the commerce-parent pom.xml.  

The example URL below is for the healthcheck example service, using the default port.

e.g. `http://localhost:8080/mock/healthcheck`

Note that the context root, `mock`, is configured in the `ep.mock.context` Maven variable configured in `ep-commerce/pom.xml`.

## Example Mock Services

There are several examples provided in the `ext-mock-examples` module to serve as templates for new mock services.  Note that there are unit and 
integration tests that execute against these sample services which may fail if they are modified.  Any changes should use a "copy and customize" 
pattern by copying the example you wish to customize to `ext-mock-services`.

* /static - this service always returns an HTTP 200 response with a static (pre-built) json response from a file in `__files/static`
* /staticByPath - this service demonstrates how to use request templating to serve different static responses
based on the values in the request.  The path, body, cookies, headers, etc in the request can be interrogated to serve
different static responses. See http://wiremock.org/docs/response-templating/
* /healthcheck - this service always returns an HTTP 200 response with no body
* /simple - uses a custom transformer to take a value from the request JSON and inject it into the response JSON
* /jackson - uses a custom transform which utilizes Jackson to deserialize a JSON request into POJOs.  The request POJOs are then used to create 
a set of response POJOs which are then serialized back into a response JSON with Jackson 
* /soap - uses a custom tranformer which parses a soap request and generates a soap response using JAXB

