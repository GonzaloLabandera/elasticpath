package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.editor.CustomerEditor;

/**
 * Account search results pane.
 */
public class AccountSearchResultPane extends AbstractPageObject {

	private static final String SEARCH_RESULTS_TAB_WIDGET_ID = "Account Search Result";
	private static final String RESULTS_PANE_PARENT_CSS = "div[pane-location='center-pane-inner'][seeable='true'] ";
	private static final String RESULTS_LIST_TABLE_PARENT_CSS = RESULTS_PANE_PARENT_CSS + "div[widget-id='Account Search Result Table'] ";
	private static final String ACCOUNT_SEARCH_RESULT_PARENT_CSS = "div[widget-id='" + SEARCH_RESULTS_TAB_WIDGET_ID
			+ " Table'][widget-type='Table'] ";
	private static final String ACCOUNT_SEARCH_RESULT_ROW_CSS = ACCOUNT_SEARCH_RESULT_PARENT_CSS + "div[parent-widget-id='"
			+ SEARCH_RESULTS_TAB_WIDGET_ID + " Table'] ";
	private static final String ACCOUNT_SEARCH_RESULT_CELL_CSS = ACCOUNT_SEARCH_RESULT_ROW_CSS + "div[column-id='%s']";
	private static final String ACCOUNT_SEARCH_RESULT_CELL_PARTIAL_MATCH_CSS = ACCOUNT_SEARCH_RESULT_ROW_CSS
			+ "div[column-id*='%s'][column-num='%s']";
	private static final String CUSTOMER_EDITOR_TOOLTIP_CSS = "div[widget-id*='Customer #']";
	private static final String CUSTOMER_EDITOR_TOOLTIP_BUSINESS_NAME_CSS = "div[widget-id*='Customer #%s']";

	public static final String SHARED_ID_COLUMN = "Shared ID";
	public static final String BUSINESS_NAME_COLUMN = "Business Name";
	public static final String BUSINESS_NUMBER_COLUMN = "Business Number";
	public static final int BILLING_ADDRESS_COLUMN_INDEX = 4;
	private static final String OPENED_ACCOUNT = "div[automation-id="
			+ "'com.elasticpath.cmclient.fulfillment.FulfillmentMessages.CustomerDetails_Tooltip']> div[style*='close.gif']";

	private static final String DELETE_ACCOUNT_BUTTON_CSS = "div[widget-id='Delete Account'][appearance-id='toolbar-button'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives his page.
	 */
	public AccountSearchResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Returns parent table css.
	 *
	 * @return RESULTS_LIST_TABLE_PARENT_CSS the parent table css
	 */
	public static String getResultsListTableParentCssParentCss() {
		return RESULTS_LIST_TABLE_PARENT_CSS.trim();
	}


	/**
	 * Verifies if a search result table empty.
	 *
	 * @return true if the account was not found in a search result list
	 */
	public boolean isSearchResultTableEmpty() {
		return getSearchResultTableRowsAmount() == 0;
	}

	/**
	 * Returns search result table rows amount.
	 *
	 * @return search result table rows amount
	 */
	public int getSearchResultTableRowsAmount() {
		return getSearchResultTableElements(ACCOUNT_SEARCH_RESULT_ROW_CSS).size();
	}

	/**
	 * Returns collection of Search result table elements using provided Css selector
	 *
	 * @param elementsCss Css selector used for elements collection
	 * @return collection of Search result table elements using provided Css selector
	 */
	private List<WebElement> getSearchResultTableElements(final String elementsCss) {
		getWaitDriver().waitForElementToBeInteractable(ACCOUNT_SEARCH_RESULT_PARENT_CSS);
		setWebDriverImplicitWait(1);
		List<WebElement> tableElements = getDriver().findElements(By.cssSelector(elementsCss));
		setWebDriverImplicitWaitToDefault();
		return tableElements;
	}

	/**
	 * Verifies if provided account value is in the first row of the search result table using full match equality for expected value in the table.
	 *
	 * @param expectedAccountValue account value to find in a list
	 * @param columnName           the column name
	 * @return true if the account was found in a search result list
	 */
	public boolean isAccountInListFullMatch(final String expectedAccountValue, final String columnName) {
		return selectItemInCenterPane(ACCOUNT_SEARCH_RESULT_PARENT_CSS, ACCOUNT_SEARCH_RESULT_CELL_CSS, expectedAccountValue, columnName);
	}

	/**
	 * Verifies if provided account value is in the search result table using partial match equality for expected value in the
	 * table.
	 *
	 * @param expectedAccountValue account value to find in a list
	 * @param columnIndex          an index of the table column for verification
	 * @return true if the account was found in a search result list
	 */
	public boolean isAccountInListPartialMatch(final String expectedAccountValue, final int columnIndex) {
		getWaitDriver().waitForElementToBeInteractable(ACCOUNT_SEARCH_RESULT_PARENT_CSS);
		return isElementPresent(By.cssSelector(String.format(ACCOUNT_SEARCH_RESULT_CELL_PARTIAL_MATCH_CSS, expectedAccountValue, columnIndex)));
	}

	public void closeAccountSearchResultsPane() {
		closePane(SEARCH_RESULTS_TAB_WIDGET_ID);
	}

	public void closeFoundAccount() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(OPENED_ACCOUNT)));
		waitTillElementDisappears(By.cssSelector(OPENED_ACCOUNT));
	}

	/**
	 * Select Account and open editor.
	 *
	 * @param accountSharedID the account shared id.
	 * @return the customer editor.
	 */
	public CustomerEditor selectAndOpenAccountEditor(final String accountSharedID) {
		verifyEntryInList(accountSharedID, SHARED_ID_COLUMN);
		doubleClick(getSelectedElement(), CUSTOMER_EDITOR_TOOLTIP_CSS);
		return new CustomerEditor(getDriver());
	}

	/**
	 * Select Account and open editor by business name.
	 *
	 * @param accountBusinessName the account business name.
	 * @return the customer editor.
	 */
	public CustomerEditor selectAndOpenAccountEditorByBusinessName(final String accountBusinessName) {
		verifyEntryInList(accountBusinessName, BUSINESS_NAME_COLUMN);
		doubleClick(getSelectedElement(), CUSTOMER_EDITOR_TOOLTIP_CSS);
		return new CustomerEditor(getDriver());
	}

	/**
	 * Checks if there is entry with provided value in a search result table using full match for expected table value.
	 *
	 * @param expectedValue an expected value.
	 * @param columnName    column name associated with expected value
	 */
	public void verifyEntryInList(final String expectedValue, final String columnName) {
		assertThat(isAccountInListFullMatch(expectedValue, columnName))
				.as("Unable to find customer with provided parameter:" + expectedValue)
				.isTrue();
	}

	public ConfirmDialog clickDeleteServiceLevelButton() {
		clickButton(DELETE_ACCOUNT_BUTTON_CSS, "Delete Account");
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Checks if account tab is closed.
	 * @param businessName business name of the account tab
	 */
	public void verifyAccountTabNotExists(final String businessName) {
		assertThat(isElementPresent(By.cssSelector(String.format(CUSTOMER_EDITOR_TOOLTIP_BUSINESS_NAME_CSS, businessName))))
				.as("There are opened account tabs")
				.isFalse();
	}
}
