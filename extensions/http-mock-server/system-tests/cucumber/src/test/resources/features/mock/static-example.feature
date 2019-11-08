@wiremock @static-example
Feature: Wiremock Simple Static Example Service

  Scenario Outline: Example service returns a valid 200 response with static body
    Given Wiremock service is up
    When I GET the <MOCK_ENDPOINT> endpoint
    Then I receive a <HTTP_STATUS_CODE> response with a body that contains <RESPONSE_CONTAINS>

    Examples:
    | MOCK_ENDPOINT   | HTTP_STATUS_CODE | RESPONSE_CONTAINS |
    | /static         | 200              | "service": "1"    |
    | /staticByPath/1 | 200              | "responseId": "1"    |
    | /staticByPath/2 | 200              | "responseId": "2"    |
    | /staticByPath/3 | 200              | "responseId": "3"    |