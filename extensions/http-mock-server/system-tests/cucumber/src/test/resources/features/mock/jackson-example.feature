@wiremock @jackson-example
Feature: Wiremock Soap Dynamic Jackson Example Service

  Scenario Outline: Example mock service returns a valid 200 response with JSON body
    Given Wiremock service is up
    When I POST to the <MOCK_ENDPOINT> endpoint with request <REQUEST_FILE>
    Then I receive a <HTTP_STATUS_CODE> response with a body that contains <RESPONSE_CONTAINS>

    Examples:
    | MOCK_ENDPOINT | REQUEST_FILE                     | HTTP_STATUS_CODE | RESPONSE_CONTAINS |
    | /jackson         | jackson/request.json                 | 200              | "status":"Complete"   |