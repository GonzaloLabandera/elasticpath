# language: en
@orderHoldNotificationEmail

Feature: Order Hold Notification Email
  In order to be alerted when customer's orders are placed On Hold so that I can investigate and resolve it,
  as the Customer Service Representative,
  I want to be sent an email when orders are currently in On Hold status.

  Background:
	Given email sending is enabled

  Scenario: an order is placed on hold, and an email is sent to the hold notification email address
    Given the customer is shopping in Store "Test Store"
    And the email address store.admin@example.org is configured as the on hold notification email address
	And the customer made a purchase when order hold enabled
	When the order hold notification job runs
	Then I should receive 1 email in my store.admin@example.org inbox
	And the subject of the email should be "Orders On Hold Notification"
	And the email should contain the store name
	And the email should contain the number of orders on hold