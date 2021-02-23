@permissions
Feature: Limited Catalog Browser role for account user

  Background:
    Given I authenticate with BUYER username usertest3@elasticpath.com and password password and role REGISTERED in scope mobee
    And I add X-Ep-Account-Shared-Id header LimitedCatalogBrowser@elasticpath.com

  Scenario: Account gets role LIMITED_CATALOG_BROWSER from associations and has no access to cart.
    When I go to my cart
    Then I should not see the following links
      | additemstocartform |

  Scenario: Account gets role LIMITED_CATALOG_BROWSER from associations from store and has no access to prices.
    Given an item Product With No Discounts exists in my catalog
    Then I should not see the following links
      | price |

  Scenario: Account gets role LIMITED_CATALOG_BROWSER from associations from store and has no access to discount.
    When I go to my cart
    Then I should not see the following links
      | discount |

  Scenario: Account gets role LIMITED_CATALOG_BROWSER from associations from store and has no access to total.
    When I go to my cart
    Then I should not see the following links
      | total |

  Scenario: Account gets role LIMITED_CATALOG_BROWSER from associations and can not create custom cart.
    When I navigate to root
    Then I should not see the following links
      | carts |
