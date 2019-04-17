@httpCaching
Feature: HTTP Caching - Lookups

  Background:
    Given I login as a newly registered shopper

  Scenario: Lookups should have HTTP caching
    Given I retrieve the lookups link point
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Batch items lookup form should have HTTP caching
    Given I retrieve the batch items lookup form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Item lookup form should have HTTP caching
    Given I retrieve the item lookup form
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario Outline: Code for item should have HTTP caching
    Given I look up an item with code <ITEM_CODE>
    And the item code is <ITEM_CODE>
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

    Examples:
      | ITEM_CODE                     |
      | product_with_no_discounts_sku |

  Scenario: Batch Offer lookup form should have HTTP caching
    Given I go to batchofferslookupform
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response

  Scenario: Offer lookup form should have HTTP caching
    Given I go to offerlookupform
    Then I should see the Cache-Control header in the response with a valid max age and private directive
    And I should see an ETag header in the response