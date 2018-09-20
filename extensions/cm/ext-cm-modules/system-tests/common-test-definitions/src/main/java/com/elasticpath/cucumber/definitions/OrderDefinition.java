package com.elasticpath.cucumber.definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.log4j.Logger;

import com.elasticpath.cucumber.util.CortexMacrosTestBase;
import com.elasticpath.selenium.dialogs.AddCustomerAddressDialog;
import com.elasticpath.selenium.dialogs.CompleteShipmentDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditItemDetailsDialog;
import com.elasticpath.selenium.dialogs.MoveItemDialog;
import com.elasticpath.selenium.dialogs.SelectASkuDialog;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.editor.CustomerEditor;
import com.elasticpath.selenium.editor.OrderEditor;
import com.elasticpath.selenium.editor.RmaEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.CustomerService;
import com.elasticpath.selenium.navigations.ShippingReceiving;
import com.elasticpath.selenium.resultspane.OrderSearchResultPane;
import com.elasticpath.selenium.resultspane.RmaSearchResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.CustomerServiceActionToolbar;
import com.elasticpath.selenium.toolbars.ShippingReceivingActionToolbar;
import com.elasticpath.selenium.wizards.CompleteReturnWizard;
import com.elasticpath.selenium.wizards.CreateExchangeWizard;
import com.elasticpath.selenium.wizards.CreateRefundWizard;
import com.elasticpath.selenium.wizards.CreateReturnWizard;
import com.elasticpath.selenium.wizards.PaymentAuthorizationWizard;

/**
 * Order step definitions.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields"})
public class OrderDefinition {

	private static final String ORDER_DETAILS_TAB = "Details";
	private static final String CUSTOMERNAME_COLUUMNNAME = "Customer Name";
	private static final String ORDER_STATUS_COLUMNNAME = "Order Status";
	private static final String ORDER_NUMBER_COLUMNNAME = "Order #";
	private static final Logger LOGGER = Logger.getLogger(OrderDefinition.class);
	private final CustomerService customerService;
	private final ActivityToolbar activityToolbar;
	private final ShippingReceivingActionToolbar shippingReceivingActionToolbar;
	private final CustomerServiceActionToolbar customerServiceActionToolbar;
	private final Product product;
	private ShippingReceiving shippingReceiving;
	private OrderSearchResultPane orderSearchResultPane;
	private RmaSearchResultPane rmaSearchResultPane;
	private OrderEditor orderEditor;
	private RmaEditor rmaEditor;
	private CompleteReturnWizard completeReturnWizard;
	private CustomerEditor customerEditor;
	private CreateRefundWizard createRefundWizard;
	private CreateReturnWizard createReturnWizard;
	private CreateExchangeWizard createExchangeWizard;
	private AddCustomerAddressDialog addCustomerAddressDialog;
	private CompleteShipmentDialog completeShipmentDialog;
	private MoveItemDialog moveItemDialog;
	private SelectASkuDialog selectASkuDialog;
	private ConfirmDialog confirmDialog;
	private EditItemDetailsDialog editItemDetailsDialog;
	private PaymentAuthorizationWizard paymentAuthorizationWizard;
	private String exchangeOrderNumber;
	private String addressNameToCheck;
	private String addressPhoneToCheck;

	/**
	 * Constructor.
	 *
	 * @param product Product.
	 */
	public OrderDefinition(final Product product) {
		activityToolbar = new ActivityToolbar((SeleniumDriverSetup.getDriver()));
		shippingReceivingActionToolbar = new ShippingReceivingActionToolbar(SeleniumDriverSetup.getDriver());
		customerService = new CustomerService(SeleniumDriverSetup.getDriver());
		customerServiceActionToolbar = new CustomerServiceActionToolbar(SeleniumDriverSetup.getDriver());
		this.product = product;
	}

	/**
	 * Search order by number.
	 *
	 * @param orderNum the order number.
	 */
	@When("^I search for an order by number (.+)$")
	public void searchOrderByNumber(final String orderNum) {
		customerService.enterOrderNumber(orderNum);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by email.
	 *
	 * @param email the email.
	 */
	@When("^I search for orders by email (.+)$")
	public void searchOrderByEmail(final String email) {
		customerService.enterEmailUserID(email);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search latest order by email.
	 */
	@When("^I search for the latest orders by email$")
	public void searchLatestByEmail() {
		searchAndOpenLatestOrderEditor();
		customerService.clearInputFields();
		customerService.enterEmailUserID(orderEditor.getCustomerEmail());
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Verify order number in search results pane.
	 *
	 * @param orderNumber the order number.
	 */
	@Then("^I should see the order number (.+) in search results pane$")
	public void verifyOrderNumberInSearchResultsPane(final String orderNumber) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(orderNumber, ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Verifies latest order in search results pane.
	 */
	@Then("^I should see the latest order in results pane$")
	public void verifyLatestOrderInResultsPane() {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(CortexMacrosTestBase.PURCHASE_NUMBER, ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Verify customer name in search results pane.
	 *
	 * @param orderNumber the order number.
	 */
	@Then("^I should see customer name (.+) in search results pane$")
	public void verifyCustomerNameInSearchResultsPane(final String orderNumber) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(orderNumber, CUSTOMERNAME_COLUUMNNAME);
	}

	/**
	 * Open order editor.
	 *
	 * @param orderStatus the order status.
	 */
	@And("^I open the order editor for (?:a|an) (.+) order$")
	public void openOrderEditor(final String orderStatus) {
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(orderStatus, ORDER_STATUS_COLUMNNAME);
	}

	/**
	 * Cancel order.
	 */
	@And("^I cancel the order$")
	public void cancelOrder() {
		confirmDialog = orderEditor.clickCancelOrderButton();
		confirmDialog.clickOKButton("FulfillmentMessages.OrderSummaryOverviewSection_DialogCancel");
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Cancel shipment.
	 */
	@And("^I cancel the shipment")
	public void cancelShipment() {
		confirmDialog = orderEditor.clickCancelShipmentButton();
		confirmDialog.clickOKButton("FulfillmentMessages.ShipmentSection_CancelShipment");
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Click Create Return button.
	 */
	@And("^I click create return button")
	public void clickCreateReturnButton() {
		createReturnWizard = orderEditor.clickCreateReturnButton();
	}

	/**
	 * Completes the order.
	 *
	 */
	@And("^I (?:can complete|complete) the order shipment")
	public void completeOrderShipment() {
		selectOrderEditorTab(ORDER_DETAILS_TAB);
		orderEditor.clickReleaseShipmentButton();
		activityToolbar.clickShippingReceivingButton();
		completeShipmentDialog = shippingReceivingActionToolbar.clickCompleteShipmentButton();
		completeShipmentDialog.completeShipment(CortexMacrosTestBase.PURCHASE_NUMBER + "-1");
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify order status.
	 *
	 * @param orderStatus the order status
	 */
	@And("^the order status should be (.+)$")
	public void verifyOrderStatus(final String orderStatus) {
		orderEditor.verifyOrderStatus(orderStatus);
	}

	/**
	 * Verify shipment status.
	 *
	 * @param shipmentStatus the shipment status.
	 */
	@And("^the shipment status should be (.+)$")
	public void verifyShipmentStatus(final String shipmentStatus) {
		orderEditor.verifyShipmentStatus(shipmentStatus);
	}

	/**
	 * Select order editor tab.
	 *
	 * @param tabName the tab name.
	 */
	@When("^I (?:select|can select) (.+) tab in the Order Editor$")
	public void selectOrderEditorTab(final String tabName) {
		orderEditor.clickTab(tabName);
	}

	/**
	 * Verify Transaction Type.
	 *
	 * @param transactionType the transaction type.
	 */
	@Then("^I should see transaction type (.+) in the Payment History$")
	public void verifyTransactionType(final String transactionType) {
		orderEditor.verifyTransactionType(transactionType);
	}

	/**
	 * Clear search results window.
	 *
	 * @param tabName the tab name.
	 */
	@When("^I close the window (.+)")
	public void clearSearchResults(final String tabName) {
		orderSearchResultPane.close(tabName);
	}

	/**
	 * Clear input fields.
	 */
	@When("^I clear the input fields$")
	public void clearInputFields() {
		customerService.clearInputFields();
	}

	/**
	 * Searches order and opens order editor.
	 */
	@When("^I (?:search|can search) and open order editor for the latest order$")
	public void searchAndOpenLatestOrderEditor() {
		String orderNumber = CortexMacrosTestBase.PURCHASE_NUMBER;
		LOGGER.info("ordernumber.... " + orderNumber);
		customerService.enterOrderNumber(orderNumber);
		orderSearchResultPane = customerService.clickOrderSearch();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(orderNumber, ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Searches order by number.
	 */
	@When("^I search the latest order by number$")
	public void searchOrderByNumber() {
		String orderNumber = CortexMacrosTestBase.PURCHASE_NUMBER;
		LOGGER.info("searching for ordernumber.... " + orderNumber);
		customerService.enterOrderNumber(orderNumber);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Creates refund.
	 *
	 * @param refundMap refund item values
	 */
	@When("^I create a refund with following values$")
	public void createRefund(final Map<String, String> refundMap) {
		orderEditor.closePane("#" + CortexMacrosTestBase.PURCHASE_NUMBER);
		activityToolbar.clickCustomerServiceButton();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(CortexMacrosTestBase.PURCHASE_NUMBER, ORDER_NUMBER_COLUMNNAME);
		createRefundWizard = orderEditor.clickCreateRefundButton();
		createRefundWizard.createRefund(refundMap);
	}

	/**
	 * Creates exchange.
	 *
	 * @param exchangeMap exchange item values
	 */
	@When("^I create a exchange with following values$")
	public void createExchange(final Map<String, String> exchangeMap) {
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		createExchangeWizard = orderEditor.clickCreateExchangeButton();
		createExchangeWizard.createExchange(exchangeMap);
		shippingReceivingActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify sku code.
	 *
	 * @param skuCode the sku code.
	 */
	@Then("^I should see the returned sku code (.+)$")
	public void verifyReturnedSkuCode(final String skuCode) {
		orderEditor.verifyReturnSkuCode(skuCode);
	}

	/**
	 * Verifies exchange order number is present.
	 */
	@Then("^I should see exchange order number$")
	public void verifyExchangeOrderNumberIsPresent() {
		exchangeOrderNumber = orderEditor.verifyExchangeOrderNumberIsPresent();
		LOGGER.info("exchange order number: " + exchangeOrderNumber);
	}

	/**
	 * Opens exchange order editor.
	 */
	@When("^I open the exchange order editor$")
	public void openExchangeOrderEditor() {
		orderEditor.clickOpenExchangeOrderButton();
	}

	/**
	 * Verifies original and exchange order number.
	 */
	@When("^I should see the original order\\# as External Order\\# and exchange order\\# as Order\\#$")
	public void verifyOrderNumbers() {
		orderEditor.verifyOriginalAndExchangeOrderNumbers(CortexMacrosTestBase.PURCHASE_NUMBER, exchangeOrderNumber);
	}

	/**
	 * Verifies sku present in the list.
	 *
	 * @param skuCodeList list of order skus
	 */
	@When("^I should see the following skus? in item list$")
	public void verifySkuCodePresentInList(final List<String> skuCodeList) {
		for (String skuCode : skuCodeList) {
			orderEditor.verifyAndSelectOrderSkuCode(skuCode);
		}
	}

	/**
	 * Verifies the order contains newly created product sku.
	 */
	@Then("^the order contains the newly created product$")
	public void verfiyNewlyCreatedSkuInOrder() {
		selectOrderEditorTab(ORDER_DETAILS_TAB);
		List<String> skuCodeList = new ArrayList<>();
		skuCodeList.add(this.product.getSkuCode());
		verifySkuCodePresentInList(skuCodeList);
	}

	/**
	 * Verifies sku code is not in the list.
	 *
	 * @param skuCodeList list of order skus
	 */
	@When("^I should not see the following skus? in item list$")
	public void verifySkuCodeIsNotPresentInList(final List<String> skuCodeList) {
		for (String skuCode : skuCodeList) {
			orderEditor.verifySkuCodeIsNotInList(skuCode);
		}
	}

	/**
	 * Creates a new shipment.
	 *
	 * @param skuCode            the sku code
	 * @param newShipmentInfoMap new shipment values
	 */
	@And("^I create a new shipment for sku (.+) with following values$")
	public void createNewShipment(final String skuCode, final Map<String, String> newShipmentInfoMap) {
		orderEditor.verifyAndSelectOrderSkuCode(skuCode);
		moveItemDialog = orderEditor.clickMoveItemButton();
		moveItemDialog.moveItem(newShipmentInfoMap.get("Address"), newShipmentInfoMap.get("Shipment Method"));
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.completePaymentAuthorization(newShipmentInfoMap.get("Payment Source"));
	}

	/**
	 * Adds an item to the shipment.
	 *
	 * @param skuCode        the sku code
	 * @param addItemInfoMap item values
	 */
	@When("^I add sku (.+) to the shipment with following values$")
	public void addItemToShipment(final String skuCode, final Map<String, String> addItemInfoMap) {
		selectASkuDialog = orderEditor.clickAddItemButton();
		selectASkuDialog.selectSkuAndPriceList(skuCode, addItemInfoMap.get("Price List Name"));
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.completePaymentAuthorization(addItemInfoMap.get("Payment Source"));
		customerServiceActionToolbar.clickReloadActiveEditor();
	}


	/**
	 * Removes an item from shipment.
	 *
	 * @param skuCode the sku code
	 */
	@And("^I remove sku (.+) from the shipment$")
	public void addItemToShipment(final String skuCode) {
		orderEditor.verifyAndSelectOrderSkuCode(skuCode);
		orderEditor.clickRemoveItemButton();
		customerServiceActionToolbar.clickSaveAllButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verifies number of shipments.
	 *
	 * @param numberOfShipments number of shipments
	 */
	@And("^I should see (\\d+) shipments?$")
	public void verifyNumberOfShipments(final int numberOfShipments) {
		for (int i = 0; i < numberOfShipments; i++) {
			String shipmentNumber = CortexMacrosTestBase.PURCHASE_NUMBER + "-" + (i + 1);
			LOGGER.info("verifying shipment #: " + shipmentNumber);
			orderEditor.verifyShipmentNumber(shipmentNumber);
		}
	}

	/**
	 * Creates return for digital item.
	 *
	 * @param quantity the quantity
	 * @param skuCode  the sku code
	 */
	@And("^I create digital item return with quantity (\\d+) for sku (.+)$")
	public void createReturnForDigital(final int quantity, final String skuCode) {
		searchAndOpenLatestOrderEditor();
		selectOrderEditorTab(ORDER_DETAILS_TAB);
		clickCreateReturnButton();
		createReturnWizard.createDigitalReturn(quantity, skuCode);
	}

	/**
	 * Creates return for physical item.
	 *
	 * @param quantity the quantity
	 * @param skuCode  the sku code
	 */
	@And("^I create physical item return with quantity (\\d+) for sku (.+)$")
	public void createReturnForPhysical(final int quantity, final String skuCode) {
		searchAndOpenLatestOrderEditor();
		selectOrderEditorTab(ORDER_DETAILS_TAB);
		completeOrderShipment();
		clickCreateReturnButton();
		createReturnWizard.createPhysicalReturn(quantity, skuCode);
	}

	/**
	 * Opens customer editor.
	 *
	 * @param tabName the tab name.
	 */
	@When("^I open Customer Profile (.+) tab$")
	public void openCustomerProfileEditor(final String tabName) {
		customerEditor = orderEditor.clickOpenCustomerProfileButton();
		customerEditor.clickTab(tabName);
	}

	/**
	 * Opens customer editor.
	 */
	@When("^I open Customer Profile$")
	public void openCustomerProfileEditor() {
		customerEditor = orderEditor.clickOpenCustomerProfileButton();
	}

	/**
	 * Verifies current order number in customer profile order tab.
	 */
	@When("^I should see the latest order details$")
	public void verifyCustomerProfileOrderId() {
		customerEditor.verifyCustomerOrderIdColumnValue(CortexMacrosTestBase.PURCHASE_NUMBER, "Order ID");
	}

	/**
	 * Edits customer name.
	 *
	 * @param firstName New customer first name
	 * @param lastName  New customer last name
	 */
	@When("^I update first name (.+) and last name (.+) for the customer$")
	public void enterNewCustomerName(final String firstName, final String lastName) {
		customerEditor.enterFirstName(firstName);
		customerEditor.enterLastName(lastName);
		customerServiceActionToolbar.clickSaveButton();
	}

	/**
	 * Verifies updated customer name.
	 *
	 * @param firstName New customer first name
	 * @param lastName  New customer last name
	 */
	@Then("^I should see the updated first name (.+) and last name (.+)$")
	public void verifyUpdatedCustomerName(final String firstName, final String lastName) {
		customerEditor.verifyFirstName(firstName);
		customerEditor.verifyLastName(lastName);
	}

	/**
	 * Clicks Add Address button.
	 */
	@Then("^I click Add Address button$")
	public void clickAddAddressButton() {
		addCustomerAddressDialog = customerEditor.clickAddAddressButton();
	}

	/**
	 * Enter a new customer address.
	 *
	 * @param addressMap the address map
	 */
	@When("^I add new address with the following values$")
	public void fillInCustomerAddressDialog(final Map<String, String> addressMap) {
		this.addressNameToCheck = addressMap.get("first name") + " " + addressMap.get("last name");
		this.addressPhoneToCheck = addressMap.get("phone");
		addCustomerAddressDialog.enterFirstName(addressMap.get("first name"));
		addCustomerAddressDialog.enterLastName(addressMap.get("last name"));
		addCustomerAddressDialog.enterAddressLine1(addressMap.get("address line 1"));
		addCustomerAddressDialog.enterCity(addressMap.get("city"));
		addCustomerAddressDialog.selectState(addressMap.get("state"));
		addCustomerAddressDialog.enterZip(addressMap.get("zip"));
		addCustomerAddressDialog.selectCountry(addressMap.get("country"));
		addCustomerAddressDialog.enterPhone(addressMap.get("phone"));
		addCustomerAddressDialog.clickSave();
		customerServiceActionToolbar.clickSaveButton();
	}

	/**
	 * Verify new address exists.
	 */
	@Then("^the new address should exist in the address list$")
	public void verifyNewAddressExists() {
		//Check name and phone to avoid country dependant address format issues
		customerEditor.verifyAddressLineExists(this.addressNameToCheck, "Name");
		customerEditor.verifyAddressLineExists(this.addressPhoneToCheck, "Phone Number");
	}

	/**
	 * Opens item detail.
	 *
	 * @param skuCode skucode.
	 */
	@When("^I view item detail of the order line item (.*)$")
	public void openItemDetail(final String skuCode) {
		editItemDetailsDialog = orderEditor.clickItemDetailButton(skuCode);
	}

	/**
	 * Verifies configurable fields values against the purchase.
	 *
	 * @param configurableFieldsMap configurableFieldsMap.
	 */
	@Then("^the item detail matches the configurable field values from the purchase$")
	public void verifyConfigurableFieldsValues(final Map<String, String> configurableFieldsMap) {
		editItemDetailsDialog.verifyConfigurableFieldValues(configurableFieldsMap);


	}

	/**
	 * * Returned sku is received.
	 *
	 * @param receivedQuantity String
	 * @param receivedSku      String
	 */
	@When("^shipping receive return is processed for quantity (.+) of sku (.+)$")
	public void receiveReturnForSku(final String receivedQuantity, final String receivedSku) {
		shippingReceiving = activityToolbar.clickShippingReceivingButton();
		shippingReceiving.clickReturnsTab();
		String orderNumber = CortexMacrosTestBase.PURCHASE_NUMBER;
		shippingReceiving.enterOrderNumber(orderNumber);
		rmaSearchResultPane = shippingReceiving.clickReturnsSearch();
		rmaEditor = rmaSearchResultPane.selectOrderAndOpenRmaEditor(orderNumber, ORDER_NUMBER_COLUMNNAME);
		rmaEditor.setReturnedQuantity(receivedSku, receivedQuantity);
		customerServiceActionToolbar.clickSaveButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Completes a return.
	 */
	@When("^I complete the return$")
	public void completeReturn() {
		orderEditor = rmaEditor.clickOpenOriginalOrderButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
		orderEditor.clickTab("Returns and Exchanges");
		completeReturnWizard = orderEditor.clickCompleteReturnButton();
		completeReturnWizard.completeReturn();
	}

	/**
	 * Verifies received quantity.
	 *
	 * @param recQuantity String
	 */
	@Then("^the item received quantity shows as (.+)$")
	public void verifyReceivedQuantity(final String recQuantity) {
		orderEditor.verifyReturnedSkuColumnValue(recQuantity, "Rec Qty");
	}
}