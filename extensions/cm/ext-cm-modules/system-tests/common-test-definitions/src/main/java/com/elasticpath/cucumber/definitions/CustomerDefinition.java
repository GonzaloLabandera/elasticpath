package com.elasticpath.cucumber.definitions;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.editor.CustomerEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.CustomerService;
import com.elasticpath.selenium.resultspane.CustomerSearchResultsPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.util.Constants;

/**
 * Customer step definition.
 */
public class CustomerDefinition {
	private final CustomerService customerService;
	private CustomerSearchResultsPane customerSearchResultsPane;
	private CustomerEditor customerEditor;
	private final ActivityToolbar activityToolbar;

	/**
	 * Constructor.
	 */
	public CustomerDefinition() {
		customerService = new CustomerService(SeleniumDriverSetup.getDriver());
		activityToolbar = new ActivityToolbar(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Click customer tab.
	 */
	@When("^I select Customers tab")
	public void clicksCustomersTab() {
		customerService.clickCustomersTab();
	}

	/**
	 * Search for customer.
	 *
	 * @param customerEmailID the customer email id.
	 */
	@When("^I search for customer with email ID (.+)$")
	public void searchCustomer(final String customerEmailID) {
		clicksCustomersTab();
		enterCustomerEmailID(customerEmailID);
		clickCustomerSearch();

		int index = 0;

		while (!customerSearchResultsPane.isCustomerInList(customerEmailID) && index < Constants.UUID_END_INDEX) {
			customerSearchResultsPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			clickCustomerSearch();
			index++;
		}
	}

	/**
	 * Verify Customer exists.
	 *
	 * @param expectedCustomerID the expected customer Id.
	 */
	@Then("^I should see customer with email ID (.+) in result list$")
	public void verifyCustomerExists(final String expectedCustomerID) {
		customerSearchResultsPane.verifyCustomerExists(expectedCustomerID);
	}

	/**
	 * Search and open Customer editor.
	 *
	 * @param customerEmailID the customer email id.
	 */
	@When("^I search and open customer editor for email ID (.+)$")
	public void openCustomerEditor(final String customerEmailID) {
		activityToolbar.clickCustomerServiceButton();
		customerService.clickCustomersTab();
		enterCustomerEmailID(customerEmailID);
		clickCustomerSearch();
		customerEditor = customerSearchResultsPane.selectAndOpenCustomerEditor(customerEmailID);
	}

	/**
	 * Search for customer by phone number.
	 * @param phoneNumber phone number.
	 */
	@When("^I search for customer by phone number (.+)$")
	public void searchCustomerByPhone(final String phoneNumber) {
		activityToolbar.clickCustomerServiceButton();
		customerService.clickCustomersTab();
		customerService.clearInputFields();
		enterPhoneNumber(phoneNumber);
		clickCustomerSearch();
	}

	/**
	 * Select Customer editor tab.
	 *
	 * @param tabName the tab name.
	 */
	@When("^I select (.+) tab in the Customer Editor$")
	public void selectCustomerEditorTab(final String tabName) {
		customerEditor.clickTab(tabName);
	}

	/**
	 * Updates customer profile phone number.
	 * @param phoneNumber phone number.
	 */
	@When("^I update the phone number to (.+)$")
	public void updatePhoneNumber(final String phoneNumber) {
		customerEditor.enterPhoneNumber(phoneNumber);
		activityToolbar.saveAll();
		activityToolbar.clickReloadActiveEditor();
		customerEditor.closeCustomerEditor();
		customerSearchResultsPane.closePane("Customer Search Results");
	}


	private void enterCustomerEmailID(final String customerEmailID) {
		customerService.enterEmailUserID(customerEmailID);
	}

	private void enterPhoneNumber(final String phoneNumber) {
		customerService.enterPhoneNumber(phoneNumber);
	}

	private void clickCustomerSearch() {
		customerSearchResultsPane = customerService.clickCustomerSearch();
	}
}
