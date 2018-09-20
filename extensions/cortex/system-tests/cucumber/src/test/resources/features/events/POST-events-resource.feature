@events
Feature: POST to events resource

  Background:
    Given I login as a public shopper

  Scenario Outline: POST to events resource test
    When I POST an event <JSON> to URI <events-resource>
    Then the HTTP status code is 200

    Examples:
      | events-resource | JSON                                                                                                                                                                                      |
      | events/mobee    | {"type": "events/roleTransition","oldUserGuid": "5d4c0961-d493-4952-abbe-4e15e819ee9d","newUserGuid": "4FEE8C7F-1F50-3D62-08F1-67E3CE1928A3","oldRole": "PUBLIC","newRole": "REGISTERED"} |