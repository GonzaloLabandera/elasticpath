@smoketest @customerService @order
Feature: Order Item Detail

  Background:
    Given I sign in to CM as admin user
    And I go to Customer Service

  Scenario Outline: Configurable fields values saved in order item detail
    Given I have an order for scope mobee with sku <SKU>, quantity 1 and following configurable fields
      | giftCertificate.message        | <MESSAGE>         |
      | giftCertificate.recipientEmail | <RECIPIENT_EMAIL> |
      | giftCertificate.recipientName  | <RECIPIENT_NAME>  |
      | giftCertificate.senderName     | <SENDER_NAME>     |
    And I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    When I view item detail of the order line item <SKU>
    Then the item detail matches the configurable field values from the purchase
      | giftCertificate.message        | <MESSAGE>         |
      | giftCertificate.recipientEmail | <RECIPIENT_EMAIL> |
      | giftCertificate.recipientName  | <RECIPIENT_NAME>  |
      | giftCertificate.senderName     | <SENDER_NAME>     |

    Examples:
      | SKU        | MESSAGE      | RECIPIENT_EMAIL | RECIPIENT_NAME | SENDER_NAME |
      | berries_20 | Test Message | test@test.com   | Test Recipient | Test Sender |
