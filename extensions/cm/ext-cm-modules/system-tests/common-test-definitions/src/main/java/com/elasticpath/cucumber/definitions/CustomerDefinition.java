package com.elasticpath.cucumber.definitions;

import static com.elasticpath.selenium.framework.util.SeleniumDriverSetup.getDriver;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.dialogs.AddAccountAssociateDialog;
import com.elasticpath.selenium.dialogs.DeleteAccountAssociateDialog;
import com.elasticpath.selenium.editor.CustomerEditor;
import com.elasticpath.selenium.navigations.CustomerServiceNavigation;
import com.elasticpath.selenium.resultspane.AccountSearchResultPane;
import com.elasticpath.selenium.resultspane.CustomerSearchResultsPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.DBConnector;

/**
 * Customer step definition.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public class CustomerDefinition {
	private final CustomerServiceNavigation customerServiceNavigation;
	private CustomerSearchResultsPane customerSearchResultsPane;
	private CustomerEditor customerEditor;
	private AddAccountAssociateDialog addAccountAssociateDialog;
	private final ActivityToolbar activityToolbar;
	private final WebDriver driver;

	private static final String CUSTOMER_EDITOR_FIELDS_CSS = "div[appearance-id='label-wrapper'][automation-id*='com"
			+ ".elasticpath.cmclient.fulfillment.FulfillmentMessages.Profile%sSection']";

	private static final String CHILD_IN_TABLE_CSS = "div[parent-widget-id='Child Accounts'] div[column-id='%s']";
	private static final String OPEN_ACCOUNT_BUTTON_CSS = "div[widget-id='Open Account Profile...']";
	private static final String CHILD_ACCOUNT_BTN_CSS = "div[widget-id='Add Child Account...']";
	private static final String CHILD_ACCOUNT_HYPERLINK_CSS = "div[appearance-id='hyperlink'][widget-type=Hyperlink]";
	private static final String CUSTOMER_EDITOR_TABS_CSS = "div[appearance-id='ctab-item'][automation-id*='_Title']";

	/**
	 * Constructor.
	 */
	public CustomerDefinition() {
		driver = getDriver();
		customerServiceNavigation = new CustomerServiceNavigation(driver);
		activityToolbar = new ActivityToolbar(driver);
	}

	/**
	 * Click customer tab.
	 */
	@When("^I select Customers tab")
	public void clicksCustomersTab() {
		customerServiceNavigation.clickCustomersTab();
	}

	/**
	 * Search for customer by shared id.
	 *
	 * @param customerSharedID the customer shared id.
	 */
	@When("^I search for customer by shared ID (.+)$")
	public void searchCustomerSharedId(final String customerSharedID) {
		searchCustomer(() -> enterCustomerSharedID(customerSharedID));
	}

	/**
	 * Search for customer by email.
	 *
	 * @param customerEmail the customer email.
	 */
	@When("^I search for customer by email (.+)$")
	public void searchCustomerEmail(final String customerEmail) {
		searchCustomer(() -> enterCustomerEmail(customerEmail));
	}

	/**
	 * Search for customer by username.
	 *
	 * @param customerUsername the customer username.
	 */
	@When("^I search for customer by username (.+)$")
	public void searchCustomerUsername(final String customerUsername) {
		searchCustomer(() -> enterCustomerUsername(customerUsername));
	}

	/**
	 * Search for customer by email with Store filter.
	 *
	 * @param customerEmail   the customer email.
	 * @param store           Store associated with the customer
	 */
	@When("^I search for customer by store filter (.+) and email (.+)$")
	public void searchCustomerEmailAndStore(final String store, final String customerEmail) {
		fillSearchForm(() -> customerServiceNavigation.enterEmail(customerEmail));
		customerServiceNavigation.selectStore(store);
		clickCustomerSearch();
	}

	/**
	 * Search for customer by First name.
	 *
	 * @param firstName a customer First name.
	 */
	@When("^I search for customer by first name (.+)$")
	public void searchCustomerFirstName(final String firstName) {
		searchCustomer(() -> customerServiceNavigation.enterFirstName(firstName));
	}

	/**
	 * Search for customer by Last name.
	 *
	 * @param lastName a customer Last name.
	 */
	@When("^I search for customer by last name (.+)$")
	public void searchCustomerLastName(final String lastName) {
		searchCustomer(() -> customerServiceNavigation.enterLastName(lastName));
	}

	/**
	 * Search for customer by Zip Code.
	 *
	 * @param zipCode a customer Zip Code.
	 */
	@When("^I search for customer by zip code (.+)$")
	public void searchCustomerZipCode(final String zipCode) {
		searchCustomer(() -> customerServiceNavigation.enterPostalCode(zipCode));
	}

	/**
	 * Verify Customer exists.
	 *
	 * @param expectedCustomerSharedId the expected customer sharedId.
	 */
	@Then("^I should see customer with shared ID (.+) in result list$")
	public void verifyCustomerExists(final String expectedCustomerSharedId) {
		verifyCustomerSearchResult(expectedCustomerSharedId, CustomerSearchResultsPane.SHARED_ID_COLUMN);
	}

	/**
	 * Checks if there is entry with provided email in a search result table.
	 *
	 * @param expectedCustomerEmail the expected customer email.
	 */
	@Then("^I should see customer with email (.+) in result list$")
	public void verifyCustomerEmailInList(final String expectedCustomerEmail) {
		verifyCustomerSearchResult(expectedCustomerEmail, CustomerSearchResultsPane.EMAIL_ADDRESS_COLUMN);
	}

	/**
	 * Checks if there is entry with provided username in a search result table.
	 *
	 * @param expectedCustomerUsername the expected customer email.
	 */
	@Then("^I should see customer with username (.+) in result list$")
	public void verifyCustomerUsernameInList(final String expectedCustomerUsername) {
		verifyCustomerSearchResult(expectedCustomerUsername, CustomerSearchResultsPane.USERNAME_COLUMN);
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
	 * @param expectedEmailAddress expected email address for verification
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
	 * @param customerSharedID the customer shared id.
	 */
	@When("^I search and open customer editor for shared ID (.+)$")
	public void openCustomerEditor(final String customerSharedID) {
		activityToolbar.clickCustomerServiceButton();
		customerServiceNavigation.clickCustomersTab();
		enterCustomerSharedID(customerSharedID);
		clickCustomerSearch();
		customerEditor = customerSearchResultsPane.selectAndOpenCustomerEditor(customerSharedID);
	}

	/**
	 * Search and open Customer editor.
	 *
	 * @param customerEmail the customer email.
	 */
	@When("^I search and open customer editor for customer by email (.+)$")
	public void searchCustomerByEmail(final String customerEmail) {
		activityToolbar.clickCustomerServiceButton();
		customerServiceNavigation.clickCustomersTab();
		enterCustomerEmail(customerEmail);
		clickCustomerSearch();
		customerEditor = customerSearchResultsPane.selectAndOpenCustomerEditor(customerEmail);
	}

	/**
	 * Search and open Customer editor.
	 *
	 * @param customerUsername the customer username.
	 */
	@When("^I search and open customer editor for username (.+)$")
	public void searchCustomerByUsername(final String customerUsername) {
		activityToolbar.clickCustomerServiceButton();
		customerServiceNavigation.clickCustomersTab();
		enterCustomerUsername(customerUsername);
		clickCustomerSearch();
		customerEditor = customerSearchResultsPane.selectAndOpenCustomerEditor(customerUsername);
	}

	/**
	 * Search and open Account editor.
	 *
	 * @param accountSharedID account shared id.
	 */
	@When("^I search and open account editor for shared ID (.+)$")
	public void openAccountEditor(final String accountSharedID) {
		activityToolbar.clickCustomerServiceButton();
		customerServiceNavigation.clickAccountsTab();
		customerServiceNavigation.enterAccountSharedID(accountSharedID);
		AccountSearchResultPane accountSearchResultPane = customerServiceNavigation.clickAccountSearch();
		customerEditor = accountSearchResultPane.selectAndOpenAccountEditor(accountSharedID);
	}

	/**
	 * Search for customer by phone number.
	 *
	 * @param phoneNumber phone number.
	 */
	@When("^I search for customer by phone number (.+)$")
	public void searchCustomerByPhone(final String phoneNumber) {
		searchCustomer(() -> customerServiceNavigation.enterPhoneNumber(phoneNumber));
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
	 * Add new child account.
	 */
	@When("^I add new child$")
	public void selectCustomerEditorTab() throws InterruptedException {
		customerEditor.clickButton(CHILD_ACCOUNT_BTN_CSS, "Open Account Profile...");
		sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
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

	private void enterCustomerSharedID(final String customerSharedID) {
		customerServiceNavigation.enterSharedID(customerSharedID);
	}

	private void enterCustomerEmail(final String customerEmail) {
		customerServiceNavigation.enterEmail(customerEmail);
	}

	private void enterCustomerUsername(final String customerUsername) {
		customerServiceNavigation.enterUsername(customerUsername);
	}

	private void enterPhoneNumber(final String phoneNumber) {
		customerServiceNavigation.enterPhoneNumber(phoneNumber);
	}

	private void clickCustomerSearch() {
		customerSearchResultsPane = customerServiceNavigation.clickCustomerSearch();
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
	 * Select newly created Data Policy to Delete Data points.
	 */
	@When("^I Delete Data Points for recent Data Policy$")
	public void deleteDataPointsInNewlyCreatedDataPolicy() {
		customerEditor.verifyDataPolicyExists(DataPolicyNameHolder.getName());
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
	 * Verify newly created Data Policy is present in the table.
	 */
	@Then("^I should see newly Disabled Data Policy$")
	public void verifyNewlyCreatedDataPolicyExists() {
		customerEditor.verifyDataPolicyExists(DataPolicyNameHolder.getName());
		resetDataPolicyState(DataPolicyNameHolder.getName());
	}

	/**
	 * Verify that the correct set of fields are displayed.
	 *
	 * @param sectionTitle   customer editor section title.
	 * @param expectedFields expected set of fields.
	 */
	@Then("^I should see following fields in the (.+) section:$")
	public void verifyFieldsInGivenSection(final String sectionTitle, final List<String> expectedFields) {
		final List<String> fieldLabels = driver.findElements(By.cssSelector(String.format(CUSTOMER_EDITOR_FIELDS_CSS, sectionTitle.split(" ")[0])))
				.stream().map(WebElement::getText).collect(Collectors.toList());
		fieldLabels.remove(sectionTitle);

		verifyFieldsMatching(fieldLabels, expectedFields, sectionTitle);
	}

	/**
	 * Verify ordered parents of account.
	 *
	 * @param expectedParents expected set of parents.
	 */
	@Then("^I should see account with ordered parents:$")
	public void verifyParentsChain(final List<String> expectedParents) {
		final List<String> fieldLabels = driver.findElements(By.cssSelector(CHILD_ACCOUNT_HYPERLINK_CSS))
				.stream().map(WebElement::getText).collect(Collectors.toList());

		assertThat(fieldLabels.equals(expectedParents)).isTrue();
	}

	/**
	 * Verify account children.
	 */
	@Then("^Child Accounts table contains first level (.+) child")
	public void verifyFirstLevelAccountChild(final String expectedChild) {
		final List<String> children =
				driver.findElements(By.cssSelector("div[parent-widget-id='Child Accounts'] div[column-num='0']"))
						.stream()
						.map(WebElement::getText)
						.collect(Collectors.toList());

		assertThat(children.get(0))
				.as("Account shows not expected child.")
				.isEqualTo(expectedChild);
	}

	/**
	 * Open child.
	 */
	@Then("^I open (.+) child")
	public void openChild(final String expectedChild) {
		customerEditor.click(By.cssSelector(String.format(CHILD_IN_TABLE_CSS, expectedChild)));
		customerEditor.clickButton(OPEN_ACCOUNT_BUTTON_CSS, "Open Account Profile...");
	}

	/**
	 * Check direct parent.
	 */
	@Then("^I should see opened account with direct parent (.+) name")
	public void checkAccountDirectParentBusinessName(final String expectedParent) {
		final String parent = driver.findElements(By.cssSelector(CHILD_ACCOUNT_HYPERLINK_CSS))
				.stream()
				.map(WebElement::getText)
				.reduce((first, second) -> second)
				.orElse(null);

		assertThat(parent)
				.as("Direct parent is not matched.")
				.isEqualTo(expectedParent);
	}

	/**
	 * Verify that the correct set of tabs are displayed.
	 *
	 * @param expectedTabs expected set of tabs.
	 */
	@Then("^I should see following tabs:$")
	public void verifyTabs(final List<String> expectedTabs) {
		final List<String> tabLabels = driver.findElements(By.cssSelector(CUSTOMER_EDITOR_TABS_CSS))
				.stream().map(WebElement::getText).collect(Collectors.toList());
		verifyTabsMatching(tabLabels, expectedTabs);
	}

	/**
	 * Checks the number and composition of tabs.
	 *
	 * @param tabLabels    actual set of tabs.
	 * @param expectedTabs expected set of tabs.
	 */
	private void verifyTabsMatching(final List<String> tabLabels, final List<String> expectedTabs) {
		assertThat(tabLabels.size())
				.as("Customer editor doesn't contain expected amount of tabs")
				.isEqualTo(expectedTabs.size());
		for (int i = 0; i < tabLabels.size(); i++) {
			assertThat(tabLabels.get(i).equals(expectedTabs.get(i)))
					.as(String.format("Customer editor doesn't contain expected tab: '%s' doesn't match '%s'",
							tabLabels.get(i), expectedTabs.get(i)))
					.isTrue();
		}
	}

	/**
	 * Checks the number and composition of fields.
	 *
	 * @param fieldLabels    actual set of fields.
	 * @param expectedFields expected set of fields.
	 * @param sectionTitle   customer editor section title.
	 */
	private void verifyFieldsMatching(final List<String> fieldLabels, final List<String> expectedFields, final String sectionTitle) {
		assertThat(fieldLabels.size())
				.as(sectionTitle + " section doesn't contain expected amount of fields")
				.isEqualTo(expectedFields.size());
		for (int i = 0; i < fieldLabels.size(); i++) {
			assertThat(fieldLabels.get(i).contains(expectedFields.get(i)))
					.as(String.format("%s section doesn't contain expected field: '%s' doesn't contain '%s'",
							sectionTitle, fieldLabels.get(i), expectedFields.get(i)))
					.isTrue();
		}
	}

	/**
	 * Resets Data Policy State.
	 *
	 * @param dataPolicyName String.
	 */
	private void resetDataPolicyState(final String dataPolicyName) {
		DBConnector dbConnector = new DBConnector();
		dbConnector.executeUpdateQuery("UPDATE TDATAPOLICY SET STATE='1', END_DATE=NULL WHERE POLICY_NAME='" + dataPolicyName + "'");
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
		customerServiceNavigation.clearInputFieldsInCustomersTab();
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
	
	/**
	 * Add Associate.
	 *
	 * @param userEmail the user email.
	 */
	@When("^I add associate user (.+)$")
	public void addAssociateUser(final String userEmail) {
		customerEditor.verifyAssociateDoesNotExist(userEmail);
		addAccountAssociateDialog = customerEditor.clickAddAssociateButton();
		addAccountAssociateDialog.enterUserEmail(userEmail);
		addAccountAssociateDialog.clickSave();
	}
	
	@Then("^user with email (.+) is added to the Associates table$")
	public void verifyAssociateIsAdded(final String userEmail) {
		customerEditor.verifyAssociateExists(userEmail);
	}
	
	@When("^I delete associate user (.+)$")
	public void deleteAssociateUser(final String userEmail) {
		customerEditor.verifyAssociateExists(userEmail);
		DeleteAccountAssociateDialog deleteAccountAssociateDialog = customerEditor.clickDeleteAssociateButton();
		deleteAccountAssociateDialog.clickOK();
	}
	
	@Then("^user with email (.+) is removed from the Associates table$")
	public void verifyAssociateIsRemoved(final String userEmail) {
		customerEditor.verifyAssociateDoesNotExist(userEmail);
	}
	
	/**
	 * Unlike {@link #addAssociateUser()}, this method does not wait for the dialog to close, as we expect to verify an error message instead.
	 * @param userEmail the user email
	 */
	@When("^I add invalid associate user (.+)$")
	public void addInvalidAssociateUser(final String userEmail) {
		addAccountAssociateDialog = customerEditor.clickAddAssociateButton();
		addAccountAssociateDialog.enterUserEmail(userEmail);
		addAccountAssociateDialog.clickButton(AddAccountAssociateDialog.SAVE_BUTTON_CSS, "Save");
	}
	
	@Then("^the following error message is displayed: (.+)$")
	public void verifyError(final String errorMessage) {
		customerEditor.verifyErrorMessageDisplayed(errorMessage);
	}

}
