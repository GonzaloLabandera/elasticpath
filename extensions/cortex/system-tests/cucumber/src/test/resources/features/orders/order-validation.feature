@Orders

Feature: Tests order resource validation

  Scenario: Cannot submit an order when you have no cart content
    Given I login as a registered shopper
    When I retrieve the purchase form
    And there are no needinfo links
    Then there are no submitorderaction links
    And post to a created submitorderaction uri
    And the HTTP status is conflict

  Scenario: Cart with physical product has billing address, email, payment method, and shipping details advisors on order when none set
    Given I am logged in as a public shopper
    When I add item with code FocUSsku to my cart
    Then I retrieve the order
    And there are advisor messages with the following fields:
      | messageType | messageId             | debugMessage                                                             | linkedTo                             |
      | needinfo    | need.billing.address  | A billing address must be provided before you can complete the purchase. | orders.billingaddress-info           |
      | needinfo    | need.email            | An email address must be provided before you can complete the purchase.  | orders.email-info                    |
      | needinfo    | need.payment.method   | A payment method must be provided before you can complete the purchase.  | paymentmethods.paymentmethod-info    |
      | needinfo    | need.shipment.details | Shipment details must be provided before you can complete the purchase.  | shipmentdetails.shipping-option-info |
      | needinfo    | need.shipment.details | Shipment details must be provided before you can complete the purchase.  | shipmentdetails.destination-info     |

  Scenario: Cart with no product has billing address and email advisors on order when none set
    Given I am logged in as a public shopper
    And I add item with code tt888456tw to my cart
    When I retrieve the order
    Then there are advisor messages with the following fields:
      | messageType | messageId            | debugMessage                                                             | linkedTo                   |
      | needinfo    | need.billing.address | A billing address must be provided before you can complete the purchase. | orders.billingaddress-info |
      | needinfo    | need.email           | An email address must be provided before you can complete the purchase.  | orders.email-info          |
    And there is no advisor linked to paymentmethod-info
    And there is no advisor linked to shipping-option-info
    And there is no advisor linked to destination-info