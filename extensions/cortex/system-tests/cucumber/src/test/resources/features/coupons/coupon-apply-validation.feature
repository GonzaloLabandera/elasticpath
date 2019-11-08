@coupons
Feature: Coupon is validated when applied to order

  Scenario: Anonymous customer with no email cannot apply Limit-Per-Specified-User coupon
    Given I am logged in as a public shopper
    When I apply an invalid coupon couponCodeLimitedByUserFor25PercentDiscount to the order
    Then the HTTP status is conflict
    And I should see validation error message with message type, message id, and debug message
      | messageType | messageId             | debugMessage                                                                            |
      | error       | coupon.email.required | Email address is required for the <couponCodeLimitedByUserFor25PercentDiscount> coupon. |

  Scenario: Anonymous customer with email can apply Limit-Per-Specified-User coupon
    Given I am logged in as a public shopper
    When I create an email
    When I apply a coupon code couponCodeLimitedByUserFor25PercentDiscount to the order
    Then the HTTP status is OK, created
    When I retrieve the coupon info for the order
    Then there is exactly 1 coupon applied to the order
