package com.elasticpath.selenium.navigations;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.resultspane.AccountSearchResultPane;
import com.elasticpath.selenium.resultspane.CustomerSearchResultsPane;
import com.elasticpath.selenium.resultspane.OrderSearchResultPane;
import com.elasticpath.selenium.util.Constants;

/**
 * Customer Service Page.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class CustomerServiceNavigation extends AbstractNavigation {

	private static final String ACTIVE_LEFT_PANE = "div[pane-location='left-pane-inner'] div[active-editor='true'] ";
	private static final String SEARCH_VIEW_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String APPEARANCE_ID_CSS = "div[appearance-id='ctab-item']";
	private static final String CUSTOMERS_TAB_CSS = APPEARANCE_ID_CSS + "[widget-id='Users']";
	private static final String ORDERS_TAB_CSS = APPEARANCE_ID_CSS + "[widget-id='Orders']";
	private static final String ACCOUNTS_TAB_CSS = APPEARANCE_ID_CSS + "[widget-id='Accounts']";
	private static final String ORDER_NUMBER_LABEL_CSS = SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_OrderNumber']";
	private static final String SHAREDID_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_SharedId'] > input";
	private static final String EMAIL_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_Email'] > input";
	private static final String USERNAME_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_Username'] > input";
	private static final String PHONE_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_PhoneNumber'] > input";
	private static final String ORDER_NUMBER_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_OrderNumber'] > input";
	private static final String SEARCH_BUTTON_CSS = "div[pane-location='left-pane-inner'] div[widget-id='Search'][seeable='true']";
	private static final String CLEAR_BUTTON_CSS = "div[pane-location='left-pane-inner'] "
			+ SEARCH_VIEW_PARENT_CSS + ".SearchView_ClearButton'][seeable='true']";
	private static final String FIRST_NAME_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".CustomerDetails_FirstNameLabel'] > input";
	private static final String LAST_NAME_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".CustomerDetails_LastNameLabel'] > input";
	private static final String ACCOUNT_SHAREDID_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".AccountDetails_SharedId'] > input";
	private static final String BUSINESS_NAME_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".AccountDetails_BusinessName'] > input";
	private static final String BUSINESS_NUMBER_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".AccountDetails_BusinessNumber'] > input";
	private static final String ZIP_CODE_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".AccountDetails_ZipPostalCode'] > input";
	private static final String POSTAL_CODE_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_PostalCode'] > input";
	private static final String STORE_COMBO_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_Filter_Stores'][widget-type='CCombo']";
	private static final String ORDER_STATUS_COMBO_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_Filter_OrderStatus'][widget-type='CCombo']";
	private static final String ORDER_SHIPMENT_STATUS_COMBO_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_Filter_ShipmentStatus'][widget-type='CCombo']";
	private static final String SKU_CODE_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_ContainsSku'] > input";
	private static final String FROM_DATE_INPUT_CSS = ACTIVE_LEFT_PANE
			+ "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.SampleDateTime'] > input";
	private static final String TO_DATE_INPUT_CSS = ACTIVE_LEFT_PANE + SEARCH_VIEW_PARENT_CSS
			+ ".SearchView_ToDate'] > div[widget-type='Text'] > input";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CustomerServiceNavigation(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verifies if order number field exists.
	 */
	public void verifyOrderNumberFieldExist() {
		getWaitDriver().waitForElementToBeInteractable(ORDER_NUMBER_INPUT_CSS);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_NUMBER_INPUT_CSS));
	}

	/**
	 * Enters order number.
	 *
	 * @param orderNum String
	 */
	public void enterOrderNumber(final String orderNum) {
		clearAndType(ORDER_NUMBER_INPUT_CSS, orderNum);
	}

	/**
	 * Clicks on search for order.
	 *
	 * @return OrderSearchResultPane
	 */
	public OrderSearchResultPane clickOrderSearch() {
		clickButtonAndWaitForPaneToOpen(SEARCH_BUTTON_CSS, "Search", OrderSearchResultPane.getResultsListTableParentCssParentCss());
		return new OrderSearchResultPane(getDriver());
	}

	/**
	 * Clicks Customer tab.
	 */
	public void clickCustomersTab() {
		assertThat(getWaitDriver().waitForElementToBeInteractable(CUSTOMERS_TAB_CSS))
				.as("Customer tab is not interactable")
				.isTrue();

		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CUSTOMERS_TAB_CSS)));
	}

	/**
	 * Clicks Accounts tab.
	 */
	public void clickAccountsTab() {
		assertThat(getWaitDriver().waitForElementToBeInteractable(ACCOUNTS_TAB_CSS))
				.as("Account tab is not interactable")
				.isTrue();

		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(ACCOUNTS_TAB_CSS)));
	}

	/**
	 * Enters Shared ID.
	 *
	 * @param sharedId String
	 */
	public void enterSharedID(final String sharedId) {
		getWaitDriver().waitForElementToBeInteractable(SHAREDID_INPUT_CSS);
		clearAndType(SHAREDID_INPUT_CSS, sharedId);
	}

	/**
	 * Enters Email.
	 *
	 * @param email String
	 */
	public void enterEmail(final String email) {
		getWaitDriver().waitForElementToBeInteractable(EMAIL_INPUT_CSS);
		clearAndType(EMAIL_INPUT_CSS, email);
	}

	/**
	 * Enters Username.
	 *
	 * @param username String
	 */
	public void enterUsername(final String username) {
		getWaitDriver().waitForElementToBeInteractable(USERNAME_INPUT_CSS);
		clearAndType(USERNAME_INPUT_CSS, username);
	}

	/**
	 * Enters customer phone number in search.
	 *
	 * @param phoneNumber String
	 */
	public void enterPhoneNumber(final String phoneNumber) {
		getWaitDriver().waitForElementToBeInteractable(PHONE_INPUT_CSS);
		clearAndType(PHONE_INPUT_CSS, phoneNumber);
	}

	/**
	 * Clicks Search for customer.
	 *
	 * @return CustomerSearchResultsPane
	 */
	public CustomerSearchResultsPane clickCustomerSearch() {
		clickButtonAndWaitForPaneToOpen(SEARCH_BUTTON_CSS, "Search", CustomerSearchResultsPane.getCustomerSearchResultParentCss());
		return new CustomerSearchResultsPane(getDriver());
	}

	/**
	 * Clicks Search for account.
	 *
	 * @return {@link AccountSearchResultPane}
	 */
	public AccountSearchResultPane clickAccountSearch() {
		clickButtonAndWaitForPaneToOpen(SEARCH_BUTTON_CSS, "Search", AccountSearchResultPane.getResultsListTableParentCssParentCss());
		return new AccountSearchResultPane(getDriver());
	}

	/**
	 * Clear the input fields in customers tab.
	 */
	public void clearInputFieldsInCustomersTab() {
		clickButton(CLEAR_BUTTON_CSS, "Clear");
		waitForFocus(SHAREDID_INPUT_CSS);
	}

	/**
	 * Clear the input fields in accounts tab.
	 */
	public void clearInputFieldsInAccountsTab() {
		clickButton(CLEAR_BUTTON_CSS, "Clear");
		waitForFocus(ACCOUNT_SHAREDID_INPUT_CSS);
	}

	/**
	 * Clear the input fields in orders tab.
	 */
	public void clearInputFieldsInOrdersTab() {
		clickButton(CLEAR_BUTTON_CSS, "Clear");
		waitForFocus(ORDER_NUMBER_INPUT_CSS);
	}

	/**
	 * Enters First Name.
	 *
	 * @param firstName String
	 */
	public void enterFirstName(final String firstName) {
		getWaitDriver().waitForElementToBeInteractable(FIRST_NAME_INPUT_CSS);
		clearAndType(FIRST_NAME_INPUT_CSS, firstName);
	}

	/**
	 * Enters Last Name.
	 *
	 * @param lastName String
	 */
	public void enterLastName(final String lastName) {
		getWaitDriver().waitForElementToBeInteractable(LAST_NAME_INPUT_CSS);
		clearAndType(LAST_NAME_INPUT_CSS, lastName);
	}

	/**
	 * Enters Postal code.
	 *
	 * @param postalCode String
	 */
	public void enterPostalCode(final String postalCode) {
		getWaitDriver().waitForElementToBeInteractable(POSTAL_CODE_INPUT_CSS);
		clearAndType(POSTAL_CODE_INPUT_CSS, postalCode);
	}

	/**
	 * Selects store.
	 *
	 * @param store the store.
	 */
	public void selectStore(final String store) {
		assertThat(selectComboBoxItem(STORE_COMBO_CSS, store))
				.as("Unable to find store - " + store)
				.isTrue();
	}

	/**
	 * Selects status.
	 *
	 * @param orderStatus the status.
	 */
	public void selectStatus(final String orderStatus) {
		assertThat(selectComboBoxItem(ORDER_STATUS_COMBO_CSS, orderStatus))
				.as("Unable to find orders with status - " + orderStatus)
				.isTrue();
	}

	/**
	 * Selects Shipment status.
	 *
	 * @param shipmentStatus the Shipment status.
	 */
	public void selectShipmentStatus(final String shipmentStatus) {
		assertThat(selectComboBoxItem(ORDER_SHIPMENT_STATUS_COMBO_CSS, shipmentStatus))
				.as("Unable to find orders shipment status - " + shipmentStatus)
				.isTrue();
	}

	/**
	 * Enters Order SKU Code.
	 *
	 * @param skuCode String
	 */
	public void enterSkuCode(final String skuCode) {
		click(ORDER_NUMBER_LABEL_CSS);
		scrollDownWithDownArrowKey(getDriver().findElement(By.cssSelector(ORDERS_TAB_CSS)), 1);
		scrollElementIntoView(SKU_CODE_INPUT_CSS);
		getWaitDriver().waitForElementToBeInteractable(SKU_CODE_INPUT_CSS);
		clearAndType(SKU_CODE_INPUT_CSS, skuCode);
	}

	/**
	 * Inputs 'from' date.
	 */
	public void enterFromDate() {
		getWaitDriver().waitForElementToBeInteractable(FROM_DATE_INPUT_CSS);
		WebElement fromDateElement = getDriver().findElement(By.cssSelector(FROM_DATE_INPUT_CSS));
		fromDateElement.clear();
		enterDateWithJavaScript(FROM_DATE_INPUT_CSS, getFormattedDateTime(0));
		updateDateField(fromDateElement);
	}

	/**
	 * Inputs 'to' date.
	 */
	public void enterToDate() {
		getWaitDriver().waitForElementToBeInteractable(TO_DATE_INPUT_CSS);
		WebElement toDateElement = getDriver().findElement(By.cssSelector(TO_DATE_INPUT_CSS));
		toDateElement.clear();
		enterDateWithJavaScript(TO_DATE_INPUT_CSS, getFormattedDateTime(1));
		updateDateField(toDateElement);
	}

	/**
	 * Enters Shared ID.
	 *
	 * @param sharedID shared id.
	 */
	public void enterAccountSharedID(final String sharedID) {
		getWaitDriver().waitForElementToBeInteractable(ACCOUNT_SHAREDID_INPUT_CSS);
		clearAndType(ACCOUNT_SHAREDID_INPUT_CSS, sharedID);
	}

	/**
	 * Enters Business Name.
	 *
	 * @param businessName business name.
	 */
	public void enterBusinessName(final String businessName) {
		getWaitDriver().waitForElementToBeInteractable(BUSINESS_NAME_INPUT_CSS);
		clearAndType(BUSINESS_NAME_INPUT_CSS, businessName);
	}

	/**
	 * Enters Business Number.
	 *
	 * @param businessNumber business number.
	 */
	public void enterBusinessNumber(final String businessNumber) {
		getWaitDriver().waitForElementToBeInteractable(BUSINESS_NUMBER_INPUT_CSS);
		clearAndType(BUSINESS_NUMBER_INPUT_CSS, businessNumber);
	}

	/**
	 * Enters Zip / Postal Code.
	 *
	 * @param zipCode zip code.
	 */
	public void enterZipCode(final String zipCode) {
		getWaitDriver().waitForElementToBeInteractable(ZIP_CODE_INPUT_CSS);
		clearAndType(ZIP_CODE_INPUT_CSS, zipCode);
	}

	private void updateDateField(final WebElement element) {
		element.click();
		element.sendKeys(Keys.SPACE);
		element.sendKeys(Keys.BACK_SPACE);
	}

	private void waitForFocus(final String widgetPath) {
		int count = 0;
		while (!isElementFocused(widgetPath) && count < Constants.RETRY_COUNTER_5) {
			sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			count++;
		}
	}
}
