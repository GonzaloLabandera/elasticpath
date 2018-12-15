package com.elasticpath.selenium.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cortexTestObjects.Purchase;
import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.AddEditCustomerAddressDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditItemDetailsDialog;
import com.elasticpath.selenium.dialogs.MoveItemDialog;
import com.elasticpath.selenium.dialogs.SelectASkuDialog;
import com.elasticpath.selenium.domainobjects.Shipment;
import com.elasticpath.selenium.domainobjects.ShipmentTableRecord;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.CompleteReturnWizard;
import com.elasticpath.selenium.wizards.CreateExchangeWizard;
import com.elasticpath.selenium.wizards.CreateRefundWizard;
import com.elasticpath.selenium.wizards.CreateReturnWizard;

/**
 * Order Editor.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
public class OrderEditor extends AbstractPageObject {

	//TODO: the pane-location disappears if re-login to an idled CM. Re-check again when PB-3174 fixed.
	/**
	 * Page Object Id.
	 */
	public static final String EDITOR_PANE_PARENT_CSS = "div[pane-location='editor-pane'] div[active-editor='true'] ";
	private static final String CANCEL_ORDER_BUTTON_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Cancel Order'][seeable='true']";
	private static final String EDITOR_BUTTON_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='%s'][seeable='true']";
	private static final String CREATE_REFUND_BUTTON_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Create Refund'][seeable='true']";
	private static final String ORDER_STATUS_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Order Status'] > input";
	private static final String SHIPMENT_STATUS_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Status'] > input";
	private static final String TAB_CSS = "div[widget-id='%s'][seeable='true']";
	private static final String PAYMENT_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Order Payments History Table'] ";
	private static final String COLUMN_ID_CSS = "div[column-id='%s']";
	private static final String PAYMENT_COLUMN_CSS = PAYMENT_TABLE_PARENT_CSS + COLUMN_ID_CSS;
	private static final String SKU_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Sku Table'] ";
	private static final String SKU_COLUMN_CSS = SKU_TABLE_PARENT_CSS + COLUMN_ID_CSS;
	private static final String EXCHANGE_ORDER_NUMBER_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Exchange Order #'] > input";
	private static final String ORDER_NUMBER_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Order #'] > input";
	private static final String EXTERNAL_ORDER_NUMBER_INPUT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='External Order #'] > input";
	private static final String ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id*='Shipment Table']"
			+ "[widget-type='Table'][seeable='true'] ";
	private static final String ORDER_SHIPMENT_DETAIL_TABLE_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id*='%s Table'][widget-type='Table'][seeable='true'] ";
	private static final String ORDER_DETAIL_SHIPMENT_TABLE_COLUMN_CSS = ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS + "div[column-id='%s']";
	private static final String ATTRIBUTE_VALUE = "value";
	private static final String EMAIL_ADDRESS_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-type='Hyperlink'][seeable='true']";
	private static final String FULFILLMENT_MESSAGE = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String EDITOR_TITLE_LABEL = "[appearance-id='label-wrapper']";
	public static final String SHIPMENT_SECTION = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String ORDER_SUMMARY_TITLE = FULFILLMENT_MESSAGE + ".OrderSummaryPage_Form_Title'][seeable='true']"
			+ EDITOR_TITLE_LABEL;
	private static final String ORDER_DETAILS_TITLE = FULFILLMENT_MESSAGE + ".OrderDetailPage_Form_Title'][seeable='true']"
			+ EDITOR_TITLE_LABEL;
	private static final String ORDER_PAYMENTS_TITLE = FULFILLMENT_MESSAGE + ".OrderPaymentsPage_Form_Title'][seeable='true']"
			+ EDITOR_TITLE_LABEL;
	private static final String RETURNS_AND_EXCHANGES_TITLE = FULFILLMENT_MESSAGE + ".OrderReturnsPage_Title'][seeable='true']"
			+ EDITOR_TITLE_LABEL;
	private static final String ITEM_DETAIL_BUTTON_CSS = SHIPMENT_SECTION
			+ ".ShipmentSection_EditItemAttributesButton'][seeable='true']";
	private static final String RETURNED_SKU_TABLE_PARENT_CSS = EDITOR_PANE_PARENT_CSS + "div[widget-id='Sku Table'] ";
	private static final String RETURNED_SKU_COLUMN_CSS = RETURNED_SKU_TABLE_PARENT_CSS + COLUMN_ID_CSS;
	private static final int SLEEP_TIME = 500;
	private static final String UNLOCK_ORDER_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages"
			+ ".OrderActionUnlockOrder'][seeable='true']";
	private static final String SHIPMENT_SUMMARY_PRICE_VALUE_CSS = "[widget-id*='%s'] + div div[automation-id$=%s][widget-type='Text'] "
			+ "+ div[widget-type='Text'] > input";
	private static final String SHIPMENT_SUMMARY_SHIPMENT_DISCOUNT_CSS = "div[widget-id*=' Shipment Discount'][ widget-type='Text'] + div > input";
	private static final String SHIPMENT_SUMMARY_SHIPMENT_COST_CSS = "div[widget-id='Shipping Cost:'][ widget-type='Text'] + div > input";
	private static final String PROMOTION_TABLE_PARENT_CSS = "div[widget-id='Order Promotions Table'] ";
	private static final String PROMOTION_COLUMN_CSS = PROMOTION_TABLE_PARENT_CSS + COLUMN_ID_CSS + "[column-num='1']";
	private static final String BILLING_PHONE_NUMBER_CSS = SHIPMENT_SECTION
			+ ".OrderSummaryBillingAddressSection_PhoneNumber'] > input";
	private static final String ORDER_DATE_VALUE_CSS = SHIPMENT_SECTION
			+ ".OrderSummaryOverviewSection_CreatedDate'] > input";
	private static final String ORDER_DETAILS_SHIPPING_METHOD_CSS = SHIPMENT_SECTION
			+ ".ShipmentSection_ShippingMethod'][widget-type='CCombo'][seeable='true']";
	private static final String ORDER_DETAILS_SHIPPING_ADDRESS_CSS = SHIPMENT_SECTION
			+ ".ShipmentSection_ShippingAddress'][widget-type='CCombo']";
	private static final String SHIPMENT_TABLE_COLUMN_CSS = ORDER_DETAIL_SHIPMENT_TABLE_PARENT_CSS + "div[column-num='%s']";
	private static final String BUNDLE_NAME_COLUMN_NAME = "Bundle Name";
	private static final String INVENTORY_COLUMN_NAME = "Inventory";
	private static final String SKU_CODE_COLUMN_NAME = "SKU Code";
	private static final String PRODUCT_NAME_COLUMN_NAME = "Product Name";
	private static final String SKU_OPTION_COLUMN_NAME = "SKU Option";
	private static final String LIST_PRICE_COLUMN_NAME = "List Price";
	private static final String SALE_PRICE_COLUMN_NAME = "Sale Price";
	private static final String QUANTITY_COLUMN_NAME = "Qty";
	private static final String DISCOUNT_COLUMN_NAME = "Discount";
	private static final String TOTAL_PRICE_COLUMN_NAME = "Total Price";
	private static final String PAYMENT_SCHEDULE_COLUMN_NAME = "Payment Schedule";
	private static final List<String> SHIPMENT_TABLE_COLUMN_HEADER_VALUES = Arrays.asList(BUNDLE_NAME_COLUMN_NAME, INVENTORY_COLUMN_NAME,
			SKU_CODE_COLUMN_NAME, PRODUCT_NAME_COLUMN_NAME, SKU_OPTION_COLUMN_NAME, LIST_PRICE_COLUMN_NAME, SALE_PRICE_COLUMN_NAME,
			QUANTITY_COLUMN_NAME, DISCOUNT_COLUMN_NAME, TOTAL_PRICE_COLUMN_NAME);
	private static final List<String> ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES = Arrays.asList("Dummy Value", BUNDLE_NAME_COLUMN_NAME,
			SKU_CODE_COLUMN_NAME, PRODUCT_NAME_COLUMN_NAME, SKU_OPTION_COLUMN_NAME, LIST_PRICE_COLUMN_NAME, SALE_PRICE_COLUMN_NAME,
			QUANTITY_COLUMN_NAME, DISCOUNT_COLUMN_NAME, TOTAL_PRICE_COLUMN_NAME);
	private static final List<String> RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES = Arrays.asList("Dummy Value", BUNDLE_NAME_COLUMN_NAME,
			SKU_CODE_COLUMN_NAME, PRODUCT_NAME_COLUMN_NAME, SKU_OPTION_COLUMN_NAME, LIST_PRICE_COLUMN_NAME, SALE_PRICE_COLUMN_NAME,
			QUANTITY_COLUMN_NAME, DISCOUNT_COLUMN_NAME, TOTAL_PRICE_COLUMN_NAME, PAYMENT_SCHEDULE_COLUMN_NAME);
	private static final Logger LOGGER = Logger.getLogger(OrderEditor.class);

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public OrderEditor(final WebDriver driver) {
		super(driver);
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
		clickEditorButton("Complete Return...");
		return new CompleteReturnWizard(getDriver());
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
		clickEditorButton("Cancel Shipment");
		return new ConfirmDialog(getDriver());
	}

	/**
	 * Clicks Create Return button.
	 *
	 * @return CreateReturnWizard
	 */
	public CreateReturnWizard clickCreateReturnButton() {
		clickEditorButton("Create Return ");
		return new CreateReturnWizard(getDriver());
	}

	/**
	 * Checks if Create Return button in view port.
	 *
	 * @return boolean
	 */
	public boolean isCreateReturnButtonInViewport() {
		return isEditorButtonInViewport("Create Return ");
	}

	/**
	 * Clicks Create Exchange button.
	 *
	 * @return CreateExchangeWizard
	 */
	public CreateExchangeWizard clickCreateExchangeButton() {
		clickEditorButton("Create Exchange");
		return new CreateExchangeWizard(getDriver());
	}

	/**
	 * Clicks Release Shipment button.
	 */
	public void clickReleaseShipmentButton() {
		String buttonText = "Release Shipment";
		scrollWidgetIntoView(String.format(EDITOR_BUTTON_CSS, buttonText));
		clickButton(String.format(EDITOR_BUTTON_CSS, buttonText), buttonText);
		new ConfirmDialog(getDriver()).clickOKButton("FulfillmentMessages.ShipmentSection_ReleaseShipmentConfirm");
	}

	/**
	 * Checks if Release Shipment button in view port.
	 *
	 * @return boolean
	 */
	public boolean isReleaseShipmentButtonInViewport() {
		return isEditorButtonInViewport("Release Shipment");
	}

	/**
	 * Clicks Create Refund button.
	 *
	 * @return CreateRefundWizard
	 */
	public CreateRefundWizard clickCreateRefundButton() {
		clickButton(CREATE_REFUND_BUTTON_CSS, "Create Refund");
		return new CreateRefundWizard(getDriver());
	}

	/**
	 * Clicks Open Exchange Order button.
	 */
	public void clickOpenExchangeOrderButton() {
		clickEditorButton("Open Exchange Order...");
	}

	/**
	 * Clicks Move Item button.
	 *
	 * @return MoveItemDialog
	 */
	public MoveItemDialog clickMoveItemButton() {
		clickEditorButton("Move Item...");
		return new MoveItemDialog(getDriver());
	}

	/**
	 * Clicks Add Item button.
	 *
	 * @return SelectASkuDialog
	 */
	public SelectASkuDialog clickAddItemButton() {
		clickEditorButton("Add Item...");
		return new SelectASkuDialog(getDriver());
	}

	/**
	 * Clicks Remove Item button.
	 */
	public void clickRemoveItemButton() {
		clickEditorButton("Remove Item...");
		new ConfirmDialog(getDriver()).clickOKButton("FulfillmentMessages.ShipmentSection_RemoveItemConfirm");
	}

	/**
	 * Verifies Order Status.
	 *
	 * @param expectedOrderStatus the expected order status.
	 */
	public void verifyOrderStatus(final String expectedOrderStatus) {
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(ORDER_STATUS_INPUT_CSS)).getAttribute(ATTRIBUTE_VALUE))
				.as("Order Status validation failed")
				.isEqualTo(expectedOrderStatus);
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
		String cssSelector = String.format(TAB_CSS, tabName);
		resizeWindow(cssSelector);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(cssSelector)));
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
			case "Returns and Exchanges":
				getWaitDriver().waitForElementToBeVisible(By.cssSelector(RETURNS_AND_EXCHANGES_TITLE));
				break;
			default:
				fail("Editor is not visible for tab - " + tabName);
				return;
		}
	}

	/**
	 * Verifies Transaction Type.
	 *
	 * @param transactionType the transaction type.
	 */
	public void verifyTransactionType(final String transactionType) {
		verifyPaymentHistoryColumnValue(transactionType, "Transaction Type");
	}

	/**
	 * Verifies Payment History column value.
	 *
	 * @param columnValue the column value.
	 * @param columnName  the column number.
	 */
	public void verifyPaymentHistoryColumnValue(final String columnValue, final String columnName) {
		sleep(SLEEP_TIME);
		assertThat(selectItemInEditorPane(PAYMENT_TABLE_PARENT_CSS, PAYMENT_COLUMN_CSS, columnValue, columnName))
				.as("Unable to find payment history column value - " + columnValue)
				.isTrue();
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
				.as("Unable to find order sku - " + skuCode)
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
		clickEditorButton("Open Customer Profile...");
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
		clickEditorButton("Edit Shipping Address...");
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
		switch(record.getShipmentType()) {
			case "physical":
				webElement = String.format(ORDER_SHIPMENT_DETAIL_TABLE_CSS, "Physical Shipment");
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(BUNDLE_NAME_COLUMN_NAME), record.getBundleName());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(INVENTORY_COLUMN_NAME), record.getInventory());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_CODE_COLUMN_NAME), record.getSkuCode());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PRODUCT_NAME_COLUMN_NAME), record.getProductName());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_OPTION_COLUMN_NAME), record.getSkuOptions());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(LIST_PRICE_COLUMN_NAME), record.getListPrice());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SALE_PRICE_COLUMN_NAME), record.getSalePrice());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(QUANTITY_COLUMN_NAME), record.getQuantity());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(DISCOUNT_COLUMN_NAME), record.getDiscount());
				verifyShipmentTableLineItemRow(webElement, SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(TOTAL_PRICE_COLUMN_NAME), record.getTotalPrice());
				break;
			case "e-shipment":
				webElement = String.format(ORDER_SHIPMENT_DETAIL_TABLE_CSS, "Electronic Shipment");
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(BUNDLE_NAME_COLUMN_NAME), record.getBundleName());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_CODE_COLUMN_NAME), record.getSkuCode());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PRODUCT_NAME_COLUMN_NAME), record.getProductName());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_OPTION_COLUMN_NAME), record.getSkuOptions());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(LIST_PRICE_COLUMN_NAME), record.getListPrice());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SALE_PRICE_COLUMN_NAME), record.getSalePrice());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(QUANTITY_COLUMN_NAME), record.getQuantity());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(DISCOUNT_COLUMN_NAME), record.getDiscount());
				verifyShipmentTableLineItemRow(webElement, ELECTRONIC_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(TOTAL_PRICE_COLUMN_NAME), record.getTotalPrice());
				break;
			case "recurring":
				webElement = String.format(ORDER_SHIPMENT_DETAIL_TABLE_CSS, "Recurring Items");
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(BUNDLE_NAME_COLUMN_NAME), record.getBundleName());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_CODE_COLUMN_NAME), record.getSkuCode());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PRODUCT_NAME_COLUMN_NAME), record.getProductName());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SKU_OPTION_COLUMN_NAME), record.getSkuOptions());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(LIST_PRICE_COLUMN_NAME), record.getListPrice());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(SALE_PRICE_COLUMN_NAME), record.getSalePrice());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(QUANTITY_COLUMN_NAME), record.getQuantity());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(DISCOUNT_COLUMN_NAME), record.getDiscount());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(TOTAL_PRICE_COLUMN_NAME), record.getTotalPrice());
				verifyShipmentTableLineItemRow(webElement, RECURRING_SHIPMENT_TABLE_COLUMN_HEADER_VALUES.indexOf(PAYMENT_SCHEDULE_COLUMN_NAME), record.getPaymentSchedule());
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
			assertThat(getDriver().findElement(By.cssSelector(webElementCSS + "div[column-num='" + columnIndex + "']")).getText())
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
		assertThat(getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(SHIPMENT_SUMMARY_PRICE_VALUE_CSS, shipmentNumber, key)))
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
	 * Clicks Edit Billing Address... button.
	 *
	 * @return EditAddressDialog
	 */
	public AddEditCustomerAddressDialog clickEditBillingAddressButton() {
		clickEditorButton("Edit Address...");
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
	 * @param column		the column to set new value
	 * @param expectedValue	new value to be set
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
	public void verifyShipmentLineItemSkuCode(final String sku) { verifyShipmentLineItemRow("SKU Code", sku); }

	/**
	 * Verify the value of Sale Price column in a particular row of the Items table of a Shipment.
	 *
	 * @param salePrice the price expected.
	 */
	public void verifyShipmentLineItemSalePrice(final String salePrice) {
		verifyShipmentLineItemRow("Sale Price", salePrice);
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
		verifyShipmentLineItemRow("Total Price", totalPrice);
	}

	/**
	 *  Set new quantity for the order shipment line item
	 *
	 * @param quantity new line item quantity
	 */
	public void setShipmentLineItemQuantity(final String quantity) { setShipmentLineItemRowValue("Qty", quantity); }

	/**
	 *  Set order shipment line item discount
	 *
	 * @param discount discount amount to be set
	 */
	public void setShipmentLineItemDiscount(final String discount) { setShipmentLineItemRowValue("Discount", discount); }

	/**
	 *  Set new value in order Less Shipment Discount field
	 *
	 * @param discount new value to be set
	 */
	public void setLessShipmentDiscountValue(final String discount) {
		enterNewValueForField(SHIPMENT_SUMMARY_SHIPMENT_DISCOUNT_CSS, discount);
	}

	/**
	 *  Set new value in order Shipping Cost field
	 *
	 * @param cost new value to be set
	 */
	public void setShippingCostValue(final String cost) {
		enterNewValueForField(SHIPMENT_SUMMARY_SHIPMENT_COST_CSS, cost);
	}

	/**
	 *  Set provided value into specified by CSS WebElement
	 *
	 * @param fieldCSS CSS value for WebElement to be modified
	 * @param newValue new value to be set
	 */
	private void enterNewValueForField(final String fieldCSS, final String newValue) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(fieldCSS));
		clearAndTypeNonJSCheck(fieldCSS, newValue);
	}
}
