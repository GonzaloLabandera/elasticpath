package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.editor.CustomerEditor;

/**
 * Customer Search Results Pane.
 */
public class CustomerSearchResultsPane extends AbstractPageObject {

	private static final String SEARCH_RESULTS_TAB_WIDGET_ID = "Customer Search Result";
	private static final String CUSTOMER_SEARCH_RESULT_PARENT_CSS = "div[widget-id='" + SEARCH_RESULTS_TAB_WIDGET_ID
			+ " Table'][widget-type='Table'] ";
	private static final String CUSTOMER_SEARCH_RESULT_ROW_CSS = CUSTOMER_SEARCH_RESULT_PARENT_CSS + "div[parent-widget-id='"
			+ SEARCH_RESULTS_TAB_WIDGET_ID + " Table'] ";
	private static final String CUSTOMER_SEARCH_RESULT_CELL_CSS = CUSTOMER_SEARCH_RESULT_ROW_CSS + "div[column-id='%s']";
	private static final String CUSTOMER_SEARCH_RESULT_CELL_PARTIAL_MATCH_CSS = CUSTOMER_SEARCH_RESULT_ROW_CSS
			+ "div[column-id*='%s'][column-num='%s']";
	private static final String CUSTOMER_EDITOR_TOOLTIP_CSS = "div[widget-id*='Customer #']";
	public static final String EMAIL_ADDRESS_COLUMN = "Email Address";
	public static final String FIRST_NAME_COLUMN = "First Name";
	public static final String LAST_NAME_COLUMN = "Last Name";
	public static final String PHONE_NUMBER_COLUMN = "Telephone #";
	public static final int EMAIL_ADDRESS_COLUMN_INDEX = 5;
	public static final int BILLING_ADDRESS_COLUMN_INDEX = 6;

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CustomerSearchResultsPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Checks if there is entry with provided value in a search result table using full match for expected table value.
	 *
	 * @param expectedValue an expected value.
	 * @param columnName    column name associated with expected value
	 */
	public void verifyEntryInList(final String expectedValue, final String columnName) {
		assertThat(isCustomerInListFullMatch(expectedValue, columnName))
				.as("Unable to find customer with provided parameter:" + expectedValue)
				.isTrue();
	}

	/**
	 * Verifies if provided customer value is in the first row of the search result table using full match equality for expected value in the table.
	 *
	 * @param expectedCustomerValue customer value to find in a list
	 * @param columnName            the column name
	 * @return true if the customer was found in a search result list
	 */
	public boolean isCustomerInListFullMatch(final String expectedCustomerValue, final String columnName) {
		return selectItemInCenterPane(CUSTOMER_SEARCH_RESULT_PARENT_CSS, CUSTOMER_SEARCH_RESULT_CELL_CSS, expectedCustomerValue, columnName);
	}

	/**
	 * Verifies if provided customer value is in the search result table using partial match equality for expected value in the
	 * table.
	 *
	 * @param expectedCustomerValue customer value to find in a list
	 * @param columnIndex           an index of the table column for verification
	 * @return true if the customer was found in a search result list
	 */
	public boolean isCustomerInListPartialMatch(final String expectedCustomerValue, final int columnIndex) {
		getWaitDriver().waitForElementToBeInteractable(CUSTOMER_SEARCH_RESULT_PARENT_CSS);
		return isElementPresent(By.cssSelector(String.format(CUSTOMER_SEARCH_RESULT_CELL_PARTIAL_MATCH_CSS, expectedCustomerValue, columnIndex)));
	}

	/**
	 * Verifies if a search result table empty.
	 *
	 * @return true if the customer was not found in a search result list
	 */
	public boolean isSearchResultTableEmpty() {
		if (getSearchResultTableRowsAmount() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Returns search result table rows amount.
	 *
	 * @return search result table rows amount
	 */
	public int getSearchResultTableRowsAmount() {
		return getSearchResultTableElements(CUSTOMER_SEARCH_RESULT_ROW_CSS).size();
	}

	/**
	 * Select Customer and open editor.
	 *
	 * @param columnValue the column value.
	 * @return the customer editor.
	 */
	public CustomerEditor selectAndOpenCustomerEditor(final String columnValue) {
		verifyEntryInList(columnValue, EMAIL_ADDRESS_COLUMN);
		doubleClick(getSelectedElement(), CUSTOMER_EDITOR_TOOLTIP_CSS);
		return new CustomerEditor(getDriver());
	}

	/**
	 * Returns parent table css.
	 *
	 * @return CUSTOMER_SEARCH_RESULT_PARENT_CSS the parent table css
	 */
	public static String getCustomerSearchResultParentCss() {
		return CUSTOMER_SEARCH_RESULT_PARENT_CSS.trim();
	}

	/**
	 * Closes Customer Search Results tab.
	 */
	public void closeCustomerSearchResultsPane() {
		closePane(SEARCH_RESULTS_TAB_WIDGET_ID);
	}

	/**
	 * Verifies if provided customer value is in all the search result table entries using partial match equality for expected value in the table.
	 *
	 * @param expectedCustomerValue customer value to find in a list
	 * @param columnIndex           an index of the table column for verification
	 */
	public void isCustomerValueInAllListEntries(final String expectedCustomerValue, final int columnIndex) {
		int rowsWithExpectedValue = getSearchResultTableElements(
				String.format(CUSTOMER_SEARCH_RESULT_CELL_PARTIAL_MATCH_CSS, expectedCustomerValue, columnIndex)).size();
		assertThat(rowsWithExpectedValue)
				.as("Unexpected amount of search result entries which satisfy search criteria")
				.isEqualTo(getSearchResultTableRowsAmount());
	}

	/**
	 * Returns collection of Search result table elements using provided Css selector
	 *
	 * @param elementsCss Css selector used for elements collection
	 * @return collection of Search result table elements using provided Css selector
	 */
	private List<WebElement> getSearchResultTableElements(final String elementsCss) {
		getWaitDriver().waitForElementToBeInteractable(CUSTOMER_SEARCH_RESULT_PARENT_CSS);
		setWebDriverImplicitWait(1);
		List<WebElement> tableElements = getDriver().findElements(By.cssSelector(elementsCss));
		setWebDriverImplicitWaitToDefault();
		return tableElements;
	}
}
