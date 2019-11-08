@wiremock @soap-example
Feature: Wiremock Soap Dynamic Example Service

  Scenario Outline: Example SOAP service returns a valid 200 response with SOAP XML body
    Given Wiremock service is up
    When I POST to the <MOCK_ENDPOINT> endpoint with request <REQUEST_FILE>
    Then I receive a <HTTP_STATUS_CODE> response with a body that contains <RESPONSE_CONTAINS>

    Examples:
    | MOCK_ENDPOINT | REQUEST_FILE                     | HTTP_STATUS_CODE | RESPONSE_CONTAINS |
    | /soap         | soap/request.xml                 | 200              | <dcId>1</dcId>   |