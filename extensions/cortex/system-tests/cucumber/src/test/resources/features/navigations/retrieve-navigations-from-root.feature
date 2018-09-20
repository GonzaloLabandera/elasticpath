@Navigations
Feature: Retrieve Navigation nodes and child nodes

  Scenario: Can find a list of navigations from the root.
    Given I am logged in as a public shopper
    When I follow the root navigations link
    Then the expected navigation list exactly matches the following
      | Games               |
      | Accessories         |
      | Smartphones         |
      | Movies              |
      | TV                  |
      | GiftCertificate     |
      | phone_plan          |
      | AcceptanceTestItems |

  Scenario Outline: Can find child nodes
    Given I am logged in as a public shopper
    When I follow the root navigations link
    Then the navigation node <CATEGORY_NAME> should contain exactly the following child nodes
      | MobileGames |
      | VideoGames  |
    And the child node <SUB_CATEGORY> should contain the following sub child nodes
      | AndroidGames |
      | IPhoneGames  |

    Examples:
      | CATEGORY_NAME | SUB_CATEGORY |
      | Games         | MobileGames  |

