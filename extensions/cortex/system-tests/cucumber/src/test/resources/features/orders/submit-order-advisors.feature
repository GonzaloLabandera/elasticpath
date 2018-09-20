@Purchases

Feature: purchase resource advisor tests

  Scenario: Cart with product has billing address, email, and payment method advisors on order when none set
    Given I am logged in as a public shopper
    And I add item with code FocUSsku to my cart
    When I retrieve the purchase form
    Then there are advisor messages with the following fields:
      | messageType | messageId             | debugMessage                                                             | linkedTo                             | blocks            |
      | needinfo    | need.billing.address  | A billing address must be provided before you can complete the purchase. | orders.billingaddress-info           | submitorderaction |
      | needinfo    | need.email            | An email address must be provided before you can complete the purchase.  | orders.email-info                    | submitorderaction |
      | needinfo    | need.payment.method   | A payment method must be provided before you can complete the purchase.  | paymentmethods.paymentmethod-info    | submitorderaction |
      | needinfo    | need.shipment.details | Shipment details must be provided before you can complete the purchase.  | shipmentdetails.destination-info     | submitorderaction |
      | needinfo    | need.shipment.details | Shipment details must be provided before you can complete the purchase.  | shipmentdetails.shipping-option-info | submitorderaction |
    When I create an email for my order
    And I fill in billing address needinfo
    And I fill in payment methods needinfo
    Then there is no advisor linked to billingaddress-info
    And there is no advisor linked to email-info
    And there is no advisor linked to paymentmethod-info
    And there is no advisor linked to destination-info
    And there is no advisor linked to shipping-option-info
    And I can make a purchase

  Scenario: Cannot submit order when needinfo link exists
    Given I login as a public shopper
    And I add item with code FocUSsku to my cart
    And I create address with Country GB, Extended-Address County with no shipping options, Locality London, Organization Company Inc, Phone-Number 555-555-5555, Postal-Code N0N 0N0, Region , Street-Address fake street, Family-Name Test and Given-Name Test
    And I create an email for my order
    And I fill in payment methods needinfo
    And I retrieve the purchase form
    And there is an advisor message with the following fields:
      | messageType | messageId             | debugMessage                                                            | linkedTo                             | blocks            |
      | needinfo    | need.shipment.details | Shipment details must be provided before you can complete the purchase. | shipmentdetails.shipping-option-info | submitorderaction |
    When post to a created submitorderaction uri
    Then the HTTP status is conflict