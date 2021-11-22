/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.cortexTestObjects.Purchase;
import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AddEditCustomerAddressDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditItemDetailsDialog;
import com.elasticpath.selenium.dialogs.MoveItemDialog;
import com.elasticpath.selenium.dialogs.OpenNoteDialog;
import com.elasticpath.selenium.dialogs.OrderHoldDialog;
import com.elasticpath.selenium.dialogs.PaymentProcessingErrorDialog;
import com.elasticpath.selenium.dialogs.SelectASkuDialog;
import com.elasticpath.selenium.dialogs.TransactionDetailsDialog;
import com.elasticpath.selenium.domainobjects.PaymentConfiguration;
import com.elasticpath.selenium.domainobjects.Shipment;
import com.elasticpath.selenium.domainobjects.ShipmentTableRecord;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.CompleteExchangeWizard;
import com.elasticpath.selenium.wizards.CompleteReturnWizard;
import com.elasticpath.selenium.wizards.CreateExchangeWizard;
import com.elasticpath.selenium.wizards.CreateRefundWizard;
import com.elasticpath.selenium.wizards.CreateReturnWizard;

/**
 * Order Editor.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass", "PMD.ExcessiveClassLength"})
public class OrderEditor extends AbstractPageObject {

	/**
	 * Page Object Id.
	 */
    public static final String ORDER_EDITOR_TAB = "div[automation-id='com.elasticpath.cmclient.fulfillment"
            + ".FulfillmentMessages.OrderEditor_ToolTipText'][active-tab='true']";
	public static final String EDITOR_PANE_PARENT_CSS = "div[pane-location='editor-pane'] div[active-editor='true'] ";
	private static final String CANCEL_ORDER_BUTTON_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Cancel order'][seeable='true']";
	private static final String EDITOR_BUTTON_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='%s'][seeable='true']";
	private static final String CREATE_REFUND_BUTTON_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Create refund'][seeable='true']";
	private static final String ORDER_STATUS_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Order status'] > input";
	private static final String ORDER_BALANCE_DUE = EDITOR_PANE_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.OrderSummaryOverviewSection_BalanceDue'] > input";
	private static final String SHIPMENT_STATUS_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Status'] > input";
	private static final String TAB_CSS = "div[widget-id='%s'][seeable='true']";
	private static final String PAYMENT_TABLE_CSS = "div[widget-id='Order Payments History Table'][widget-type='Table'][seeable='true']";
	private static final String PAYMENT_TABLE_ROW_CSS = PAYMENT_TABLE_CSS + " div[widget-type='table_row']";
	private static final String PAYMENT_ROW_CSS = PAYMENT_TABLE_CSS + " div[widget-type='row']";
	private static final String VIEW_PAYMENT_DETAILS_BUTTON = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.OrderPaymentHistorySection_ViewPaymentDetailsButton'][seeable='true']";
	private static final String NOTES_TABLE_CSS = "div[widget-id='Order Notes Table'][widget-type='Table']";
	private static final String NOTES_TABLE_ROW_CSS = NOTES_TABLE_CSS + " div[widget-type='table_row']";
	private static final String OPEN_NOTE_BUTTON = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.OrderNoteNotes_Button']";
	private static final String COLUMN_ID_CSS = "div[column-id='%s']";
	private static final String COLUMN_NUM_CSS = "div[column-num='%d']";
	private static final String SKU_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Sku Table'] ";
	private static final String SKU_COLUMN_CSS = SKU_TABLE_PARENT_CSS + COLUMN_ID_CSS;
	private static final String EXCHANGE_ORDER_NUMBER_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Exchange order #'] > input";
	private static final String EXCHANGE_ORDER_STATUS_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Status'] > input";
	private static final String ORDER_NUMBER_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Order #'] > input";
	private static final String EXTERNAL_ORDER_NUMBER_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='External order #'] > input";
	private static final String ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id*='Shipment Table']"
			+ "[widget-type='Table'][seeable='true'] ";
	private static final String E_SHIPMENT_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[automation-id*='ElectronicShipmentSection_Title'] + div ";
	private static final String E_SHIPMENT_RETURN_BUTTON_CSS = E_SHIPMENT_PARENT_CSS + "div[automation-id*='CreateReturnButton']";
	private static final String ORDER_DETAIL_E_SHIPMENT_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id*='Electronic Shipment Table']"
			+ "[widget-type='Table'][seeable='true'] ";
	private static final String ORDER_SHIPMENT_DETAIL_TABLE_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id*='%s Table'][widget-type='Table'][seeable"
			+ "='true'] ";
	private static final String ORDER_DETAIL_SHIPMENT_TABLE_COLUMN_CSS = ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS + "div[column-id='%s']";
	private static final String ATTRIBUTE_VALUE = "value";
	private static final String EMAIL_ADDRESS_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-type='Hyperlink'][seeable='true']";
	private static final String FULFILLMENT_MESSAGE = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String ORDER_SUMMARY_TITLE = FULFILLMENT_MESSAGE + ".OrderSummaryPage_Form_Title'][seeable='true']";
	private static final String ORDER_DETAILS_TITLE = FULFILLMENT_MESSAGE + ".OrderDetailPage_Form_Title'][seeable='true']";
	private static final String ORDER_PAYMENTS_TITLE = FULFILLMENT_MESSAGE + ".OrderPaymentsPage_Form_Title'][seeable='true']";
	private static final String RETURNS_AND_EXCHANGES_TITLE = FULFILLMENT_MESSAGE + ".OrderReturnsPage_Title'][seeable='true']";
	private static final String NOTES_TITLE = FULFILLMENT_MESSAGE + ".OrderNotePage_Title'][seeable='true']";
	private static final String ITEM_DETAIL_BUTTON_CSS = FULFILLMENT_MESSAGE
			+ ".ShipmentSection_EditItemAttributesButton'][seeable='true']";
	private static final String RETURNED_SKU_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Sku Table'] ";
	private static final String RETURNED_SKU_COLUMN_CSS = RETURNED_SKU_TABLE_PARENT_CSS + COLUMN_ID_CSS;
	private static final int SLEEP_TIME = 500;
	private static final String UNLOCK_ORDER_CSS = FULFILLMENT_MESSAGE + ".OrderActionUnlockOrder'][seeable='true']";
	private static final String SHIPMENT_SECTION_BY_NUMBER_CSS = "div[widget-id*='%s'] + div ";
	private static final String SHIPMENT_DETAIL_TABLE = EDITOR_PANE_PARENT_CSS + SHIPMENT_SECTION_BY_NUMBER_CSS + "div[widget-id*='Shipment Table']"
			+ "[widget-type='Table'][seeable='true'] ";
	private static final String SHIPMENT_SUMMARY_PRICE_VALUE_CSS = SHIPMENT_SECTION_BY_NUMBER_CSS + "div[automation-id$=%s][widget-type='Text'] "
			+ "+ div[widget-type='Text'] > input";
	private static final String SHIPMENT_SUMMARY_SHIPMENT_DISCOUNT_DIV_CSS = "div[widget-id*=' shipment discount'][ widget-type='Text'] + div";
	private static final String SHIPMENT_SUMMARY_SHIPMENT_DISCOUNT_CSS = SHIPMENT_SUMMARY_SHIPMENT_DISCOUNT_DIV_CSS + " > input";
	private static final String SHIPMENT_SUMMARY_SHIPMENT_COST_DIV_CSS = "div[widget-id='Shipping cost:'][ widget-type='Text'] + div";
	private static final String SHIPMENT_SUMMARY_SHIPMENT_COST_CSS = SHIPMENT_SUMMARY_SHIPMENT_COST_DIV_CSS + " > input";
	private static final String PROMOTION_TABLE_PARENT_CSS = "div[widget-id='Order Promotions Table'] ";
	private static final String PROMOTION_COLUMN_CSS = PROMOTION_TABLE_PARENT_CSS + COLUMN_ID_CSS + "[column-num='1']";
	private static final String BILLING_PHONE_NUMBER_CSS = FULFILLMENT_MESSAGE
			+ ".OrderSummaryBillingAddressSection_PhoneNumber'] > input";
	private static final String ORDER_DATE_VALUE_CSS = FULFILLMENT_MESSAGE
			+ ".OrderSummaryOverviewSection_CreatedDate'] > input";
	private static final String ORDER_DETAILS_SHIPPING_METHOD_CSS = FULFILLMENT_MESSAGE
			+ ".ShipmentSection_ShippingMethod'][widget-type='CCombo'][seeable='true']";
	private static final String ORDER_DETAILS_SHIPPING_ADDRESS_CSS = FULFILLMENT_MESSAGE
			+ ".ShipmentSection_ShippingAddress'][widget-type='CCombo']";
	private static final String SHIPMENT_TABLE_COLUMN_CSS = ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS + COLUMN_NUM_CSS;
	private static final String BUNDLE_NAME_COLUMN_NAME = "Bundle name";
	private static final String INVENTORY_COLUMN_NAME = "Inventory";
	private static final String SKU_CODE_COLUMN_NAME = "SKU code";
	private static final String PRODUCT_NAME_COLUMN_NAME = "Product name";
	private static final String SKU_OPTION_COLUMN_NAME = "SKU option";
	private static final String LIST_PRICE_COLUMN_NAME = "List price";
	private static final String SALE_PRICE_COLUMN_NAME = "Sale price";
	private static final String QUANTITY_COLUMN_NAME = "Qty";
	private static final String DISCOUNT_COLUMN_NAME = "Discount";
	private static final String TOTAL_PRICE_COLUMN_NAME = "Total price";
	private static final String PAYMENT_SCHEDULE_COLUMN_NAME = "Payment schedule";
	private static final String METHOD_COLUMN_NAME = "Method";
	private static final String TYPE_COLUMN_NAME = "Type";
	private static final String DETAILS_COLUMN_NAME = "Details";
	private static final String STATUS_COLUMN_NAME = "Status";
	private static final String AMOUNT_COLUMN_NAME = "Amount";
	private static final List<String> SHIPMENT_TABLE_COLUMN_HEADER_VALUES = Arrays.asList(BUNDLE_NAME_COLUMN_NAME, INVENTORY_COLUMN_NAME,
			SKU_CODE_COLUMN_NAME, PRODUCT_NAME_COLUMN_NAME, SKU_OPTION_COLUMN_NAME, LIST_PRICE_COLUMN_NAME, SALE_PRICE_COLUMN_NAME,
			QUANTITY_COLUMN_NAME, DISCOUNT_COLUMN_NAME, TOTAL_PRICE_COLUMN_NAME);
	private static final List<String> ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES = Arrays.asList("Dummy Value", BUNDLE_NAME_COLUMN_NAME,
			SKU_CODE_COLUMN_NAME, PRODUCT_NAME_COLUMN_NAME, SKU_OPTION_COLUMN_NAME, LIST_PRICE_COLUMN_NAME, SALE_PRICE_COLUMN_NAME,
			QUANTITY_COLUMN_NAME, DISCOUNT_COLUMN_NAME, TOTAL_PRICE_COLUMN_NAME);
	private static final List<String> RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES = Arrays.asList("Dummy Value", BUNDLE_NAME_COLUMN_NAME,
			SKU_CODE_COLUMN_NAME, PRODUCT_NAME_COLUMN_NAME, SKU_OPTION_COLUMN_NAME, LIST_PRICE_COLUMN_NAME, SALE_PRICE_COLUMN_NAME,
			QUANTITY_COLUMN_NAME, DISCOUNT_COLUMN_NAME, TOTAL_PRICE_COLUMN_NAME, PAYMENT_SCHEDULE_COLUMN_NAME);
	private static final Logger LOGGER = LogManager.getLogger(OrderEditor.class);
	private static final String PHYSICAL_SHIPMENT_ITEM_ROW_CSS = EDITOR_PANE_PARENT_CSS
			+ "div[parent-widget-id='Order Details Physical Shipment Table'][widget-type='table_row']";
	private static final String E_SHIPMENT_ITEM_ROW_CSS = EDITOR_PANE_PARENT_CSS
			+ "div[parent-widget-id='Order Details Electronic Shipment Table'][widget-type='table_row']";
	private static final String CANCEL_SHIPMENT_BUTTON = SHIPMENT_SECTION_BY_NUMBER_CSS + FULFILLMENT_MESSAGE
			+ ".ShipmentSection_CancelShipmentButton']";
	private static final String RELEASE_SHIPMENT_BUTTON_CSS = "div[widget-id*='%s']+div div[automation-id="
			+ "'com.elasticpath.cmclient.fulfillment.FulfillmentMessages.ShipmentSection_ReleaseShipmentButton']";
	private static final String ORDER_HOLD_RESOLVED_COMMENT = "ORDER_HOLD_RESOLVED";
	private static final String ORDER_HOLD_UNRESOLVABLE_COMMENT = "ORDER_HOLD_UNRESOLVABLE";
	private static final String MARK_ORDER_HOLD_RESOLVED_BUTTON_CSS =
			"div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.OrderHoldList_MarkResolvedButton'";
	private static final String MARK_ORDER_HOLD_UNRESOLVABLE_BUTTON_CSS =
			"div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.OrderHoldList_MarkUnresolvableButton'";
	private static final String ACTIVE_STATUS = "ACTIVE";
	private static WebElement shipmentItemRow;
	private static final String ORDER_DATA_TABLE_CSS = "div[widget-id='Field Value Table']";
	private static final String ORDER_DATA_TABLE_ROW_CSS = "div[parent-widget-id='Field Value Table']"
			+ "[widget-id='%s'] div[column-num='1']";
	private static final String PAYMENT_SUMMARY_ORDERED_VALUE = FULFILLMENT_MESSAGE
			+ ".OrderPaymentSummaySection_Ordered'] > input";
	private static final String PAYMENT_SUMMARY_PAID_VALUE = FULFILLMENT_MESSAGE
			+ ".OrderPaymentSummaySection_Paid'] > input";
	private static final String PAYMENT_SUMMARY_DUE_VALUE = FULFILLMENT_MESSAGE
			+ ".OrderPaymentSummaySection_Due'] > input";
	private static final String MOVE_SHIPMENT_BUTTON = SHIPMENT_SECTION_BY_NUMBER_CSS + FULFILLMENT_MESSAGE + ".ShipmentSection_MoveItemButton']";
	private static final String SHOW_SKIPPED_PAYMENT_EVENTS = FULFILLMENT_MESSAGE + ".ShowSkippedPaymentEvents_Label'][seeable='true']";
	private static final String RELEASE_SHIPMENT_BUTTON = "Release shipment";
	private static final String HOLD_TITLE = FULFILLMENT_MESSAGE + ".OrderHoldPage_Title'][seeable='true']";
	private static final String ORDER_HOLD_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS
			+ "div[widget-id*='Order Hold Table'][widget-type='Table'][seeable='true'] ";
	private static final String ORDER_HOLD_TABLE_COLUMN_CSS = ORDER_HOLD_TABLE_PARENT_CSS + COLUMN_NUM_CSS;
	private static final String ORDER_HOLD_TABLE_COLUMN_VALUE_CSS = ORDER_HOLD_TABLE_PARENT_CSS + COLUMN_ID_CSS;
	private static final int ORDER_HOLD_TABLE_STATUS_COLUMN_NUM=1;
	private static final int ORDER_HOLD_TABLE_RULE_COLUMN_NUM=0;
	private static final int ORDER_HOLD_TABLE_RESOLVEDBY_COLUMN_NUM=2;
	private static final String ORDER_HOLD_RESOLVE_DIALOG_CSS =
			"div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.OrderHoldDialog_ResolveHoldWindowTitle'";
	private static final String ORDER_HOLD_UNRESOLVABLE_DIALOG_CSS =
			"div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.OrderHoldDialog_UnresolvableHoldWindowTitle'";




	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public OrderEditor(final WebDriver driver) {
		super(driver);
	}

    /**
     * Clicks on the current Order Tab editor.
     */
    public void clickCurrentOrderTab() {
        click(ORDER_EDITOR_TAB);
    }

	/**
	 * Clicks Cancel Order button.
	 *
	 * @return the confirm dialog
	 */
	public ConfirmDialog clickCancelOrderButton() {
		clickButton(CANCEL_ORDER_BUTTON_CSS, "Cancel");
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Clicks the complete return button.
	 *
	 * @return the complete return wizard
	 */
	public CompleteReturnWizard clickCompleteReturnButton() {
		clickEditorButton("Complete return...");
		return new CompleteReturnWizard(getDriver());
	}

	/**
	 * Clicks the complete return button.
	 *
	 * @return the complete return wizard
	 */
	public CompleteExchangeWizard clickCompleteExchangeButton() {
		clickEditorButton("Complete exchange...");
		return new CompleteExchangeWizard(getDriver());
	}

	/**
	 * Checks if Editor button in view port.
	 *
	 * @param buttonWidgetId String the widget id
	 * @return boolean
	 */
	public boolean isEditorButtonInViewport(final String buttonWidgetId) {
		getWaitDriver().waitForElementToBeNotStale(String.format(EDITOR_BUTTON_CSS, buttonWidgetId));
		scrollWidgetIntoView(String.format(EDITOR_BUTTON_CSS, buttonWidgetId));
		return isElementInViewport(String.format(EDITOR_BUTTON_CSS, buttonWidgetId));
	}

	/**
	 * Clicks button.
	 *
	 * @param buttonWidgetId String the widget id
	 */
	public void clickEditorButton(final String buttonWidgetId) {
		scrollWidgetIntoView(String.format(EDITOR_BUTTON_CSS, buttonWidgetId));
		getWaitDriver().waitForElementToBeInteractable(String.format(EDITOR_BUTTON_CSS, buttonWidgetId));
		clickButton(String.format(EDITOR_BUTTON_CSS, buttonWidgetId), buttonWidgetId);
	}

	/**
	 * Clicks Cancel Shipment button.
	 *
	 * @return the confirm dialog
	 */
	public ConfirmDialog clickCancelShipmentButton() {
		clickEditorButton("Cancel shipment");
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Clicks Create Return button.
	 *
	 * @return CreateReturnWizard
	 */
	public CreateReturnWizard clickCreateReturnButton() {
		clickEditorButton("Create return ");
		return new CreateReturnWizard(getDriver());
	}

	/**
	 * Checks if Create Return button in view port.
	 *
	 * @return boolean
	 */
	public boolean isCreateReturnButtonInViewport() {
		return isEditorButtonInViewport("Create return ");
	}

	/**
	 * Clicks Create Exchange button.
	 *
	 * @return CreateExchangeWizard
	 */
	public CreateExchangeWizard clickCreateExchangeButton() {
		clickEditorButton("Create exchange");
		return new CreateExchangeWizard(getDriver());
	}

	/**
	 * Clicks Cancel Exchange button.
	 */
	public ConfirmDialog clickCancelExchangeButton() {
		clickEditorButton("Cancel exchange...");
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Create payment processing error dialog.
	 *
	 * @return PaymentProcessingErrorDialog
	 */
	public PaymentProcessingErrorDialog createPaymentProcessingErrorDialog() {
		return new PaymentProcessingErrorDialog(getDriver());
	}


	/**
	 * Clicks Release Shipment button.
	 */
	public void clickReleaseShipmentButton() {
		String buttonText = RELEASE_SHIPMENT_BUTTON;
		scrollWidgetIntoView(String.format(EDITOR_BUTTON_CSS, buttonText));
		clickButton(String.format(EDITOR_BUTTON_CSS, buttonText), buttonText);
		new ConfirmDialog(getDriver()).clickOKButton("FulfillmentMessages.ShipmentSection_ReleaseShipmentConfirm");
	}

	/**
	 * Release shipment by shipment number.
	 *
	 * @param shipmentNumber the shipment number
	 */
	public void releaseShipmentByShipmentID(final String shipmentNumber) {
		String releaseShipmentButton = String.format(RELEASE_SHIPMENT_BUTTON_CSS, shipmentNumber);
		scrollWidgetIntoView(releaseShipmentButton);
		clickButton(releaseShipmentButton, RELEASE_SHIPMENT_BUTTON);
		new ConfirmDialog(getDriver()).clickOKButton("FulfillmentMessages.ShipmentSection_ReleaseShipmentConfirm");
	}

	/**
	 * Cancel shipment by shipment number.
	 *
	 * @param shipmentNumber the shipment number
	 */
	public void cancelShipmentByShipmentNumber(final String shipmentNumber) {
		String cancelShipmentButton = String.format(CANCEL_SHIPMENT_BUTTON, shipmentNumber);
		scrollWidgetIntoView(cancelShipmentButton);
		clickButton(cancelShipmentButton, "Cancel shipment");
		new ConfirmDialog(getDriver()).clickOKButton("FulfillmentMessages.ShipmentSection_CancelShipment");
	}

	/**
	 * Checks if Release Shipment button in view port.
	 *
	 * @return boolean
	 */
	public boolean isReleaseShipmentButtonInViewport() {
		return isEditorButtonInViewport(RELEASE_SHIPMENT_BUTTON);
	}

	/**
	 * Verifies is release shipment button disabled.
	 */
	public void verifyReleaseShipmentButtonDisabled() {
		String releaseShipmentButtonCssSelector = String.format(EDITOR_BUTTON_CSS, RELEASE_SHIPMENT_BUTTON);
		verifyButtonIsDisabled(releaseShipmentButtonCssSelector, RELEASE_SHIPMENT_BUTTON);
	}

	/**
	 * Clicks Create Refund button.
	 *
	 * @return CreateRefundWizard
	 */
	public CreateRefundWizard clickCreateRefundButton() {
		clickButton(CREATE_REFUND_BUTTON_CSS, "Create refund");
		return new CreateRefundWizard(getDriver());
	}

	/**
	 * Clicks Open Exchange Order button.
	 */
	public void clickOpenExchangeOrderButton() {
		clickEditorButton("Open exchange order...");
	}

	/**
	 * Clicks Move Item button.
	 *
	 * @return MoveItemDialog
	 */
	public MoveItemDialog clickMoveItemButton() {
		clickEditorButton("Move item...");
		return new MoveItemDialog(getDriver());
	}

	/**
	 * Move item by shipment number.
	 *
	 * @param shipmentNumber the shipment number
	 */
	public MoveItemDialog moveItemByShipmentNumber(final String sku, final String shipmentNumber) {
		String moveShipmentButton = String.format(MOVE_SHIPMENT_BUTTON, shipmentNumber);
		selectSkuByShipment(shipmentNumber, sku);
		clickButton(moveShipmentButton, "Move item");
		return new MoveItemDialog(getDriver());
	}

	/**
	 * Clicks Add Item button.
	 *
	 * @return SelectASkuDialog
	 */
	public SelectASkuDialog clickAddItemButton() {
		clickEditorButton("Add item...");
		return new SelectASkuDialog(getDriver());
	}

	/**
	 * Clicks Remove Item button.
	 */
	public void clickRemoveItemButton() {
		clickEditorButton("Remove item...");
		new ConfirmDialog(getDriver()).clickOKButton("FulfillmentMessages.ShipmentSection_RemoveItemConfirm");
	}

	/**
	 * Verifies Order Status.
	 *
	 * @param expectedOrderStatus the expected order status.
	 */
	public void verifyOrderStatus(final String expectedOrderStatus) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_STATUS_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order status validation failed")
				.isEqualTo(expectedOrderStatus);
	}

	/**
	 * Verifies status of exchange order.
	 *
	 * @param expectedStatus the expected status of exchange order.
	 */
	public void verifyExchangeOrderStatus(final String expectedStatus) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(EXCHANGE_ORDER_STATUS_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order status validation failed")
				.isEqualTo(expectedStatus);
	}

	/**
	 * Verifies Order Balance Due.
	 *
	 * @param expectedBalanceDue the expected order status.
	 */
	public void verifyOrderBalance(final String expectedBalanceDue) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_BALANCE_DUE)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order balance due does not match.")
				.isEqualTo(expectedBalanceDue);
	}

	/**
	 * Verifies Shipment Status.
	 *
	 * @param expectedShipmentStatus the expected shipment status.
	 */
	public void verifyShipmentStatus(final String expectedShipmentStatus) {
		getWaitDriver().waitForTextInInput(By.cssSelector(SHIPMENT_STATUS_INPUT_CSS), expectedShipmentStatus);
		assertThat(getDriver().findElement(By.cssSelector(SHIPMENT_STATUS_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("Shipment status validation failed")
				.isEqualTo(expectedShipmentStatus);
	}

	/**
	 * Clicks to select tab.
	 *
	 * @param tabName the tab name.
	 */
	public void clickTab(final String tabName) {
        clickCurrentOrderTab();
		String cssSelector = String.format(TAB_CSS, tabName);
		resizeWindow(cssSelector);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
		setWebDriverImplicitWait(1);
		if (!isElementPresent(By.cssSelector(cssSelector + "[active-tab='true']"))) {
			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
		}
		setWebDriverImplicitWaitToDefault();

		switch (tabName) {
			case "Summary":
				getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_SUMMARY_TITLE));
				break;
			case "Details":
				getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_DETAILS_TITLE));
				break;
			case "Payments":
				getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_PAYMENTS_TITLE));
				break;
			case "Returns and exchanges":
				getWaitDriver().waitForElementToBeVisible(By.cssSelector(RETURNS_AND_EXCHANGES_TITLE));
				break;
			case "Notes":
				getWaitDriver().waitForElementToBeVisible(By.cssSelector(NOTES_TITLE));
				break;
			case "Holds":
				getWaitDriver().waitForElementToBeVisible(By.cssSelector(HOLD_TITLE));
				break;
			default:
				fail("Editor is not visible for tab - " + tabName);
				return;
		}
	}

	/**
	 * Verifies that Payment Transaction table is empty.
	 */
	public void verifyPaymentTransactionTableIsEmpty() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		final List<WebElement> paymentTransactions = getPaymentTransactionsByCssSelector(PAYMENT_TABLE_ROW_CSS);
		if (!paymentTransactions.isEmpty()) {
			fail("Found unexpected payment transaction in payment history");
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verifies Order Payment Transaction in Payment History with Skipped events.
	 *
	 * @param paymentMap the payment transaction map.
	 */
	public void verifyNoSuchPaymentTransactionInPaymentHistoryWithSkippedEvents(final Map<String, String> paymentMap) {
		final List<WebElement> paymentTransactions = getPaymentTransactionsByCssSelector(PAYMENT_TABLE_ROW_CSS);
		final List<WebElement> paymentTransactionsInRow = getPaymentTransactionsByCssSelector(PAYMENT_ROW_CSS);

		verifyNoSuchPaymentTransaction(paymentMap, paymentTransactions);
		verifyNoSuchPaymentTransaction(paymentMap, paymentTransactionsInRow);
	}

	/**
	 * Verifies Type of Order Payment Transaction.
	 *
	 * @param transactionType the type of payment transaction.
	 */
	public void verifyNoSuchPaymentTransactionType(final String transactionType) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		final List<WebElement> paymentTransactions = getDriver().findElements(By.cssSelector(PAYMENT_TABLE_ROW_CSS));
		for (WebElement paymentTransaction : paymentTransactions) {
			String type = getPaymentTransactionColumnValue(paymentTransaction, 2);

			if (transactionType.equals(type)) {
				fail("Found unexpected payment transaction: %s", transactionType);
			}
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verifies Order Payment Transaction.
	 *
	 * @param paymentMap the payment transaction map.
	 */
	public void verifyNoSuchPaymentTransaction(final Map<String, String> paymentMap) {
		final List<WebElement> paymentTransactions = getPaymentTransactionsByCssSelector(PAYMENT_TABLE_ROW_CSS);
		verifyNoSuchPaymentTransaction(paymentMap, paymentTransactions);
	}

	private void verifyNoSuchPaymentTransaction(final Map<String, String> paymentMap, final List<WebElement> paymentTransactions) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		for (WebElement paymentTransaction : paymentTransactions) {
			String type = getPaymentTransactionColumnValue(paymentTransaction, 2);
			String details = getPaymentTransactionColumnValue(paymentTransaction, 3);
			String status = getPaymentTransactionColumnValue(paymentTransaction, 4);
			String amount = getPaymentTransactionColumnValue(paymentTransaction, 5);

			final boolean typeMatch = paymentMap.containsKey(TYPE_COLUMN_NAME) && paymentMap.get(TYPE_COLUMN_NAME).equals(type);
			final boolean detailsMatch = paymentMap.containsKey(DETAILS_COLUMN_NAME) && paymentMap.get(DETAILS_COLUMN_NAME).equals(details);
			final boolean statusMatch = paymentMap.containsKey(STATUS_COLUMN_NAME) && paymentMap.get(STATUS_COLUMN_NAME).equals(status);
			final boolean amountMatch = paymentMap.containsKey(AMOUNT_COLUMN_NAME) && paymentMap.get(AMOUNT_COLUMN_NAME).equals(amount);
			if (typeMatch && detailsMatch && statusMatch && amountMatch) {
				fail("Found unexpected payment transaction: %s", paymentMap.keySet().stream()
						.map(key -> key + ": " + paymentMap.get(key))
						.collect(Collectors.joining(",\n", "\n", "")));
			}
		}
		setWebDriverImplicitWaitToDefault();
	}

	private boolean isPaymentTransactionsCheckSuccessful(final PaymentConfiguration paymentConfiguration, final Map<String, String> paymentMap,
														 final List<WebElement> paymentTransactions, final String cssSelectorForTableRow) {
		for (WebElement paymentTransaction : paymentTransactions) {
			final String method = getPaymentTransactionColumnValue(paymentTransaction, 1);
			final String type = getPaymentTransactionColumnValue(paymentTransaction, 2);
			final String details = getPaymentTransactionColumnValue(paymentTransaction, 3);
			final String status = getPaymentTransactionColumnValue(paymentTransaction, 4);
			final String amount = getPaymentTransactionColumnValue(paymentTransaction, 5);
			final boolean methodMatch;
			if (paymentConfiguration.getConfigurationName().equals(StringUtils.EMPTY)) {
				methodMatch = !paymentMap.containsKey(METHOD_COLUMN_NAME) || paymentMap.get(METHOD_COLUMN_NAME).equals(method);
			} else {
				LOGGER.debug("Verifying payment method name - " + paymentConfiguration.getConfigurationName());
				methodMatch = !paymentMap.containsKey(METHOD_COLUMN_NAME) || paymentConfiguration.getConfigurationName().equals(method);
			}
			final boolean typeMatch = !paymentMap.containsKey(TYPE_COLUMN_NAME) || paymentMap.get(TYPE_COLUMN_NAME).equals(type);
			final boolean detailsMatch = !paymentMap.containsKey(DETAILS_COLUMN_NAME) || paymentMap.get(DETAILS_COLUMN_NAME).equals(details);
			final boolean statusMatch = !paymentMap.containsKey(STATUS_COLUMN_NAME) || paymentMap.get(STATUS_COLUMN_NAME).equals(status);
			final boolean amountMatch = !paymentMap.containsKey(AMOUNT_COLUMN_NAME) || paymentMap.get(AMOUNT_COLUMN_NAME).equals(amount);
			if (methodMatch && typeMatch && detailsMatch && statusMatch && amountMatch) {
				final String concretePaymentTableRowCss = cssSelectorForTableRow + "[id='%s']";
				final String rowIdCss = String.format(concretePaymentTableRowCss, paymentTransaction.getAttribute("id"));
				assertThat(selectItemInEditorPaneWithScrollBar(PAYMENT_TABLE_CSS, rowIdCss + " " + COLUMN_ID_CSS, type))
						.as("Unable to find payment transaction row - " + rowIdCss)
						.isTrue();
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifies Order Payment Transaction with skipped events.
	 *
	 * @param paymentMap the payment transaction map.
	 */
	public void verifyPaymentTransactionWithSkippedEvents(final PaymentConfiguration paymentConfiguration, final Map<String, String> paymentMap) {
		if (isPaymentTransactionsCheckSuccessful(paymentConfiguration, paymentMap,
				getPaymentTransactionsByCssSelector(PAYMENT_TABLE_ROW_CSS), PAYMENT_TABLE_ROW_CSS)) {
			TransactionDetailsDialog transactionDetailsDialog = clickViewPaymentDetails();
			transactionDetailsDialog.verifyOrderPaymentDetails(paymentConfiguration, paymentMap);
			return;
		}

		if (isPaymentTransactionsCheckSuccessful(paymentConfiguration, paymentMap,
				getPaymentTransactionsByCssSelector(PAYMENT_ROW_CSS), PAYMENT_ROW_CSS)) {
			TransactionDetailsDialog transactionDetailsDialog = clickViewPaymentDetails();
			transactionDetailsDialog.verifyOrderPaymentDetails(paymentConfiguration, paymentMap);
			return;
		}

		fail("Could not find expected payment transaction: %s", paymentMap.keySet().stream()
				.map(key -> key + ": " + paymentMap.get(key))
				.collect(Collectors.joining(",\n", "\n", "")));
	}

	private List<WebElement> getPaymentTransactionsByCssSelector(final String paymentRowCss) {
		return getDriver().findElements(By.cssSelector(paymentRowCss));
	}

	/**
	 * Verifies Order Payment Transaction.
	 *
	 * @param paymentMap the payment transaction map.
	 */
	public void verifyPaymentTransaction(final PaymentConfiguration paymentConfiguration, final Map<String, String> paymentMap) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		if (isPaymentTransactionsCheckSuccessful(paymentConfiguration, paymentMap,
				getPaymentTransactionsByCssSelector(PAYMENT_ROW_CSS), PAYMENT_TABLE_ROW_CSS)) {
			TransactionDetailsDialog transactionDetailsDialog = clickViewPaymentDetails();
			transactionDetailsDialog.verifyOrderPaymentDetails(paymentConfiguration, paymentMap);
			return;
		} else {
			getDriver().findElement(By.cssSelector(PAYMENT_TABLE_CSS)).click();
			scrollDownWithDownArrowKey(getDriver().findElement(By.cssSelector(PAYMENT_TABLE_CSS)), 5);
			if (isPaymentTransactionsCheckSuccessful(paymentConfiguration, paymentMap,
					getPaymentTransactionsByCssSelector(PAYMENT_TABLE_ROW_CSS), PAYMENT_TABLE_ROW_CSS)) {
				TransactionDetailsDialog transactionDetailsDialog = clickViewPaymentDetails();
				transactionDetailsDialog.verifyOrderPaymentDetails(paymentConfiguration, paymentMap);
				return;
			}
		}
		setWebDriverImplicitWaitToDefault();

		fail("Could not find expected payment transaction: %s", paymentMap.keySet().stream()
				.map(key -> key + ": " + paymentMap.get(key))
				.collect(Collectors.joining(",\n", "\n", "")));
	}

	private String getPaymentTransactionColumnValue(final WebElement paymentTransaction, final int columnNumber) {
		setWebDriverImplicitWait(0);
		final List<WebElement> cellElement = paymentTransaction.findElements(By.cssSelector(String.format(COLUMN_NUM_CSS, columnNumber)));
		setWebDriverImplicitWaitToDefault();
		return cellElement.isEmpty() ? "" : cellElement.get(0).getText();
	}

	/**
	 * Verifies Order Note.
	 *
	 * @param noteParts the note parts to match.
	 */
	public void verifyOrderNote(final Collection<String> noteParts) {
		final List<WebElement> orderNotes = getPaymentTransactionsByCssSelector(NOTES_TABLE_ROW_CSS);
		for (WebElement orderNote : orderNotes) {
			String description = orderNote.findElement(By.cssSelector(String.format(COLUMN_NUM_CSS, 2))).getText();
			boolean descriptionMatch = true;
			for (String notePart : noteParts) {
				descriptionMatch &= description.contains(notePart);
			}
			if (descriptionMatch) {
				final String concretePaymentTableRowCss = NOTES_TABLE_ROW_CSS + "[id='%s']";
				final String rowIdCss = String.format(concretePaymentTableRowCss, orderNote.getAttribute("id"));
				assertThat(selectItemInEditorPaneWithScrollBar(NOTES_TABLE_CSS, rowIdCss + " " + COLUMN_ID_CSS, description))
						.as("Unable to find order note row - " + rowIdCss)
						.isTrue();
				return;
			}
		}

		fail("Could not find expected order note: %s", String.join(" (.*) ", noteParts));
	}

	/**
	 * Verifies returned sku code.
	 *
	 * @param skuCode the sku code
	 */
	public void verifyReturnSkuCode(final String skuCode) {
		sleep(SLEEP_TIME);
		assertThat(selectItemInDialog(SKU_TABLE_PARENT_CSS, SKU_COLUMN_CSS, skuCode, SKU_CODE_COLUMN_NAME))
				.as("Unable to find sku - " + skuCode)
				.isTrue();
	}

	/**
	 * Verifies exchange order number is present.
	 *
	 * @return exchangeOrderNumber String
	 */
	public String verifyExchangeOrderNumberIsPresent() {
		String exchangeOrderNumber = getDriver().findElement(By.cssSelector(EXCHANGE_ORDER_NUMBER_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE);
		assertThat(Integer.parseInt(exchangeOrderNumber) > 0)
				.as("Unable to find exchange order number")
				.isTrue();
		return exchangeOrderNumber;
	}

	/**
	 * Verifies original and exchange order numbers.
	 *
	 * @param originalOrderNumber String
	 * @param exchangeOrderNumber String
	 */
	public void verifyOriginalAndExchangeOrderNumbers(final String originalOrderNumber, final String exchangeOrderNumber) {
		assertThat(getDriver().findElement(By.cssSelector(ORDER_NUMBER_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("Exchange order number validation failed")
				.isEqualTo(exchangeOrderNumber);

		assertThat(getDriver().findElement(By.cssSelector(EXTERNAL_ORDER_NUMBER_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("Exchange order number validation failed")
				.isEqualTo(originalOrderNumber);
	}

	/**
	 * Verifies and select order sku code.
	 *
	 * @param skuCode the sku code
	 */
	public void verifyAndSelectOrderSkuCode(final String skuCode) {
		sleep(SLEEP_TIME);
		assertThat(selectItemInDialog(ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS, ORDER_DETAIL_SHIPMENT_TABLE_COLUMN_CSS, skuCode, SKU_CODE_COLUMN_NAME))
				.as("Unable to find order Shipment sku - " + skuCode)
				.isTrue();
	}

	/**
	 * Select shipment sku by shipment number.
	 *
	 * @param shipmentNumber String
	 * @param sku            String
	 */
	public void selectSkuByShipment(final String shipmentNumber, final String sku) {
		String shipmentSection = String.format(RELEASE_SHIPMENT_BUTTON_CSS, shipmentNumber);
		scrollWidgetIntoView(shipmentSection);
		sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);
		String shipmentSku = String.format(SHIPMENT_DETAIL_TABLE, shipmentNumber) + String.format(COLUMN_ID_CSS, sku);
		scrollWidgetIntoView(shipmentSku);
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(shipmentSku)).click();
	}

	/**
	 * Verifies and select order sku code in E-shipment list.
	 *
	 * @param skuCode the sku code
	 */
	public void verifyAndSelectOrderEshipmentSkuCode(final String skuCode) {
		sleep(SLEEP_TIME);
		scrollWidgetIntoView(E_SHIPMENT_RETURN_BUTTON_CSS);
		sleep(SLEEP_TIME);

		assertThat(selectItemInDialog(ORDER_DETAIL_E_SHIPMENT_TABLE_PARENT_CSS, ORDER_DETAIL_SHIPMENT_TABLE_COLUMN_CSS, skuCode,
				SKU_CODE_COLUMN_NAME))
				.as("Unable to find order E-shipment sku - " + skuCode)
				.isTrue();
	}

	/**
	 * Verifies sku is not in the list.
	 *
	 * @param skuCode the sku code
	 */
	public void verifySkuCodeIsNotInList(final String skuCode) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(verifyItemIsNotInDialog(ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS, ORDER_DETAIL_SHIPMENT_TABLE_COLUMN_CSS, skuCode,
				SKU_CODE_COLUMN_NAME))
				.as("Sku is still in the list - " + skuCode)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}


	/**
	 * Verifies shipment number.
	 *
	 * @param shipmentNumber String
	 */
	public void verifyShipmentNumber(final String shipmentNumber) {
		assertThat(getDriver().findElement(By.cssSelector(EDITOR_PANE_PARENT_CSS)).getText().contains(shipmentNumber))
				.as("Unable to find shipment number - " + shipmentNumber)
				.isTrue();
	}

	/**
	 * Returns customer's email address.
	 *
	 * @return cusomer's email
	 */
	public String getCustomerEmail() {
		return getDriver().findElement(By.cssSelector(EMAIL_ADDRESS_CSS)).getText();
	}

	/**
	 * Clicks Open Customer Profile button.
	 *
	 * @return CustomerEditor
	 */
	public CustomerEditor clickOpenCustomerProfileButton() {
		clickEditorButton("Open customer profile...");
		getWaitDriver().waitForElementToBeVisible(By.cssSelector("div[widget-id='Basic Profile'][seeable='true']"));
		return new CustomerEditor(getDriver());
	}

	/**
	 * Opens Item Detail dialog.
	 *
	 * @param skuCode sku code.
	 * @return EditItemDetailsDialog.
	 */
	public EditItemDetailsDialog clickItemDetailButton(final String skuCode) {
		getWaitDriver().waitForElementToBeInteractable(ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS);
		assertThat(selectItemInDialog(ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS, ORDER_DETAIL_SHIPMENT_TABLE_COLUMN_CSS, skuCode, SKU_CODE_COLUMN_NAME))
				.as("Unable to find order sku - " + skuCode)
				.isTrue();
		clickButton(ITEM_DETAIL_BUTTON_CSS, "Item Detail", EditItemDetailsDialog.EDIT_ITEM_DIALOG_PARENT_CSS);
		return new EditItemDetailsDialog((getDriver()));
	}

	/**
	 * * Verifies Returned sku table  column value.
	 *
	 * @param columnValue the column value.
	 * @param columnName  the column number.
	 */
	public void verifyReturnedSkuColumnValue(final String columnValue, final String columnName) {
		sleep(SLEEP_TIME);
		assertThat(selectItemInEditorPane(RETURNED_SKU_TABLE_PARENT_CSS, RETURNED_SKU_COLUMN_CSS, columnValue, columnName))
				.as("Unable to find returned sku column value - " + columnValue)
				.isTrue();
	}

	/**
	 * Clicks Edit Address... button.
	 *
	 * @return EditAddressDialog
	 */
	public AddEditCustomerAddressDialog clickEditAddressButton() {
		clickEditorButton("Edit shipping address...");
		return new AddEditCustomerAddressDialog(getDriver());
	}

	/**
	 * Verify UnLock Order button is not enabled.
	 */
	public void verifyUnlockOrderIsNotEnabled() {
		assertThat(isButtonEnabled(UNLOCK_ORDER_CSS))
				.as("Order is still locked after saving.")
				.isFalse();
	}

	/**
	 * Creates and populates a list of Shipment objects using the values defined in cucumber.
	 *
	 * @param shipments the properties of each shipment belonging to an order
	 * @return Shipment objects
	 */
	public List<Shipment> populateShipments(final List<Map<String, String>> shipments) {
		List<Shipment> shipmentObjects = new ArrayList<>();
		for (Map<String, String> shipment : shipments) {
			Shipment shipmentObject = new Shipment();
			String shipmentNumber = shipment.get("shipment-number");
			if (!"E-shipment".equals(shipmentNumber)) {
				shipmentNumber = Purchase.getPurchaseNumber() + "-" + shipmentNumber;
			}
			shipmentObject.setShipmentNumber(shipmentNumber);
			shipmentObject.setItemSubTotal(shipment.get("item-sub-total"));
			shipmentObject.setShippingCost(shipment.get("shipping-cost"));
			shipmentObject.setShipmentDiscount(shipment.get("shipment-discount"));
			shipmentObject.setTotalBeforeTax(shipment.get("total-before-tax"));
			shipmentObject.setItemTaxes(shipment.get("item-taxes"));
			shipmentObject.setShippingTaxes(shipment.get("shipping-taxes"));
			shipmentObject.setShipmentTotal(shipment.get("shipment-total"));
			shipmentObjects.add(shipmentObject);
		}
		return shipmentObjects;
	}

	/**
	 * Verifies values in order shipment table based on shipment type
	 *
	 * @param record ShipmentTableRecord class object representing one table record
	 */
	public void verifyOrderItemsTableValues(final ShipmentTableRecord record) {
		String webElement;
		switch (record.getShipmentType()) {
			case "physical":
				webElement = String.format(ORDER_SHIPMENT_DETAIL_TABLE_CSS, "Order Details Physical Shipment");
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(BUNDLE_NAME_COLUMN_NAME),
						record.getBundleName());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(INVENTORY_COLUMN_NAME),
						record.getInventory());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_CODE_COLUMN_NAME), record.getSkuCode());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PRODUCT_NAME_COLUMN_NAME),
						record.getProductName());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_OPTION_COLUMN_NAME),
						record.getSkuOptions());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(LIST_PRICE_COLUMN_NAME),
						record.getListPrice());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SALE_PRICE_COLUMN_NAME),
						record.getSalePrice());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(QUANTITY_COLUMN_NAME), record.getQuantity());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(DISCOUNT_COLUMN_NAME), record.getDiscount());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(TOTAL_PRICE_COLUMN_NAME),
						record.getTotalPrice());
				break;
			case "e-shipment":
				webElement = String.format(ORDER_SHIPMENT_DETAIL_TABLE_CSS, "Order Details Electronic Shipment");
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(BUNDLE_NAME_COLUMN_NAME),
						record.getBundleName());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_CODE_COLUMN_NAME),
						record.getSkuCode());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PRODUCT_NAME_COLUMN_NAME),
						record.getProductName());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_OPTION_COLUMN_NAME),
						record.getSkuOptions());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(LIST_PRICE_COLUMN_NAME),
						record.getListPrice());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SALE_PRICE_COLUMN_NAME),
						record.getSalePrice());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(QUANTITY_COLUMN_NAME),
						record.getQuantity());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(DISCOUNT_COLUMN_NAME),
						record.getDiscount());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(TOTAL_PRICE_COLUMN_NAME),
						record.getTotalPrice());
				break;
			case "recurring":
				webElement = String.format(ORDER_SHIPMENT_DETAIL_TABLE_CSS, "Order Details Recurring Items");
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(BUNDLE_NAME_COLUMN_NAME),
						record.getBundleName());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_CODE_COLUMN_NAME),
						record.getSkuCode());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PRODUCT_NAME_COLUMN_NAME),
						record.getProductName());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_OPTION_COLUMN_NAME),
						record.getSkuOptions());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(LIST_PRICE_COLUMN_NAME),
						record.getListPrice());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SALE_PRICE_COLUMN_NAME),
						record.getSalePrice());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(QUANTITY_COLUMN_NAME),
						record.getQuantity());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(DISCOUNT_COLUMN_NAME),
						record.getDiscount());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(TOTAL_PRICE_COLUMN_NAME),
						record.getTotalPrice());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PAYMENT_SCHEDULE_COLUMN_NAME),
						record.getPaymentSchedule());
				break;
			default:
				fail("Unrecognized shipment type - " + record.getShipmentType());
				break;
		}


	}

	/**
	 * Verifies value of specified table and column
	 *
	 * @param webElementCSS specifies order shipment table to check
	 * @param columnIndex   specifies column to check the value
	 * @param expectedValue expected value
	 */
	private void verifyShipmentTableLineItemRow(final String webElementCSS, final Integer columnIndex, final String expectedValue) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		try {
			assertThat(getDriver().findElement(By.cssSelector(
					webElementCSS + String.format(COLUMN_NUM_CSS, columnIndex))).getText())
					.as("Shipment Line Item Value was not as expected")
					.isEqualTo(expectedValue);
		} catch (NoSuchElementException e) {
			if (!"".equals(expectedValue)) {
				fail("Expected value: '" + expectedValue + "' not found in the table record");
			}
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verifies the values in the Summary section of a Shipment.
	 *
	 * @param shipmentNumber the shipment number if the items in the shipment are Physical, E-shipment if the items are Digital
	 * @param expectedValue  the expected discount value
	 * @param key            of the shipment property to verify
	 */
	private void verifyShipmentSummaryValue(final String shipmentNumber, final String expectedValue, final String key) {
		assertThat(getWaitDriver().waitForElementToBeVisibleAndToBeNotStale(String.format(SHIPMENT_SUMMARY_PRICE_VALUE_CSS, shipmentNumber, key))
				.getAttribute(ATTRIBUTE_VALUE))
				.as("Expected " + key + " value does not match.")
				.isEqualTo(expectedValue);
	}

	/**
	 * Verifies Shipment Subtotal.
	 *
	 * @param shipmentNumber the shipmentNumber
	 * @param subtotal       the expected subtotal
	 */
	public void verifyShipmentSubtotal(final String shipmentNumber, final String subtotal) {
		verifyShipmentSummaryValue(shipmentNumber, subtotal, "ShippingItemTotal");
	}

	/**
	 * Verifies Shipment Shipping Cost.
	 *
	 * @param shipmentNumber the shipmentNumber
	 * @param shippingCost   the expected shipping cost
	 */
	public void verifyShipmentShippingCost(final String shipmentNumber, final String shippingCost) {
		verifyShipmentSummaryValue(shipmentNumber, shippingCost, "ShippingCost");
	}

	/**
	 * Verifies Shipment Discount.
	 *
	 * @param shipmentNumber   the shipmentNumber
	 * @param shipmentDiscount the expected discount value
	 */
	public void verifyShipmentDiscount(final String shipmentNumber, final String shipmentDiscount) {
		verifyShipmentSummaryValue(shipmentNumber, shipmentDiscount, "ShipmentDiscount");
	}

	/**
	 * Verifies Shipment Total Before Tax.
	 *
	 * @param shipmentNumber the shipmentNumber
	 * @param totalBeforeTax the expected total before tax
	 */
	public void verifyShipmentTotalBeforeTax(final String shipmentNumber, final String totalBeforeTax) {
		verifyShipmentSummaryValue(shipmentNumber, totalBeforeTax, "TotalBeforeTax");
	}

	/**
	 * Verifies Shipment Item Taxes.
	 *
	 * @param shipmentNumber the shipmentNumber
	 * @param itemTaxes      the expected item taxes
	 */
	public void verifyShipmentItemTaxes(final String shipmentNumber, final String itemTaxes) {
		verifyShipmentSummaryValue(shipmentNumber, itemTaxes, "ShippingItemTax");
	}

	/**
	 * Verifies Shipment Discount.
	 *
	 * @param shipmentNumber the shipmentNumber
	 * @param shippingTaxes  the expected shipping taxes
	 */
	public void verifyShipmentShippingTaxes(final String shipmentNumber, final String shippingTaxes) {
		verifyShipmentSummaryValue(shipmentNumber, shippingTaxes, "ShipmentTaxes");
	}

	/**
	 * Verifies Shipment Total.
	 *
	 * @param shipmentNumber the shipmentNumber
	 * @param shipmentTotal  the expected total value
	 */
	public void verifyShipmentTotal(final String shipmentNumber, final String shipmentTotal) {
		verifyShipmentSummaryValue(shipmentNumber, shipmentTotal, "ShipmentTotal");
	}

	/**
	 * Verifies Promotion Name.
	 *
	 * @param promotionColumnValue the applied promotion name.
	 * @param promotionColumnName  the applied promotion name.
	 */
	public void verifyPromotionColumnValue(final String promotionColumnValue, final String promotionColumnName) {
		scrollDownWithDownArrowKey(getDriver().findElement(By.cssSelector(EDITOR_PANE_PARENT_CSS.trim())), 1);
		scrollWidgetIntoView(getDriver().findElement(By.cssSelector(PROMOTION_TABLE_PARENT_CSS.trim())));
		assertThat(selectItemInEditorPane(PROMOTION_TABLE_PARENT_CSS, PROMOTION_COLUMN_CSS, promotionColumnValue, promotionColumnName))
				.as("Unable to find promotion name - " + promotionColumnValue)
				.isTrue();
	}

	/**
	 * Clicks Edit Billing address... button.
	 *
	 * @return EditAddressDialog
	 */
	public AddEditCustomerAddressDialog clickEditBillingAddressButton() {
		clickEditorButton("Edit address...");
		return new AddEditCustomerAddressDialog(getDriver());
	}

	/**
	 * Verifies Order Phone Number.
	 *
	 * @param expectedPhoneNumber the expected shipping phone number.
	 */
	public void verifyPhoneNumber(final String expectedPhoneNumber) {
		getWaitDriver().waitForTextInInput(By.cssSelector(BILLING_PHONE_NUMBER_CSS), expectedPhoneNumber);
		assertThat(getDriver().findElement(By.cssSelector(BILLING_PHONE_NUMBER_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order Billing Phone Number does not match the order search result.")
				.isEqualTo(expectedPhoneNumber);
	}

	/**
	 * Verify given order search result returns in date range.
	 */
	public void verifyOrderSearchResultDateRange() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		try {
			assertThat(getDateTimeFromEpFormattedString(getDriver().findElement(By.cssSelector(ORDER_DATE_VALUE_CSS)).getAttribute(ATTRIBUTE_VALUE)))
					.isBetween(getDateWithoutTime(0), getDateWithoutTime(1));
		} catch (ParseException e) {
			LOGGER.warn(e.getMessage());
		}

		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Selects Shipping Method in combo box.
	 *
	 * @param shippingMethod String
	 */
	public void selectShippingMethod(final String shippingMethod) {
		sleep(Constants.SLEEP_FIVE_SECONDS_IN_MILLIS);
		assertThat(selectComboBoxItem(ORDER_DETAILS_SHIPPING_METHOD_CSS, shippingMethod))
				.as("Unable to find Shipping Method - " + shippingMethod)
				.isTrue();
	}

	/**
	 * Selects Shipping Method in combo box by shipment number.
	 *
	 * @param shipmentNumber the shipment number
	 * @param shippingMethod the shipment method
	 */
	public void selectShippingMethodByShipmentNumber(final int shipmentNumber, final String shippingMethod) {
		sleep(Constants.SLEEP_FIVE_SECONDS_IN_MILLIS);
		assertThat(selectComboBoxItem(String.format(SHIPMENT_SECTION_BY_NUMBER_CSS, Purchase.getPurchaseNumber() + "-" + shipmentNumber) + ORDER_DETAILS_SHIPPING_METHOD_CSS, shippingMethod))
				.as("Unable to find Shipping Method - " + shippingMethod)
				.isTrue();
	}

	/**
	 * Verifies Shipping Method.
	 *
	 * @param expectedShippingMethod the expected shipping method.
	 */
	public void verifyShippingMethod(final String expectedShippingMethod) {
		assertThat(getDriver().findElement(By.cssSelector(ORDER_DETAILS_SHIPPING_METHOD_CSS + " div")).getAttribute("widget-id"))
				.as("Shipping Method validation failed")
				.isEqualTo(expectedShippingMethod);
	}

	/**
	 * Selects shipping address.
	 *
	 * @param shippingAddress the shipping address
	 */
	public void selectShippingAddress(final String shippingAddress) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_DETAILS_SHIPPING_ADDRESS_CSS));
		assertThat(selectComboBoxItem(ORDER_DETAILS_SHIPPING_ADDRESS_CSS, shippingAddress))
				.as("Unable to find shipping address - " + shippingAddress)
				.isTrue();
	}

	/**
	 * Verify a value in a particular column of a row in the Items table for a Shipment.
	 *
	 * @param column        the column to verify
	 * @param expectedValue the expected value
	 */
	private void verifyShipmentLineItemRow(final String column, final String expectedValue) {
		assertThat(getDriver().findElement(By.cssSelector(String.format(SHIPMENT_TABLE_COLUMN_CSS,
				SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(column)))).getText())
				.as("Shipment Line Item Value was not expected")
				.isEqualTo(expectedValue);
	}

	/**
	 * Set value to specified field in the row in the Items table for a Shipment.
	 *
	 * @param column        the column to set new value
	 * @param expectedValue new value to be set
	 */
	private void setShipmentLineItemRowValue(final String column, final String expectedValue) {
		click(getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(SHIPMENT_TABLE_COLUMN_CSS,
				SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(column)))));
		String textValue = getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(SHIPMENT_TABLE_COLUMN_CSS,
				SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(column)))).getText();

		getWaitDriver().waitForElementToBeVisible(
				By.cssSelector(ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS + " div[widget-id='" + textValue + "'] input:not([readonly])")).sendKeys(expectedValue);
	}

	/**
	 * Verify the value of SKU Code column in a particular row of the Items table of a Shipment.
	 *
	 * @param sku the price expected.
	 */
	public void verifyShipmentLineItemSkuCode(final String sku) {
		verifyShipmentLineItemRow("SKU code", sku);
	}

	/**
	 * Verify the value of Sale Price column in a particular row of the Items table of a Shipment.
	 *
	 * @param salePrice the price expected.
	 */
	public void verifyShipmentLineItemSalePrice(final String salePrice) {
		verifyShipmentLineItemRow("Sale price", salePrice);
	}

	/**
	 * Verify the value of Qty column in a particular row of the Items table of a Shipment.
	 *
	 * @param quantity the quantity expected.
	 */
	public void verifyShipmentLineItemQuantity(final String quantity) {
		verifyShipmentLineItemRow("Qty", quantity);
	}

	/**
	 * Verify the value of Discount column in a particular row of the Items table of a Shipment.
	 *
	 * @param discount the price expected.
	 */
	public void verifyShipmentLineItemDiscount(final String discount) {
		verifyShipmentLineItemRow("Discount", discount);
	}

	/**
	 * Verify the value of Total Price column in a particular row of the Items table of a Shipment.
	 *
	 * @param totalPrice the price expected.
	 */
	public void verifyShipmentLineItemTotalPrice(final String totalPrice) {
		verifyShipmentLineItemRow("Total price", totalPrice);
	}

	/**
	 * Set new quantity for the order shipment line item
	 *
	 * @param quantity new line item quantity
	 */
	public void setShipmentLineItemQuantity(final String quantity) {
		setShipmentLineItemRowValue("Qty", quantity);
	}

	/**
	 * Set order shipment line item discount
	 *
	 * @param discount discount amount to be set
	 */
	public void setShipmentLineItemDiscount(final String discount) {
		setShipmentLineItemRowValue("Discount", discount);
	}

	/**
	 * Set new value in order Less Shipment Discount field
	 *
	 * @param discount new value to be set
	 */
	public void setLessShipmentDiscountValue(final String discount) {
		scrollWidgetIntoView(SHIPMENT_SUMMARY_SHIPMENT_DISCOUNT_DIV_CSS);
		enterNewValueForField(SHIPMENT_SUMMARY_SHIPMENT_DISCOUNT_CSS, discount);
	}

	/**
	 * Set new value in order Shipping Cost field
	 *
	 * @param cost new value to be set
	 */
	public void setShippingCostValue(final String cost) {
		scrollWidgetIntoView(SHIPMENT_SUMMARY_SHIPMENT_COST_DIV_CSS);
		enterNewValueForField(SHIPMENT_SUMMARY_SHIPMENT_COST_CSS, cost);
	}

	/**
	 * Set provided value into specified by CSS WebElement
	 *
	 * @param fieldCSS CSS value for WebElement to be modified
	 * @param newValue new value to be set
	 */
	private void enterNewValueForField(final String fieldCSS, final String newValue) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(fieldCSS));
		clearAndTypeNonJSCheck(fieldCSS, newValue);
	}

	/**
	 * Returns list of shipment item row.
	 *
	 * @param shipmentItemRowCss the shipment item row css
	 * @return List of webElement
	 */
	private List<WebElement> getShipmentRow(final String shipmentItemRowCss) {
		return getPaymentTransactionsByCssSelector(shipmentItemRowCss);
	}

	/**
	 * Returns shipment item row.
	 *
	 * @param skuCode            the sku code
	 * @param shipmentItemRowCss the shipment item row css
	 * @return webElement the shipment item row
	 */
	private WebElement getShipmentItemRow(final String skuCode, final String shipmentItemRowCss) {
		shipmentItemRow = null;
		getWaitDriver().waitForElementToBeInteractable(shipmentItemRowCss);
		for (WebElement itemRow : getShipmentRow(shipmentItemRowCss)) {
			if (itemRow.findElement(By.cssSelector(
					String.format(COLUMN_NUM_CSS, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_CODE_COLUMN_NAME))))
					.getText().equals(skuCode)) {
				shipmentItemRow = itemRow;
				getWaitDriver().waitForElementToBeClickable(itemRow);
				itemRow.click();
				break;
			}
		}
		assertThat(shipmentItemRow)
				.as("Unable to find shipment item sku: " + skuCode)
				.isNotNull();
		return shipmentItemRow;
	}

	/**
	 * Returns physical shipment item row.
	 *
	 * @param skuCode the sku code
	 * @return physical shipment item row
	 */
	private WebElement getPhysicalShipmentItemRow(final String skuCode) {
		return getShipmentItemRow(skuCode, PHYSICAL_SHIPMENT_ITEM_ROW_CSS);
	}

	/**
	 * Verifies physical shipment item sku code.
	 *
	 * @param skuCode the sku code
	 */
	public void verifyPhysicalShipmentItemSkuCode(final String skuCode) {
		getPhysicalShipmentItemRow(skuCode);
	}

	/**
	 * Verifies physical shipment sku code and quantity.
	 *
	 * @param skuCode  the sku code
	 * @param quantity the quantity
	 */
	public void verifyPhysicalShipmentSkuCodeAndQuantity(final String skuCode, final String quantity) {
		verifyPhysicalShipmentItemSkuCode(skuCode);
		verifyShipmentItemColumnValue(QUANTITY_COLUMN_NAME, quantity);
	}

	/**
	 * Returns e-shipment item row.
	 *
	 * @param skuCode the sku code
	 * @return e-shipment item row
	 */
	private WebElement getEShipmentItemRow(final String skuCode) {
		return getShipmentItemRow(skuCode, E_SHIPMENT_ITEM_ROW_CSS);
	}

	/**
	 * Verifies e-shipment item sku code.
	 *
	 * @param skuCode the sku code
	 */
	public void verifyEShipmentItemSkuCode(final String skuCode) {
		getEShipmentItemRow(skuCode);
	}

	/**
	 * Verifies e-shipment sku code and quantity.
	 *
	 * @param skuCode  the sku code
	 * @param quantity the quantity
	 */
	public void verifyEShipmentSkuCodeAndQuantity(final String skuCode, final String quantity) {
		scrollWidgetIntoView(E_SHIPMENT_RETURN_BUTTON_CSS);
		verifyEShipmentItemSkuCode(skuCode);
		verifyShipmentItemColumnValue(QUANTITY_COLUMN_NAME, quantity);
	}

	/**
	 * Verifies shipment item column value.
	 *
	 * @param columnName    the column name
	 * @param expectedValue the expected value
	 */
	private void verifyShipmentItemColumnValue(final String columnName, final String expectedValue) {
		String actualValue = shipmentItemRow.findElement(
				By.cssSelector(String.format(COLUMN_NUM_CSS, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(columnName)))).getText();
		assertThat(actualValue)
				.as("Shipment item " + columnName + " is not as expected")
				.isEqualTo(expectedValue);
	}

	private String getOrderDataTableValue(final String orderDataKey) {
		return getDriver().findElement(By.cssSelector(String.format(ORDER_DATA_TABLE_ROW_CSS, orderDataKey))).getText();
	}

	/**
	 * Verifies order data values.
	 *
	 * @param orderDataKey   the sku code
	 * @param orderDataValue the quantity
	 */
	public void verifyOrderDataTableRow(final String orderDataKey, final String orderDataValue) {
		scrollWidgetIntoView(ORDER_DATA_TABLE_CSS);
		assertThat(getOrderDataTableValue(orderDataKey))
				.as("Unable to find order data info: " + orderDataKey + " - " + orderDataValue)
				.isEqualTo(orderDataValue);
	}

	/**
	 * Clicks View Payment Details.
	 *
	 * @return TransactionDetailsDialog
	 */
	public TransactionDetailsDialog clickViewPaymentDetails() {
		clickButton(VIEW_PAYMENT_DETAILS_BUTTON, "View Payment Details", TransactionDetailsDialog.TRANSACTION_DETAIL_PARENT);
		return new TransactionDetailsDialog((getDriver()));
	}

	/**
	 * Clicks Open Note...
	 *
	 * @return OpenNoteDialog
	 */
	public OpenNoteDialog clickOpenNote() {
		clickButton(OPEN_NOTE_BUTTON, "Open Note...", OpenNoteDialog.NOTE_PARENT);
		return new OpenNoteDialog((getDriver()));
	}

	/**
	 * Verifies Payment ordered value.
	 *
	 * @param paymentSummaryMap payment summary map.
	 */
	public void verifyPaymentSummary(final Map<String, String> paymentSummaryMap) {
		assertThat(getDriver().findElement(By.cssSelector(PAYMENT_SUMMARY_ORDERED_VALUE)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order Payment Ordered value not match.")
				.isEqualTo(paymentSummaryMap.get("Ordered"));

		assertThat(getDriver().findElement(By.cssSelector(PAYMENT_SUMMARY_PAID_VALUE)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order Payment Paid value not match.")
				.isEqualTo(paymentSummaryMap.get("Paid"));

		assertThat(getDriver().findElement(By.cssSelector(PAYMENT_SUMMARY_DUE_VALUE)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order Payment Balance Due value not match.")
				.isEqualTo(paymentSummaryMap.get("Balance Due"));
	}

	/**
	 * Select Skipped Payment Events.
	 */
	public void selectSkippedPaymentEvents() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SHOW_SKIPPED_PAYMENT_EVENTS)));
	}

	/**
	 * Verifies order hold values.
	 *
	 * @param holdRule the description or rule that triggered the order hold.
	 * @param expectedStatus the expected status of the order hold.
	 * @param resolvedBy the login id of user who resolved the hold.
	 */
	public void verifyOrderHold(final String holdRule, final String expectedStatus, final String resolvedBy) {
		assertThat(getDriver().findElement(By.cssSelector(String.format(ORDER_HOLD_TABLE_COLUMN_CSS,
				ORDER_HOLD_TABLE_STATUS_COLUMN_NUM))).getText())
				.as("Order hold status was not expected")
				.isEqualTo(expectedStatus);
		assertThat(getDriver().findElement(By.cssSelector(String.format(ORDER_HOLD_TABLE_COLUMN_CSS,
				ORDER_HOLD_TABLE_RULE_COLUMN_NUM))).getText())
				.as("Order hold rule was not expected")
				.isEqualTo(holdRule);
		if (StringUtils.isNotEmpty(resolvedBy)) {
			assertThat(getDriver().findElement(By.cssSelector(String.format(ORDER_HOLD_TABLE_COLUMN_CSS,
					ORDER_HOLD_TABLE_RESOLVEDBY_COLUMN_NUM))).getText())
					.as("Order hold resolved by was not expected")
					.isEqualTo(resolvedBy);
		}
	}

	/**
	 * Resolves all order holds that are in ACTIVE status with a comment of ORDER_HOLD_RESOLVED.
	 */
	public void resolveAllHolds() {
		selectOrderHoldByStatus(ACTIVE_STATUS);
		OrderHoldDialog holdDialog = clickMarkOrderHoldResolvedButton();
		resolveHold(holdDialog);
	}

	/**
	 * Marks the first ACTIVE order hold as unresolvable with a comment of ORDER_HOLD_UNRESOLVABLE.
	 */
	public void markHoldUnresolvable() {
		selectOrderHoldByStatus(ACTIVE_STATUS);
		OrderHoldDialog holdDialog = clickMarkOrderHoldUnresolvableButton();
		markHoldUnresolvable(holdDialog);
	}

	/**
	 * Verifies the comment of an order hold row.
	 * @param status the status of the order hold
	 */
	public void verifyOrderHoldStatus(final String status) {
		WebElement element = getDriver().findElement(By.cssSelector(String.format(ORDER_HOLD_TABLE_COLUMN_CSS, ORDER_HOLD_TABLE_STATUS_COLUMN_NUM)));
		assertThat(element.getText())
				.as("Order hold has an unexpected status.")
				.isEqualTo(status);
	}

	/**
	 * Verify that the order hold cannot be marked as unresolvable or resolved by the current user.
	 */
	public void verifyOrderHoldUneditable() {
		selectOrderHoldByStatus(ACTIVE_STATUS);
		verifyOrderHoldNonResolvable();
		verifyOrderHoldNonUnresolvable();

	}

	/**
	 * Verify that the order hold cannot be resolved by the current user.
	 */
	public void verifyOrderHoldNonResolvable() {
		clickMarkOrderHoldResolvedButton();
		assertThat(isOrderHoldResolveDialogVisible())
				.as("User should not be able to resolve order holds.")
				.isEqualTo(false);
	}

	/**
	 * Verify that the order hold cannot be marked as unresolvable by the current user.
	 */
	public void verifyOrderHoldNonUnresolvable() {
		clickMarkOrderHoldUnresolvableButton();
		assertThat(isOrderHoldUnresolvableDialogVisible())
				.as("Order hold should not be possible to mark as unresolvable.")
				.isEqualTo(false);
	}

	private boolean isOrderHoldResolveDialogVisible() {
		return isElementPresent(By.cssSelector(ORDER_HOLD_RESOLVE_DIALOG_CSS));
	}

	private boolean isOrderHoldUnresolvableDialogVisible() {
		return isElementPresent(By.cssSelector(ORDER_HOLD_UNRESOLVABLE_DIALOG_CSS));
	}

	private void resolveHold(final OrderHoldDialog holdDialog) {
		holdDialog.enterComment(ORDER_HOLD_RESOLVED_COMMENT);
		holdDialog.clickOK();
	}

	private void markHoldUnresolvable(final OrderHoldDialog holdDialog) {
		holdDialog.enterComment(ORDER_HOLD_UNRESOLVABLE_COMMENT);
		holdDialog.clickOK();
	}

	private OrderHoldDialog clickMarkOrderHoldResolvedButton() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MARK_ORDER_HOLD_RESOLVED_BUTTON_CSS)));
		return new OrderHoldDialog(getDriver());
	}

	private OrderHoldDialog clickMarkOrderHoldUnresolvableButton() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MARK_ORDER_HOLD_UNRESOLVABLE_BUTTON_CSS)));
		return new OrderHoldDialog(getDriver());
	}

	private void selectOrderHoldByStatus(final String status) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(ORDER_HOLD_TABLE_COLUMN_VALUE_CSS, status))));
	}
}
