@regressionTest @catalogManagement @catalog
Feature: Catalog Search

  Background:
    Given I sign in to CM as admin user
    And I go to Catalog Management

# Scenario for product search by product name is a part of Changeset.feature - "Lock and unlock object in changeset" scenario and SanityTest.feature

  @cleanUpProductEnableDateDB
  Scenario Outline: Search for active and non-active product by product code with "Show only active products" filter applied
    When I search and open an existing product with product code <productCode>
    And I enter future date in Enable Date / Time field and save changes
    And I close product search results tab
    And I search for product by code <productCode>
    Then I should see empty result list

    Examples:
      | productCode |
      | alien       |

  Scenario Outline: Product search by product sku
    When I search for product by sku <productSku>
    Then Product with sku <productSku> should appear in result

    Examples:
      | productSku |
      | alien_sku  |

  Scenario Outline: Bundle search by bundle code
    When I search for bundle by code <bundleCode>
    Then Bundle code <bundleCode> should appear in result

    Examples:
      | bundleCode                             |
      | bundleWithPhysicalAndDigitalComponents |

  Scenario Outline: Sku search for sku associated with single sku product by sku code
    When I search for sku by sku code <productSku>
    Then Entity with sku code <productSku> should appear in result
    And I should not see Open Parent Product button in Sku Details tab for found entity
    And I should not see Bundle Items tab in a currently opened Product Editor

    Examples:
      | productSku |
      | alien_sku  |

  Scenario Outline: Sku search for sku associated with multiple sku product by sku code
    When I search for sku by sku code <skuCode>
    Then Entity with sku code <skuCode> should appear in result
    And I should see Open Parent Product button in Sku Details tab for found sku

    Examples:
      | skuCode |
      | 72542_2 |

  Scenario Outline: Sku search for sku associated with bundle by sku code
    When I search for sku by sku code <skuCode>
    Then Entity with sku code <skuCode> should appear in result
    And I should not see Open Parent Product button in Sku Details tab for found entity
    And I should see Bundle Items tab in a currently opened Product Editor

    Examples:
      | skuCode        |
      | mb_5678901_sku |

  Scenario Outline: Sku search for sku associated with multiple sku product by product name
    When I search for sku by product name <productName>
    Then All entries in result list have product name <productName>
    And Entity with sku code portable_tv_hdbuy_sku should appear in result
    And Entity with sku code portable_tv_hdrent_sku should appear in result

    Examples:
      | productName |
      | Portable TV |

  Scenario: Sku search for sku associated with multiple sku product by product code
    When I search for sku by product code portable_tv
    Then All entries in result list have product name Portable TV
    And Entity with sku code portable_tv_hdbuy_sku should appear in result
    And Entity with sku code portable_tv_hdrent_sku should appear in result

  Scenario: Sku search for sku by Sku Options Filter
    When I search for sku by Sku Option Billed (Frequency) and Sku Option value per month (MONTHLY)
    Then Entity with sku code phone_plan_mon_sku should appear in result