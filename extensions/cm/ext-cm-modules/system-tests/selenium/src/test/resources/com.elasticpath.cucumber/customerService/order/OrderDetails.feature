@regressionTest @customerService @order
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

  Scenario: Order is unlocked when modified order saved
    Given I have an order for scope mobee with following skus
      | skuCode                 | quantity |
      | handsfree_shippable_sku | 1        |
    When I search and open order editor for the latest order
    And I update and save the following shipping address for the order
      | address line 1 | 751 Pike Rd |
      | phone          | 616 2323231 |
    Then Unlock Order button is disabled

  Scenario Outline: Parent physical item with digital dependent line item
    Given I have an order for scope mobee with following skus
      | skuCode     | quantity   |
      | hama_bt_sku | <quantity> |
    When I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    Then I should see the following skus and quantity in physical shipment list
      | hama_bt_sku | <quantity> |
    And I should see the following skus and quantity in e-shipment list
      | eco_h_sku | <quantity> |

    Examples:
      | quantity |
      | 1        |
      | 2        |

  @cleanupDependentItem
  Scenario Outline: Parent digital item with physical dependent line item
    Given I go to Catalog Management
    And I am viewing the Merchandising Associations tab of an existing product with product code <parent-product-code>
    And I select the catalog Mobile Virtual Catalog tab
    And I add product code <dependent-lineItem-product-code> to merchandising association Dependent Item
    And I have an order for scope mobee with following skus
      | skuCode           | quantity   |
      | <parent-sku-code> | <quantity> |
    And I go to Customer Service
    When I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    Then I should see the following skus and quantity in physical shipment list
      | <physical-shipment-sku-code> | <quantity> |
    And I should see the following skus and quantity in e-shipment list
      | <e-shipment-sku-code> | <quantity> |

    Examples:
      | parent-product-code | parent-sku-code | quantity | dependent-lineItem-product-code | dependent-lineItem-sku-code          | physical-shipment-sku-code           | e-shipment-sku-code |
      | GA19920             | GA19920_sku     | 1        | handsfree_efah1234              | EAF8C586-4D92-42BE-B721-E7975485B721 | EAF8C586-4D92-42BE-B721-E7975485B721 | GA19920_sku         |

  @cleanupDependentItem
  Scenario Outline: Parent physical item with physical dependent line item
    Given I go to Catalog Management
    And I am viewing the Merchandising Associations tab of an existing product with product code <parent-product-code>
    And I select the catalog Mobile Virtual Catalog tab
    And I add product code <dependent-lineItem-product-code> to merchandising association Dependent Item
    And I have an order for scope mobee with following skus
      | skuCode           | quantity   |
      | <parent-sku-code> | <quantity> |
    And I go to Customer Service
    When I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    Then I should see the following skus and quantity in physical shipment list
      | <physical-shipment-sku-code-1> | <quantity> |
      | <physical-shipment-sku-code-2> | <quantity> |

    Examples:
      | parent-product-code | parent-sku-code | quantity | dependent-lineItem-product-code | dependent-lineItem-sku-code | physical-shipment-sku-code-1 | physical-shipment-sku-code-2 |
      | iPhone10            | iphone10        | 1        | physicalProduct                 | physical_sku                | iphone10                     | physical_sku                 |

  @cleanupDependentItem
  Scenario Outline: Parent digital item with digital dependent line item
    Given I go to Catalog Management
    And I am viewing the Merchandising Associations tab of an existing product with product code <parent-product-code>
    And I select the catalog Mobile Virtual Catalog tab
    And I add product code <dependent-lineItem-product-code> to merchandising association Dependent Item
    And I have an order for scope mobee with following skus
      | skuCode           | quantity   |
      | <parent-sku-code> | <quantity> |
    And I go to Customer Service
    When I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    Then I should see the following skus and quantity in e-shipment list
      | <e-shipment-sku-code-1> | <quantity> |
      | <e-shipment-sku-code-2> | <quantity> |

    Examples:
      | parent-product-code | parent-sku-code | quantity | dependent-lineItem-product-code | dependent-lineItem-sku-code | e-shipment-sku-code-1 | e-shipment-sku-code-2 |
      | GA19920             | GA19920_sku     | 1        | eco_fee_headphones              | eco_h_sku                   | GA19920_sku           | eco_h_sku             |

  Scenario: Verify information of B2B shopper that placed order from default cart
    Given I login using jwt authorization with the following details
      | roles      | BUYER                     |
      | user-name  | Rubeus Hagrid             |
      | user-email | rubeus.hagrid@hogwarts.com |
      | user-id    | aaaa-0000-bbbb-1111       |
    And Adding an item with item code plantsVsZombies and quantity 1 to the cart
    And I fill in email needinfo
    And I fill in payment methods needinfo
    And I fill in billing address needinfo
    When the order is submitted
    Then the HTTP status is OK
    When I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    Then I should see the following in the order data table
      | name       | Rubeus Hagrid             |
      | user-name  | Rubeus Hagrid             |
      | user-email | rubeus.hagrid@hogwarts.com |
      | user-id    | aaaa-0000-bbbb-1111       |

  Scenario: Verify information of B2B shopper that placed order from named cart
    Given I login using jwt authorization with the following details
      | roles      | BUYER                     |
      | user-name  | Rubeus Hagrid             |
      | user-email | rubeus.hagrid@hogwarts.com |
      | user-id    | aaaa-0000-bbbb-1111       |
    And I fill in email needinfo
    And I fill in billing address needinfo
    And I create a new shopping cart with name family
    And I fill in payment methods needinfo for cart family
    When I add plantsVsZombies to cart family with quantity 1
    And the order for cart family is submitted
    Then the HTTP status is OK
    When I search and open order editor for the latest order
    And I select Details tab in the Order Editor
    Then I should see the following in the order data table
      | name       | family                    |
      | user-email | rubeus.hagrid@hogwarts.com |
      | user-id    | aaaa-0000-bbbb-1111       |
      | user-name  | Rubeus Hagrid             |