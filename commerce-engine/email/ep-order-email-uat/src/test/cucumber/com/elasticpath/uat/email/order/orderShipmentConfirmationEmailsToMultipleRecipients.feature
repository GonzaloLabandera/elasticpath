# language: en
@orderShipmentConfirmationEmail

Feature: Order Shipment Confirmation Email to Both Account and Buyer
  So that I know when to expect my shipment to arrive,
  As a B2B buyer, once my order has been shipped I would like to be sent a shipping confirmation email containing all relevant shipment details.
  As the account, I would like to receive the same email.

  Background:
	Given email sending is enabled
	And I am a customer of Store "Test Store"
	And the Store supports the en_US locale
	And my shopping locale is en_US
	And my email address account@example.org has been used to register my user account
	And my shopping cart with following cart data
	  | roles      | BUYER                     |
	  | user-name  | Rubeus Hagrid             |
	  | user-email | buyer@example.org |
	  | user-id    | aaaa-0000-bbbb-1111       |
  Scenario: An order shipment confirmation email is sent to both account and buyer after the shipment is completed
  	Given I have previously made a purchase
	When the shipment ships with tracking code "TRACK-001"
	Then I should receive 1 email in each of following inboxes
	  |account@example.org     |
	  |buyer@example.org     |
	And the subject of the email should be "Order Shipment Confirmation"
	And the email should contain the order number
	And the email should contain the shipment number
	And the email should contain the date the shipment shipped
	And the email should contain the shipment method
	And the email should contain the shipment tracking code "TRACK-001"
  	And the email should contain the order shipping address
	And the email should contain the shipment total
