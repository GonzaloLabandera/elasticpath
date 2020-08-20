package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.editor.OrderEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.CustomerServiceNavigation;
import com.elasticpath.selenium.util.Constants;

/**
 * Order search results pane.
 */
public class OrderSearchResultPane extends AbstractPageObject {
	private static final String RESULTS_PANE_PARENT_CSS = "div[pane-location='center-pane-inner'][seeable='true'] ";
	private static final String RESULTS_LIST_TABLE_PARENT_CSS = RESULTS_PANE_PARENT_CSS + "div[widget-id='Order Search Result Table'] ";
	private static final String RESULTS_COLUMN_CSS = RESULTS_LIST_TABLE_PARENT_CSS + "div[column-id='%s']";
	private static final String ORDER_STATUS_VALUE_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.OrderStatus_%s']";
	private static final String SEARCH_RESULTS_PANE_WIDGET_ID = "Order Search Results";
	private final CustomerServiceNavigation customerServiceNavigation;

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives his page.
	 */
	public OrderSearchResultPane(final WebDriver driver) {
		super(driver);
		customerServiceNavigation = new CustomerServiceNavigation(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Verifies the column text in Order Search Results tab and selects the row.
	 *
	 * @param columnValue the column value.
	 * @param columnName  the column number.
	 */
	public void verifyOrderColumnValueAndSelectRow(final String columnValue, final String columnName) {
		boolean isOrderInList = isOrderInList(columnValue, columnName);

		int index = 0;
		while (!isOrderInList && index < Constants.UUID_END_INDEX) {
			sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			customerServiceNavigation.clickOrderSearch();
			isOrderInList = isOrderInList(columnValue, columnName);
			index++;
		}

		assertThat(isOrderInList)
				.as("Order " + columnValue + " does not exist in search result - " + columnName)
				.isTrue();
	}

	/**
	 * Verifies if order exists.
	 *
	 * @param columnValue the column value.
	 * @param columnName  the column number.
	 * @return true if order is in the last, false otherwise
	 */
	public boolean isOrderInList(final String columnValue, final String columnName) {
		setWebDriverImplicitWait(1);
		boolean isOrderInList = selectItemInCenterPane(RESULTS_LIST_TABLE_PARENT_CSS, String.format(RESULTS_COLUMN_CSS, columnValue), columnValue,
				columnName);

		setWebDriverImplicitWaitToDefault();
		return isOrderInList;
	}

	/**
	 * Select order and open editor.
	 *
	 * @param columnValue the column value.
	 * @param columnName  the column number.
	 * @return the order editor.
	 */
	public OrderEditor selectOrderAndOpenOrderEditor(final String columnValue, final String columnName) {
		verifyOrderColumnValueAndSelectRow(columnValue, columnName);
		doubleClick(getSelectedElement(), OrderEditor.EDITOR_PANE_PARENT_CSS);
		return new OrderEditor(getDriver());
	}

	/**
	 * Closes Order Search Results pane.
	 */
	public void closeOrderSearchResultsPane() {
		closePane(SEARCH_RESULTS_PANE_WIDGET_ID);
	}

	/**
	 * Close Order Search Results pane if it's open.
	 */
	public void closeOrderSearchResultsPaneIfOpen() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		if (isElementPresent(By.cssSelector("div[widget-id='" + SEARCH_RESULTS_PANE_WIDGET_ID + "']"))) {
			setWebDriverImplicitWaitToDefault();
			closeOrderSearchResultsPane();
		}
		setWebDriverImplicitWaitToDefault();
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
	 * Verify given order status returns orders in result.
	 *
	 * @param expectedStatus String
	 */
	public void verifyOrderStatusExistInResult(final String expectedStatus) {
		String trimedExpectedStatus = expectedStatus.replaceAll("\\s", "");
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		assertThat(isElementPresent(By.cssSelector(String.format(ORDER_STATUS_VALUE_CSS, trimedExpectedStatus))))
				.as("Unable to find the order with the order status - " + expectedStatus)
				.isTrue();
		setWebDriverImplicitWaitToDefault();
	}
}
