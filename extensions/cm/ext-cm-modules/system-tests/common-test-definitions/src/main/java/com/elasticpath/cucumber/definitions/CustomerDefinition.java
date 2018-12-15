package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.editor.CustomerEditor;
import com.elasticpath.selenium.navigations.CustomerService;
import com.elasticpath.selenium.resultspane.CustomerSearchResultsPane;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.DBConnector;

/**
 * Customer step definition.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class CustomerDefinition {
	private final CustomerService customerService;
	private CustomerSearchResultsPane customerSearchResultsPane;
	private CustomerEditor customerEditor;
	private final ActivityToolbar activityToolbar;

	/**
	 * Constructor.
	 */
	public CustomerDefinition() {
		customerService = new CustomerService(SetUp.getDriver());
		activityToolbar = new ActivityToolbar(SetUp.getDriver());
	}

	/**
	 * Click customer tab.
	 */
	@When("^I select Customers tab")
	public void clicksCustomersTab() {
		customerService.clickCustomersTab();
	}

	/**
	 * Search for customer by email or user id.
	 *
	 * @param customerEmailID the customer email id.
	 */
	@When("^I search for customer by email ID (.+)$")
	public void searchCustomerIdEmail(final String customerEmailID) {
		searchCustomer(() -> enterCustomerEmailID(customerEmailID));
	}

	/**
	 * Search for customer by email or user id with Store filter.
	 *
	 * @param customerEmailID the customer email id.
	 * @param store           Store associated with the customer
	 */
	@When("^I search for customer by email (.+) with store filter (.+)$")
	public void searchCustomerEmailAndStore(final String customerEmailID, final String store) {
		fillSearchForm(() -> customerService.enterEmailUserID(customerEmailID));
		customerService.selectStore(store);
		clickCustomerSearch();
	}

	/**
	 * Search for customer by First name.
	 *
	 * @param firstName a customer First name.
	 */
	@When("^I search for customer by first name (.+)$")
	public void searchCustomerFirstName(final String firstName) {
		searchCustomer(() -> customerService.enterFirstName(firstName));
	}

	/**
	 * Search for customer by Last name.
	 *
	 * @param lastName a customer Last name.
	 */
	@When("^I search for customer by last name (.+)$")
	public void searchCustomerLastName(final String lastName) {
		searchCustomer(() -> customerService.enterLastName(lastName));
	}

	/**
	 * Search for customer by Zip Code.
	 *
	 * @param zipCode a customer Zip Code.
	 */
	@When("^I search for customer by zip code (.+)$")
	public void searchCustomerZipCode(final String zipCode) {
		searchCustomer(() -> customerService.enterPostalCode(zipCode));
	}

	/**
	 * Verify Customer exists.
	 *
	 * @param expectedCustomerID the expected customer Id.
	 */
	@Then("^I should see customer with email ID (.+) in result list$")
	public void verifyCustomerExists(final String expectedCustomerID) {
		verifyCustomerSearchResult(expectedCustomerID, CustomerSearchResultsPane.EMAIL_ADDRESS_COLUMN);
	}

	/**
	 * Checks if there is entry with provided First name in a search result table.
	 *
	 * @param expectedFirstName an expected First name.
	 */
	@Then("^I should see customer with first name (.+) in result list$")
	public void verifyFirstNameEntryInList(final String expectedFirstName) {
		verifyCustomerSearchResult(expectedFirstName, CustomerSearchResultsPane.FIRST_NAME_COLUMN);
	}

	/**
	 * Checks if there is entry with provided Last name in a search result table.
	 *
	 * @param expectedLastName an expected First name.
	 */
	@Then("^I should see customer with last name (.+) in result list$")
	public void verifyLastNameEntryInList(final String expectedLastName) {
		verifyCustomerSearchResult(expectedLastName, CustomerSearchResultsPane.LAST_NAME_COLUMN);
	}

	/**
	 * Checks if there is entry with provided Zip code in a search result table.
	 *
	 * @param expectedZipCode an expected zip code.
	 */
	@Then("^I should see customer with zip code (.+) in result list$")
	public void verifyZipCodeEntryInList(final String expectedZipCode) {
		verifyCustomerSearchResultPartialMatch(expectedZipCode, CustomerSearchResultsPane.BILLING_ADDRESS_COLUMN_INDEX);
	}

	/**
	 * Checks if there is entry with provided Phone number in a search result table.
	 *
	 * @param phoneNumber an expected phone number.
	 */
	@Then("^I should see customer with phone number (.+) in result list$")
	public void verifyPhoneNumberEntryInList(final String phoneNumber) {
		verifyCustomerSearchResult(phoneNumber, CustomerSearchResultsPane.PHONE_NUMBER_COLUMN);
	}

	/**
	 * Checks if search result table is empty.
	 */
	@Then("^I should see empty search results table$")
	public void verifyEmptyResultTable() {
		assertThat(customerSearchResultsPane.isSearchResultTableEmpty())
				.as("Search result table should be empty, but contains at least one row")
				.isTrue();
	}

	/**
	 * Checks if search result table has specified amount of results.
	 */
	@Then("^I should see more than one row in result list$")
	public void verifyResultTableRowsAmountGreaterThanOne() {
		assertThat(customerSearchResultsPane.getSearchResultTableRowsAmount())
				.as("Search result table doesn't contain expected rows amount")
				.isGreaterThan(1);
	}

	/**
	 * Checks if all entries of search results table contain provided email address (partial match).
	 *
	 * @param expectedEmailAddress  expected email address for verification
	 */
	@Then("^All entries in result list have (.+) as a part of Email Address$")
	public void verifyUserIdEntriesInList(final String expectedEmailAddress) {
		customerSearchResultsPane.isCustomerValueInAllListEntries(expectedEmailAddress, CustomerSearchResultsPane.EMAIL_ADDRESS_COLUMN_INDEX);
	}

	/**
	 * Closes customer search results tab.
	 */
	@Then("^I close customer search results tab$")
	public void closeSearchResultsTab() {
		customerSearchResultsPane.closeCustomerSearchResultsPane();
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
	 *
	 * @param phoneNumber phone number.
	 */
	@When("^I search for customer by phone number (.+)$")
	public void searchCustomerByPhone(final String phoneNumber) {
		searchCustomer(() -> customerService.enterPhoneNumber(phoneNumber));
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
	 *
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
		//this call ensures that the search results table appears on the page
		customerSearchResultsPane.isSearchResultTableEmpty();
	}

	/**
	 * Select Data Policy to view.
	 *
	 * @param dataPolicyName the Data Policy name.
	 */
	@When("^I view Data Points for Data Policy (.+)$")
	public void viewDataPoints(final String dataPolicyName) {
		customerEditor.verifyDataPolicyExists(dataPolicyName);
		customerEditor.clickViewDataPointsButton();
	}

	/**
	 * Verifies data point value per data point.
	 *
	 * @param customerDataMap map
	 */
	@Then("^the following data points (?:captured correct customer data|remain captured)$")
	public void verifyDataPointsValue(final Map<String, String> customerDataMap) {
		for (String key : customerDataMap.keySet()) {
			customerEditor.verifyDataPointValue(key, customerDataMap.get(key));
		}
	}

	/**
	 * Select Data Policy to Delete Data points.
	 *
	 * @param dataPolicyName the Data Policy name.
	 */
	@When("^I Delete Data Points for Data Policy (.+)$")
	public void deleteDataPoints(final String dataPolicyName) {
		customerEditor.verifyDataPolicyExists(dataPolicyName);
		customerEditor.clickDeletePolicyDataButton();
	}

	/**
	 * Ensure Data Point Values in the Table are empty.
	 *
	 * @param dataPointNameList List of Data Point Names.
	 */
	@Then("^Data Point Values are empty for following Removable Data Points$")
	public void verifyDataPointValueIsEmpty(final List<String> dataPointNameList) {
		for (String nameField : dataPointNameList) {
			customerEditor.verifyDataPointValueIsEmpty(nameField);
		}
	}

	/**
	 * Ensure Data Point Values are set to Hyphen.
	 *
	 * @param dataPointNameList List of Data Point Names.
	 */
	@Then("^Data Point Values are set to Hyphen for following Removable Data Points$")
	public void verifyDataPointValueSetToHyphen(final List<String> dataPointNameList) {
		for (String nameField : dataPointNameList) {
			customerEditor.verifyDataPointValueSetToHyphen(nameField);
		}
	}

	/**
	 * Select Show disabled data policies.
	 */
	@When("^I click on Show Disabled Data Policies$")
	public void selectShowDisabledDataPolicy() {
		customerEditor.selectShowDisabledDataPolicy();
	}

	/**
	 * Data Policy should not be visible in the Table.
	 *
	 * @param dataPolicyName the Data Policy name.
	 */
	@When("^Disabled Data Policy (.+) should not be visible$")
	public void verifyDataPolicyIsNotExists(final String dataPolicyName) {
		customerEditor.verifyDataPolicyIsNotExists(dataPolicyName);

	}

	/**
	 * Verify Data Policy is present in the table.
	 *
	 * @param dataPolicyName the Data Policy name.
	 */
	@When("^I should see Disabled Data Policy (.+)$")
	public void verifyDataPolicyExists(final String dataPolicyName) {
		customerEditor.verifyDataPolicyExists(dataPolicyName);
		resetDataPolicyState(dataPolicyName);
	}

	/**
	 * Resets Data Policy State.
	 *
	 * @param dataPolicyName String.
	 */
	private void resetDataPolicyState(final String dataPolicyName) {
		DBConnector dbConnector = new DBConnector();
		dbConnector.executeUpdateQuery("UPDATE TDATAPOLICY SET STATE='1', END_DATE=NULL WHERE POLICY_NAME='" + dataPolicyName + "';");
		dbConnector.closeAll();
	}

	/**
	 * Runs a search for customer with full or partial search value and verifies that result table appears.
	 *
	 * @param runnable function which fills search field.
	 */
	private void searchCustomer(final Runnable runnable) {
		fillSearchForm(runnable);
		clickCustomerSearch();
	}

	/**
	 * Navigates to search form and fills it
	 *
	 * @param runnable function which fills search field.
	 */
	private void fillSearchForm(final Runnable runnable) {
		clicksCustomersTab();
		customerService.clearInputFieldsInCustomersTab();
		runnable.run();
	}

	/**
	 * Verify a customer is in a result list in a Search Results Table using full match equality for expected value in the table.
	 *
	 * @param verificationCriteria customer value for verification
	 * @param columnName           column name of the Search result table associated with verification criteria
	 */
	private void verifyCustomerSearchResult(final String verificationCriteria, final String columnName) {
		int index = 0;
		while (!customerSearchResultsPane.isCustomerInListFullMatch(verificationCriteria, columnName) && index < Constants.UUID_END_INDEX) {
			customerSearchResultsPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			clickCustomerSearch();
			index++;
		}
		assertThat(customerSearchResultsPane.isCustomerInListFullMatch(verificationCriteria, columnName))
				.as("The search result doesn't contain expected entry")
				.isTrue();
	}

	/**
	 * Verify a customer is in a result list in a Search Results Table using partial match equality for expected value in the table.
	 *
	 * @param verificationCriteria customer value for verification
	 * @param columnIndex          an index of the table column for verification
	 */
	private void verifyCustomerSearchResultPartialMatch(final String verificationCriteria, final int columnIndex) {
		int index = 0;
		while (!customerSearchResultsPane.isCustomerInListPartialMatch(verificationCriteria, columnIndex) && index < Constants.UUID_END_INDEX) {
			customerSearchResultsPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			clickCustomerSearch();
			index++;
		}
		assertThat(customerSearchResultsPane.isCustomerInListPartialMatch(verificationCriteria, columnIndex))
				.as("The search result doesn't contain expected entry")
				.isTrue();
	}

}
