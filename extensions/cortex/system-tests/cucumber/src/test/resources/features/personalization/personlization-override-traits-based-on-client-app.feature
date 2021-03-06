@personalization
Feature: Test overriding traits from a client application
  I want to override traits that are defined in CE for the authenticated shopper
  so that I have the flexibility to control customer traits from the client application

  Background:
    Given I am logged in as a public shopper

  Scenario Outline: Verify the default product prices are the same for male and female users
    When I authenticate as a registered shopper <USERNAME> with the default scope
    And I request the purchase price for item <MOVIE>
    Then I get the purchase price equal to <PRICE>

    Examples:
      | USERNAME                    | MOVIE               | PRICE  |
      | female.user@elasticpath.com | Transformers Female | $29.99 |
      | male.user@elasticpath.com   | Transformers Female | $29.99 |

  Scenario: Verify the product price when overriding the personalization header with multiple traits
    Given I am logged in as a public shopper
    And Hugo has an original purchase price equal to $34.99

    When I append to the overwritten personalization header the key GEOIP_COUNTRY_CODE and value CA
    And I append to the overwritten personalization header the key GEOIP_STATE_OR_PROVINCE and value BC
    And I request the purchase price for item Hugo
    Then I get the purchase price equal to $24.99

  Scenario Outline: Verifying purchase price of shoppers from different countries
    Given I am logged in as a public shopper

    When I append to the overwritten personalization header the key <HEADER_KEY> and value <HEADER_VALUE>
    And I request the purchase price for item <MOVIE>
    Then I get the purchase price equal to <PRICE>

    Examples:
      | MOVIE    | HEADER_KEY         | HEADER_VALUE | PRICE  |
      | Twilight | GEOIP_COUNTRY_CODE | CA           | $27.99 |
      | Twilight | GEOIP_COUNTRY_CODE | US           | $47.47 |
      | Twilight | GEOIP_COUNTRY_CODE | ''           | $47.47 |

  Scenario: Verify the shopper can override the shopping start time
    Given I am logged into scope MOBEE as a public shopper
    And I add Alien to my default cart with quantity 1
    And I go to my default cart
    And the list of applied promotions is empty
    When I append to the overwritten personalization header the key SHOPPING_CONTEXT_DATE_OVERRIDE and value 2100-01-10T00:00:00
    And I go to my default cart
    Then the list of applied promotions contains promotion FutureCartPromoMobee
