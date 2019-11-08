@offer
Feature: Sort offer search results

  Background:
    Given I am logged in as a public shopper

  Scenario: Sort offer result by a string in ascending order
    When I search for offer Movies
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value name A-Z
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | 12 Angry Men       |
      | Alien              |
      | Avatar             |
      | Avengers           |
      | Back To The Future |

  Scenario: Sort offer result by a string in descending order
    When I search for offer Movies
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value name Z-A
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | Twilight                      |
      | Transformers Registered       |
      | Transformers Over 50          |
      | Transformers First Time Buyer |
      | Transformers Female           |

  Scenario: Sort offer result by a number in descending order
    When I search for offer Movies
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value rating high to low
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | Super Bundle     |
      | Top Level Bundle |
      | The Godfather    |
      | Avatar           |
      | Casablanca       |

  Scenario: Sort offer result by a number in ascending order
    When I search for offer Movies
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value rating low to high
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | Twilight                |
      | Transformers Registered |
      | Transformers Female     |
      | Movie Deal              |
      | Rent Movies Bundle      |

  Scenario: Sort offer result by a boolean in descending order
    When I search for offer Games
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value featured
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | Wing Warrior    |
      | Super Game Pack |

  Scenario: Sort offer result by a boolean in ascending order
    When I search for offer Games
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value Not Featured
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | Little Adventure   |
      | Fun Games Pack     |
      | Tetris Heaven      |
      | Plants vs. Zombies |
      | Angry Birds        |

  Scenario: Sort offer result by price in ascending order
    When I am shopping in locale en with currency CAD
    And I search for offer Movies
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value Price low to high
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | se7en         |
      | Old Movies    |
      | 12 Angry Men  |
      | The Godfather |
      | Casablanca    |

  Scenario: Sort offer result by price in descending order
    When I am shopping in locale en with currency CAD
    And I search for offer Movies
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I select the choice with field display-name and value Price high to low
    And I follow the link offersearchresult
    Then the element list contains items with display-names
      | RentMovieLowTVCombo  |
      | Super Bundle         |
      | Movie Deal           |
      | Extreme Movie Bundle |
      | Just Released Movies |

  Scenario: Changing locale displays the name for that locale
    When I am shopping in locale fr with currency CAD
    And I search for offer Movies
    And I follow links sortattributes
    Then there are 8 links of rel choice
    And there is 1 link of rel chosen
    When I follow links choice -> description
    Then the field display-name has value name FR A-Z

