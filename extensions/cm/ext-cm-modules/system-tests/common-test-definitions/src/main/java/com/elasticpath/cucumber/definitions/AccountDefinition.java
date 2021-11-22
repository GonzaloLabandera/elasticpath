package com.elasticpath.cucumber.definitions;

import static com.elasticpath.selenium.framework.util.SeleniumDriverSetup.getDriver;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.navigations.CustomerServiceNavigation;
import com.elasticpath.selenium.resultspane.AccountSearchResultPane;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.CreateAccountWizard;

/**
 * Account step definition.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class AccountDefinition {

	private static final String CHILD_ACCOUNTS_TABLE_CSS = "div[parent-widget-id='Child Accounts'] div[column-num='0']";
	private final CustomerServiceNavigation customerServiceNavigation;
	private AccountSearchResultPane accountSearchResultsPane;
	private CreateAccountWizard createAccountWizard;
	private String businessName;
	private final WebDriver driver;

	private static final String ADD_ACCOUNT_BUTTON_CSS = "div[widget-id='Add Account'][appearance-id='toolbar-button'][seeable='true']";

	/**
	 * Constructor.
	 */
	public AccountDefinition() {
		customerServiceNavigation = new CustomerServiceNavigation(getDriver());
		createAccountWizard = new CreateAccountWizard(getDriver());
		driver = getDriver();
	}

	/**
	 * Clears input fields in the account search tab.
	 */
	@When("^I open account search tab$")
	public void openAccountsSearchTab() {
		customerServiceNavigation.clickAccountsTab();
		customerServiceNavigation.clearInputFieldsInAccountsTab();
	}

	/**
	 * Search for account by shared id.
	 *
	 * @param accountSharedID the account shared id.
	 */
	@When("^I search for account by shared ID (.+)$")
	public void searchAccountSharedId(final String accountSharedID) {
		searchAccount(() -> customerServiceNavigation.enterAccountSharedID(accountSharedID));
	}

	/**
	 * Search for account by business name.
	 *
	 * @param businessName the account business name.
	 */
	@When("^I search for account by business name (.+)$")
	public void searchAccountBusinessName(final String businessName) {
		searchAccount(() -> customerServiceNavigation.enterBusinessName(businessName));
	}

	/**
	 * Search for account by business number.
	 *
	 * @param businessNumber the account business number.
	 */
	@When("^I search for account by business number (.+)$")
	public void searchAccountBusinessNumber(final String businessNumber) {
		searchAccount(() -> customerServiceNavigation.enterBusinessNumber(businessNumber));
	}

	/**
	 * Search for account by zip code.
	 *
	 * @param zipCode the account zip code.
	 */
	@When("^I search for account by zip code (.+)$")
	public void searchAccountZipCode(final String zipCode) {
		searchAccount(() -> customerServiceNavigation.enterZipCode(zipCode));
	}

	/**
	 * Opens create account wizard.
	 */
	@When("^I open create account wizard$")
	public void openCreateAccountWizard() {
		searchAccountBusinessName("Some name");
		accountSearchResultsPane.clickButton(ADD_ACCOUNT_BUTTON_CSS, "Add Account");
	}

	/**
	 * Fills in required fields.
	 */
	@When("^I fill in the required fields$")
	public void fillRequiredFields() {
		createAccountWizard = new CreateAccountWizard(getDriver());
		businessName = "Some name " + UUID.randomUUID();
		createAccountWizard.fillRequiredFields(businessName);
	}

	/**
	 * Fills in decimal attribute.
	 */
	@When("^I fill in the decimal attribute (.+)$")
	public void fillDecimalAttribute(final String attributeValue) {
		createAccountWizard.fillDecimalAttribute(attributeValue);
	}

	/**
	 * Saves account.
	 */
	@When("^I save account$")
	public void saveAccount() {
		createAccountWizard.saveAccount();
	}

	/**
	 * Verify first level account child.
	 */
	@Then("^Child Accounts table does not contain (.+) child")
	public void verifySecondLevelAccountChild(final String expectedChild) {
		final List<String> children = extractChildren();

		assertThat(children.isEmpty() || !children.get(0).equals(expectedChild))
				.as("Account shows not expected child.")
				.isTrue();
	}

	/**
	 * Verify new child.
	 */
	@Then("^Child Accounts table contains created child")
	public void verifyNewChild() {
		final List<String> children = extractChildren();

		assertThat(children)
				.as("Account does not show expected child.")
				.contains(businessName);
	}

	/**
	 * Closes account search results tab.
	 */
	@Then("^I close account search results tab$")
	public void closeSearchResultsTab() {
		clickAccountSearch();
		accountSearchResultsPane.closeAccountSearchResultsPane();
	}

	/**
	 * Closes found account.
	 */
	@Then("^I close found customer$")
	public void closeFoundAccount() {
		accountSearchResultsPane.closeFoundAccount();
	}

	/**
	 * Checks if there is at least one entry in a search result table.
	 */
	@Then("^I can click search button and non-empty search results table appears on the page$")
	public void verifyAccountEntryInList() {
		assertThat(clickAccountSearch())
				.as("The search result doesn't contain any entry")
				.isFalse();
	}

	/**
	 * Checks if there is entry with provided shared ID in a search result table.
	 *
	 * @param expectedSharedId an expected shared ID.
	 */
	@Then("^I should see account with shared ID (.+) in result list$")
	public void verifySharedIdEntryInList(final String expectedSharedId) {
		verifyAccountSearchResult(expectedSharedId, AccountSearchResultPane.SHARED_ID_COLUMN);
	}

	/**
	 * Checks if there is entry with provided business name in a search result table.
	 *
	 * @param expectedBusinessName an expected business name.
	 */
	@Then("^I should see account with business name (.+) in result list$")
	public void verifyBusinessNameEntryInList(final String expectedBusinessName) {
		verifyAccountSearchResult(expectedBusinessName, AccountSearchResultPane.BUSINESS_NAME_COLUMN);
	}

	/**
	 * Checks if there is entry with provided business number in a search result table.
	 *
	 * @param expectedBusinessNumber expected business number.
	 */
	@Then("^I should see account with business number (.+) in result list$")
	public void verifyBusinessNumberEntryInList(final String expectedBusinessNumber) {
		verifyAccountSearchResult(expectedBusinessNumber, AccountSearchResultPane.BUSINESS_NUMBER_COLUMN);
	}

	/**
	 * Checks if there is entry with provided zip code in a search result table.
	 *
	 * @param expectedZipCode expected zip code.
	 */
	@Then("^I should see account with zip code (.+) in result list$")
	public void verifyZipCodeEntryInList(final String expectedZipCode) {
		verifyAccountSearchResultPartialMatch(expectedZipCode, AccountSearchResultPane.BILLING_ADDRESS_COLUMN_INDEX);
	}

	/**
	 * Checks if search result table is empty.
	 */
	@Then("^I should see empty account search results table$")
	public void verifyEmptyResultTable() {
		assertThat(accountSearchResultsPane.isSearchResultTableEmpty())
				.as("Account search result table should be empty, but contains at least one row")
				.isTrue();
	}

	/**
	 * Verifies that account was saved.
	 */
	@Then("^new Account is created$")
	public void verifyAccountCreated() {
		accountSearchResultsPane.closeAccountSearchResultsPane();
		searchAccountBusinessName(businessName);
		verifyBusinessNameEntryInList(businessName);
	}

	@When("^I delete the newly created account$")
	public void deleteNewAccount() {
		accountSearchResultsPane.isAccountInListFullMatch(businessName, businessName);
		ConfirmDialog confirmDialog = accountSearchResultsPane.clickDeleteServiceLevelButton();
		confirmDialog.clickOKButton("FulfillmentMessages.ConfirmDeleteAccount");
	}

	@Then("^I verify account is deleted$")
	public void verifyAccountIsDeleted() {
		assertThat(accountSearchResultsPane.isAccountInListFullMatch(businessName, businessName))
				.as("Delete failed, Account is still in the list - " + businessName)
				.isFalse();
	}

	@Then("^I open the newly created account$")
	public void openAccountDetailsTab() {
		accountSearchResultsPane.selectAndOpenAccountEditorByBusinessName(businessName);
	}


	@Then("^I verify the newly created account tab is closed$")
	public void iVerifyAccountTabIsClosed() {
		accountSearchResultsPane.verifyAccountTabNotExists(businessName);
	}

	/**
	 * Verify a account is in a result list in a Search Results Table using full match equality for expected value in the table.
	 *
	 * @param verificationCriteria account value for verification
	 * @param columnName           column name of the Search result table associated with verification criteria
	 */
	private void verifyAccountSearchResult(final String verificationCriteria, final String columnName) {
		int index = 0;
		while (!accountSearchResultsPane.isAccountInListFullMatch(verificationCriteria, columnName) && index < Constants.RETRY_COUNTER_40) {
			closeSearchResultsTab();
			accountSearchResultsPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			clickAccountSearch();
			index++;
		}
		assertThat(accountSearchResultsPane.isAccountInListFullMatch(verificationCriteria, columnName))
				.as("The search result doesn't contain expected entry")
				.isTrue();
	}

	/**
	 * Verify a account is in a result list in a Search Results Table using partial match equality for expected value in the table.
	 *
	 * @param verificationCriteria account value for verification
	 * @param columnIndex          an index of the table column for verification
	 */
	private void verifyAccountSearchResultPartialMatch(final String verificationCriteria, final int columnIndex) {
		int index = 0;
		while (!accountSearchResultsPane.isAccountInListPartialMatch(verificationCriteria, columnIndex) && index < Constants.RETRY_COUNTER_5) {
			accountSearchResultsPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			clickAccountSearch();
			index++;
		}
		assertThat(accountSearchResultsPane.isAccountInListPartialMatch(verificationCriteria, columnIndex))
				.as("The search result doesn't contain expected entry")
				.isTrue();
	}


	/**
	 * Runs a search for account with full or partial search value and verifies that result table appears.
	 *
	 * @param runnable function which fills search field.
	 */
	private void searchAccount(final Runnable runnable) {
		fillSearchForm(runnable);
		clickAccountSearch();
	}

	/**
	 * Navigates to search form and fills it
	 *
	 * @param runnable function which fills search field.
	 */
	private void fillSearchForm(final Runnable runnable) {
		customerServiceNavigation.clickAccountsTab();
		customerServiceNavigation.clearInputFieldsInAccountsTab();
		runnable.run();
	}

	private boolean clickAccountSearch() {
		accountSearchResultsPane = customerServiceNavigation.clickAccountSearch();
		//this call ensures that the search results table appears on the page
		return accountSearchResultsPane.isSearchResultTableEmpty();
	}

	private List<String> extractChildren() {
		return driver.findElements(By.cssSelector(CHILD_ACCOUNTS_TABLE_CSS))
				.stream()
				.map(WebElement::getText)
				.collect(Collectors.toList());
	}
}
