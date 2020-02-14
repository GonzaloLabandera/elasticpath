# language: en
@orderConfirmationEmail

Feature: Order Confirmation Emails to Both Account and Buyer
  So that I am sure my order has been received correctly,
  As a B2B buyer, after placing an order I want to be sent a confirmation email containing all relevant order details.
  As the account, I would like to receive the same email.

  Background:
    Given email sending is enabled
    And I am shopping in Store "Test Store"
    And the Store supports the en_US locale
    And my shopping locale is en_US
    And my email address account@example.org has been used to register my user account

  Scenario: An order confirmation email is sent to both account and buyer after the buyer makes a purchase
	Given I have an item in my shopping cart
    And my shopping cart with following cart data
      | roles      | BUYER                     |
      | user-name  | Rubeus Hagrid             |
      | user-email | buyer@example.org |
      | user-id    | aaaa-0000-bbbb-1111       |
    When I successfully purchase my shopping cart contents
    Then I should receive 1 email in each of following inboxes
      |account@example.org     |
      |buyer@example.org     |
    And the subject of the email should be "Order Confirmation"
    And the email should contain the order number
    And the email should contain the order total
    And the email should contain the order date
    And the email should contain the order items
    And the email should contain the order shipping address
