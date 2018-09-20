#TODO: investigate way to test time fields
@Navigations

Feature: Retrieve Navigation nodes and child nodes

  Scenario: Can retrieve navigation details
    Given I am logged in as a public shopper
    When I follow the root navigations link
    And open the element with field name of Games
    Then the navigation node attributes contain
      | name             | display name         | display value                                                            |
   #	  | catReleaseDate   | Release Date         | 2012                                                                     |
      | catDescription   | Category Description | This category contains listing of all Games titles                       |
      | catSubCategories | Subcategories        | Mobile Games, Video Games, 憤怒的小鳥, 怒っている鳥, नाराज पक्षियों, پرندگان عصبانی |
      | catReleased      | Released             | True                                                                     |
      | catRating        | Category Rating      | 8.25                                                                     |
      | catTotalItems    | Total items          | 5                                                                        |
   #	  | catLastModified  | Last modified        | 2012                                                                     |
      | catName          | Name                 | All <b><i>Games</i></b>                                                  |

  Scenario: Media attributes and details with no value are not displayed
    Given I am logged in as a public shopper
    When I follow the root navigations link
    And open the element with field name of Games
    And open the child with field name of MobileGames
    Then the field details does not contain value catReleased
    And the field details does not contain value catLastModified
    And the field details does not contain value catImage
    And the field details does not contain value catTheme

  Scenario Outline: Node displays detail values based on scope locale
    Given I am logged into scope <ENGLISH_SCOPE> as a public shopper
    And I follow the root navigations link
    And open the element with field name of Movies
    And the navigation node array field details contains
      | name    | value  |
      | catName | Movies |
    When I am logged into scope <FRENCH_SCOPE> as a public shopper
    And I follow the root navigations link
    And open the element with field name of Movies
    Then the navigation node array field details contains
      | name    | value  |
      | catName | Cinéma |

    Examples:
      | ENGLISH_SCOPE | FRENCH_SCOPE |
      | mobee         | toastie      |


  Scenario: Navigations node with no attributes associated with it will not display an attributes field
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Smartphones
    Then the field details does not exist

  Scenario: Verify links on node with child and no parent
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Games
    Then there are no parent links
    And there is a child link
    And there is a top link

  Scenario: Verify links on node with multiple children
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Games
    Then there are 2 links of rel child

  Scenario: Verify links on node with no parent and no child
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Smartphones
    Then there are no parent links
    And there are no child links
    And there is a top link

  Scenario: Verify links on node with parent and child
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Games
    And open the child with field name of MobileGames
    Then there is a parent link
    And there is a child link
    And there is a top link

  Scenario: Verify links on node with parent and no child
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Games
    And open the child with field name of MobileGames
    And open the child with field name of IPhoneGames
    Then there is a parent link
    And there are no child links
    And there is a top link
    And I follow links parent
    And the field name has value MobileGames

  Scenario Outline: Nodes have links to items
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of <NODE_ITEMS>
    Then there is a items link
    And I follow links items
    And there are 1 links of rel element

    Examples:
      | NODE_ITEMS      |
      | GiftCertificate |

  Scenario Outline: Items link always exists regardless if the node has items
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of <NODE_WITHOUT_ITEMS>
    Then there is a items link
    And I follow links items
    And there are 0 links of rel element

    Examples:
      | NODE_WITHOUT_ITEMS |
      | Games              |

  Scenario: Scope with no nodes has no navigation elements
    Given I am logged into scope rockjam as a public shopper
    When I follow the root navigations link
    Then there are no element links

  Scenario Outline: Configurable items in node
    Given I am logged in as a public shopper
    When I follow the root navigations link
    When open the element with field name of Movies
    And I follow links items
    Then there is an item with display-name <CONFIGURABLE_ITEM>

    Examples:
      | CONFIGURABLE_ITEM |
      | Avatar            |

  Scenario Outline: Items in node do not include subnode items
    Given I am logged in as a public shopper
    When I follow the root navigations link
    When open the element with field name of Games
    And open the child with field name of MobileGames
    And I follow links items
    Then there is not an item with display-name <SUBNODE_ITEM>

    Examples:
      | SUBNODE_ITEM    |
      | Super Game Pack |

  Scenario Outline: Items in node do not include parent items
    Given I am logged in as a public shopper
    When I follow the root navigations link
    And open the element with field name of Games
    And open the child with field name of MobileGames
    And open the child with field name of IPhoneGames
    And I follow links items
    Then there is not an item with display-name <PARENT_ITEM>

    Examples:
      | PARENT_ITEM   |
      | Tetris Heaven |

  Scenario: Featured items are in the specified order
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Movies
    And I follow links items
    Then the items are listed in the follow order
      | Die Hard      |
      | Casablanca    |
      | Avatar        |
      | Movie Deal    |
      | Sleepy Hallow |

  Scenario: Featured items show up first
    Given I am logged in as a public shopper
    And I follow the root navigations link
    When open the element with field name of Games
    And open the child with field name of MobileGames
    And open the child with field name of IPhoneGames
    And I follow links items
    Then the items are listed in the follow order
      | Super Game Pack  |
      | Wing Warrior     |
      | Little Adventure |

  Scenario Outline: Does not display not scope visible products
    Given I am logged into scope mobee as a public shopper
    And I follow the root navigations link
    When open the element with field name of Games
    And open the child with field name of MobileGames
    And open the child with field name of AndroidGames
    And I follow links items
    Then there is not an item with display-name <NOT_SCOPE_VISIBLE_PRODUCT>

    Examples:
      | NOT_SCOPE_VISIBLE_PRODUCT        |
      | Angry Birds Seasons - Valentines |