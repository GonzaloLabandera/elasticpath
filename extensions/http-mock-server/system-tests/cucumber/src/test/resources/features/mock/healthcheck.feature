@wiremock @healthcheck
Feature: Wiremock Healthcheck Service

  Scenario Outline: Healthcheck returns a valid response
    Given Wiremock service is up
    When I GET the <MOCK_ENDPOINT> endpoint
    Then I receive a <HTTP_STATUS_CODE> response with an empty body

    Examples:
      | MOCK_ENDPOINT | HTTP_STATUS_CODE |
      | /healthcheck  | 200              |


