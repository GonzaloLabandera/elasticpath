@wiremock @simple-example
Feature: Wiremock Simple Dynamic Example Service

  Scenario Outline: Example service returns a valid 200 response with dynamic body
    Given Wiremock service is up
    When I POST to the <MOCK_ENDPOINT> endpoint with body <REQUEST_JSON>
    Then I receive a <HTTP_STATUS_CODE> response with a body that contains <RESPONSE_CONTAINS>

    Examples:
    | MOCK_ENDPOINT | REQUEST_JSON                     | HTTP_STATUS_CODE | RESPONSE_CONTAINS |
    | /simple       | { "service" : "dynamicValue123" }| 200              | dynamicValue123   |