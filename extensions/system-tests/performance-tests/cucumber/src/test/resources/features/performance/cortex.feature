Feature: Cortex performance tests
  This is a set of Cortex-related performance tests, using various shopping workflows.
  When using "I start measuring db queries on <SERVER> server", the application cache is automatically cleaned.
  If desired, the test may not clean the application cache by using "I start measuring db queries <SERVER> server without cleaning cache" step

  1. Full Cortex shopping workflow
     This is very comprehensive and the most common shopping workflow:
        - login as anonymous use
        - using DB lookup, search for SKUs
        - add SKUs to cart
        - view cart (no zoom)
        - view cart (with zoom)
        - registering a user
        - getting user profile
        - check address profile
        - create a default address
        - view payment methods
        - create payment instrument
        - view payment instrument
        - get shipping options
        - select shipping option
        - view order detail
        - checkout
        - view order receipt (no zoom)
        - view order receipt (with zoom)
        - logout

  Scenario Outline: Full Cortex shopping workflow
    Given I start measuring db queries on cortex server
    When I login as a public shopper
    And I add following items to the cart
      | sony_bt_sku                                                |
      | 20off_shipping_sku                                         |
      | physical_product_with_lineitem_promotion_sku               |
      | physicalItemFromBundleWithPhysicalAndDigitalComponents_sku |
      | digitalItemFromBundleWithPhysicalAndDigitalComponents_sku  |
      | FocUSsku                                                   |
      | t384lkef                                                   |
      | mk34abef                                                   |
      | motox_sku                                                  |
      | physical_sku                                               |
      | tt0373883                                                  |
      | tt0926084_sku                                              |
      | tt0984938                                                  |
      | tt0034583_sku                                              |
      | tt1483735                                                  |
      | tt0068646_sku                                              |
      | tt0050083_sku                                              |
      | tt0162661                                                  |
      | mb_8974323_sku                                             |
      | mb_1234567_sku                                             |
      | mb_5678901_sku                                             |
      | mb_2893033                                                 |
      | mb_4324324_sku                                             |
      | mb_9012345_sku                                             |
      | tt0970179_sku                                              |
      | transformers01_sku                                         |
      | transformers02_sku                                         |
      | transformers03_sku                                         |
      | transformers04_sku                                         |
      | gravity_sku                                                |
      | alien_sku                                                  |
    And I view default cart
    And I zoom to cart with a query <LARGE_ZOOM>
    And I transition to registered shopper
    And I retrieve default user profile
    And I retrieve all user addresses
    And I create default billing Canadian address
    And I view all payment methods
    And I create default saved payment instrument
    And I view default payment instrument
    And I list all shipping options
    And I select shipping option CanadaPostExpress
    And I view cart order
    And I checkout
    And I view order receipt
    And I zoom order receipt
    And I logout
    And I wait 3 seconds
    Then I stop measuring db queries

    # Large zoom query from client project that puts a strain on the backend.
    Examples:
      | LARGE_ZOOM                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
      | lineitems:element,lineitems:element:item:definition,lineitems:element:item:price,lineitems:element:item:code,lineitems:element:availability,lineitems:element:price,lineitems:element:total,lineitems:element:appliedpromotions,lineitems:element:appliedpromotions:element,lineitems:element:appliedpromotions:element:discountedproductselection:discountedlineitem:item:code,lineitems:element:satisfiedpromotions,lineitems:element:satisfiedpromotions:element,lineitems:element:satisfiedpromotions:element:discountedproductselection,lineitems:element:satisfiedpromotions:element:discountedproductselection:discountableitem:availability,lineitems:element:satisfiedpromotions:element:discountedproductselection:discountableitem:code,lineitems:element:satisfiedpromotions:element:discountedproductselection:discountableitem:definition,lineitems:element:satisfiedpromotions:element:discountedproductselection:discountableitem:price,lineitems:element:satisfiedpromotions:element:discountedproductselection:discountableitem:total,lineitems:element:satisfiedpromotions:element:discountedproductselection:discountableitem:appliedpromotions,lineitems:element:satisfiedpromotions:element:discountedproductselection:discountableitem:appliedpromotions:element,order,order:total,order:tax,order:error,appliedpromotions,appliedpromotions:element,discount |