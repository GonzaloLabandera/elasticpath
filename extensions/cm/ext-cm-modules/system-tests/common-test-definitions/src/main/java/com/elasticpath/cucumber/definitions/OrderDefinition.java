package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.log4j.Logger;

import com.elasticpath.cortexTestObjects.Purchase;
import com.elasticpath.selenium.dialogs.AddEditCustomerAddressDialog;
import com.elasticpath.selenium.dialogs.CompleteShipmentDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditItemDetailsDialog;
import com.elasticpath.selenium.dialogs.MoveItemDialog;
import com.elasticpath.selenium.dialogs.SelectASkuDialog;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.Shipment;
import com.elasticpath.selenium.domainobjects.ShipmentTableRecord;
import com.elasticpath.selenium.editor.CustomerEditor;
import com.elasticpath.selenium.editor.OrderEditor;
import com.elasticpath.selenium.editor.RmaEditor;
import com.elasticpath.selenium.navigations.CustomerService;
import com.elasticpath.selenium.navigations.ShippingReceiving;
import com.elasticpath.selenium.resultspane.OrderSearchResultPane;
import com.elasticpath.selenium.resultspane.RmaSearchResultPane;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.CatalogManagementActionToolbar;
import com.elasticpath.selenium.toolbars.CustomerServiceActionToolbar;
import com.elasticpath.selenium.toolbars.ShippingReceivingActionToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.CompleteReturnWizard;
import com.elasticpath.selenium.wizards.CreateExchangeWizard;
import com.elasticpath.selenium.wizards.CreateRefundWizard;
import com.elasticpath.selenium.wizards.CreateReturnWizard;
import com.elasticpath.selenium.wizards.PaymentAuthorizationWizard;


/**
 * Order step definitions.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength"})
public class OrderDefinition {

	private static final String ORDER_DETAILS_TAB = "Details";
	private static final String CUSTOMERNAME_COLUUMNNAME = "Customer Name";
	private static final String ORDER_NUMBER_COLUMNNAME = "Order #";
	private static final String ORDER_STORE_COLUUMNNAME = "Store";
	private static final String ORDER_PHONE_FIELD = "phone";
	private static final Logger LOGGER = Logger.getLogger(OrderDefinition.class);
	private final CustomerService customerService;
	private final ActivityToolbar activityToolbar;
	private final ShippingReceivingActionToolbar shippingReceivingActionToolbar;
	private final CustomerServiceActionToolbar customerServiceActionToolbar;
	private final CatalogManagementActionToolbar catalogManagementActionToolbar;
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
	private AddEditCustomerAddressDialog addEditCustomerAddressDialog;
	private CompleteShipmentDialog completeShipmentDialog;
	private MoveItemDialog moveItemDialog;
	private SelectASkuDialog selectASkuDialog;
	private ConfirmDialog confirmDialog;
	private EditItemDetailsDialog editItemDetailsDialog;
	private PaymentAuthorizationWizard paymentAuthorizationWizard;
	private String exchangeOrderNumber;
	private String addressNameToCheck;
	private String addressPhoneToCheck;
	private static final String PAYMENT_SOURCE = "Payment Source";

	/**
	 * Constructor.
	 *
	 * @param product Product.
	 */
	public OrderDefinition(final Product product) {
		activityToolbar = new ActivityToolbar((SetUp.getDriver()));
		shippingReceivingActionToolbar = new ShippingReceivingActionToolbar(SetUp.getDriver());
		customerService = new CustomerService(SetUp.getDriver());
		customerServiceActionToolbar = new CustomerServiceActionToolbar(SetUp.getDriver());
		catalogManagementActionToolbar = new CatalogManagementActionToolbar(SetUp.getDriver());
		this.product = product;
	}

	/**
	 * Search order by number.
	 *
	 * @param orderNum the order number.
	 */
	@When("^I search for an order by number (.+)$")
	public void searchOrderByNumber(final String orderNum) {
		assertThat(null == orderNum || orderNum.isEmpty()).as("OrderNumber is null or empty because order was not created successfully")
				.isFalse();
		customerService.enterOrderNumber(orderNum);
		orderSearchResultPane = customerService.clickOrderSearch();

		int index = 0;
		while (!orderSearchResultPane.isOrderInList(orderNum, "Order #") && index < Constants.UUID_END_INDEX) {
			orderSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			customerService.clickOrderSearch();
			index++;
		}
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
		customerService.clearInputFieldsInOrdersTab();
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
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Verify customer name in search results pane.
	 *
	 * @param customerName the customer name.
	 */
	@Then("^I should see customer name (.+) in search results pane$")
	public void verifyCustomerNameInSearchResultsPane(final String customerName) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(customerName, CUSTOMERNAME_COLUUMNNAME);
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
		int counter = 0;
		boolean isButtonInViewPort = orderEditor.isCreateReturnButtonInViewport();
		while (!isButtonInViewPort && counter < Constants.RETRY_COUNTER_3) {
			isButtonInViewPort = orderEditor.isCreateReturnButtonInViewport();
			counter++;
		}

		assertThat(isButtonInViewPort)
				.as("Create Return buttton is not in view")
				.isTrue();

		createReturnWizard = orderEditor.clickCreateReturnButton();
	}

	/**
	 * Completes the order.
	 */
	@And("^I (?:can complete|complete) the order shipment")
	public void completeOrderShipment() {
		selectOrderEditorTab(ORDER_DETAILS_TAB);

		int counter = 0;
		while (!orderEditor.isReleaseShipmentButtonInViewport() && counter < Constants.RETRY_COUNTER_3) {
			counter++;
		}

		orderEditor.clickReleaseShipmentButton();
		activityToolbar.clickShippingReceivingButton();
		completeShipmentDialog = shippingReceivingActionToolbar.clickCompleteShipmentButton();
		completeShipmentDialog.completeShipment(Purchase.getPurchaseNumber() + "-1");
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
	 * Close order search results tab.
	 */
	@And("^I close order search results tab")
	public void closeOrderSearchResultsTab() {
		orderSearchResultPane.closeOrderSearchResultsPane();
	}

	/**
	 * Clear input fields.
	 */
	@When("^I clear the input fields in customers tab$")
	public void clearInputFieldsInCustomersTab() {
		customerService.clearInputFieldsInCustomersTab();
	}

	/**
	 * Clear input fields.
	 */
	@When("^I clear the input field in orders tabs$")
	public void clearInputFieldsInOrdersTab() {
		customerService.clearInputFieldsInOrdersTab();
	}

	/**
	 * Searches order and opens order editor.
	 */
	@When("^I (?:search|can search) and open order editor for the latest order$")
	public void searchAndOpenLatestOrderEditor() {
		customerService.enterOrderNumber(getLatestOrderNumber());
		orderSearchResultPane = customerService.clickOrderSearch();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(getLatestOrderNumber(), ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Searches order by number.
	 */
	@When("^I search the latest order by number$")
	public void searchOrderByNumber() {
		LOGGER.info("searching for ordernumber.... " + getLatestOrderNumber());
		searchOrderByNumber(getLatestOrderNumber());
	}

	/**
	 * Creates refund.
	 *
	 * @param refundMap refund item values
	 */
	@When("^I create a refund with following values$")
	public void createRefund(final Map<String, String> refundMap) {
		orderEditor.closePane("#" + Purchase.getPurchaseNumber());
		activityToolbar.clickCustomerServiceButton();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
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
		orderEditor.verifyOriginalAndExchangeOrderNumbers(Purchase.getPurchaseNumber(), exchangeOrderNumber);
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
	 * Verifies sku and quantity present in the list.
	 *
	 * @param shipmentItemMap shipment list of order skus
	 */
	@When("^I should see the following skus? and quantity in physical shipment list$")
	public void verifySkuCodeAndQuantityPresentInList(final Map<String, String> shipmentItemMap) {
		for (Map.Entry<String, String> entry : shipmentItemMap.entrySet()) {
			orderEditor.verifyPhysicalShipmentSkuCodeAndQuantity(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Verifies sku present in the E-shipment list.
	 *
	 * @param skuCodeList list of order skus
	 */
	@When("^I should see the following skus? in the E-shipment item list$")
	public void verifySkuCodePresentInEshipmentList(final List<String> skuCodeList) {
		for (String skuCode : skuCodeList) {
			orderEditor.verifyAndSelectOrderEshipmentSkuCode(skuCode);
		}
	}

	/**
	 * Verifies sku and quantity present in the E-shipment list.
	 *
	 * @param shipmentItemMap shipment list of order skus
	 */
	@When("^I should see the following skus? and quantity in e-shipment list$")
	public void verifySkuCodeAndQuantityPresentInEshipmentList(final Map<String, String> shipmentItemMap) {
		for (Map.Entry<String, String> entry : shipmentItemMap.entrySet()) {
			orderEditor.verifyEShipmentSkuCodeAndQuantity(entry.getKey(), entry.getValue());
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
		paymentAuthorizationWizard.completePaymentAuthorization(newShipmentInfoMap.get(PAYMENT_SOURCE));
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
		paymentAuthorizationWizard.completePaymentAuthorization(addItemInfoMap.get(PAYMENT_SOURCE));
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
			String shipmentNumber = Purchase.getPurchaseNumber() + "-" + (i + 1);
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
		customerEditor.verifyCustomerOrderIdColumnValue(Purchase.getPurchaseNumber(), "Order ID");
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
		addEditCustomerAddressDialog = customerEditor.clickAddAddressButton();
	}

	/**
	 * Enter a new customer address.
	 *
	 * @param addressMap the address map
	 */
	@When("^I add new address with the following values$")
	public void fillInCustomerAddressDialog(final Map<String, String> addressMap) {
		this.addressNameToCheck = addressMap.get("first name") + " " + addressMap.get("last name");
		this.addressPhoneToCheck = addressMap.get(ORDER_PHONE_FIELD);
		addEditCustomerAddressDialog.enterFirstName(addressMap.get("first name"));
		addEditCustomerAddressDialog.enterLastName(addressMap.get("last name"));
		addEditCustomerAddressDialog.enterAddressLine1(addressMap.get("address line 1"));
		addEditCustomerAddressDialog.enterCity(addressMap.get("city"));
		addEditCustomerAddressDialog.selectState(addressMap.get("state"));
		addEditCustomerAddressDialog.enterZip(addressMap.get("zip"));
		addEditCustomerAddressDialog.selectCountry(addressMap.get("country"));
		addEditCustomerAddressDialog.enterPhone(addressMap.get(ORDER_PHONE_FIELD));
		addEditCustomerAddressDialog.clickSave();
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
		String orderNumber = Purchase.getPurchaseNumber();
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

	/**
	 * Update Shipping Address for the Order.
	 *
	 * @param addressMap the address map
	 */
	@When("^I update and save the following shipping address for the order$")
	public void updateOrderShippingAddress(final Map<String, String> addressMap) {
		selectOrderEditorTab(ORDER_DETAILS_TAB);
		addEditCustomerAddressDialog = orderEditor.clickEditAddressButton();
		addEditCustomerAddressDialog.enterAddressLine1(addressMap.get("address line 1"));
		addEditCustomerAddressDialog.enterPhone(addressMap.get(ORDER_PHONE_FIELD));
		addEditCustomerAddressDialog.clickSave();
		customerServiceActionToolbar.clickSaveButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Update Shipping Address and Shipping Method for the Order.
	 *
	 * @param addressMap     the address map
	 * @param shippingMethod the new shipping method
	 */
	@When("^I update the shipping address and set \"(.+)\" shipping method for the order$")
	public void updateOrderShippingAddressAndMethod(final String shippingMethod, final Map<String, String> addressMap) {
		selectOrderEditorTab(ORDER_DETAILS_TAB);
		addEditCustomerAddressDialog = orderEditor.clickEditAddressButton();
		addEditCustomerAddressDialog.selectCountry(addressMap.get("Country"));
		addEditCustomerAddressDialog.selectState(addressMap.get("State/Province"));
		addEditCustomerAddressDialog.enterCity(addressMap.get("City"));
		addEditCustomerAddressDialog.enterZip(addressMap.get("Zip/Postal Code"));
		addEditCustomerAddressDialog.enterAddressLine1(addressMap.get("Address Line 1"));
		addEditCustomerAddressDialog.enterPhone(addressMap.get("Phone"));
		addEditCustomerAddressDialog.clickSave();
		orderEditor.selectShippingMethod(shippingMethod);
		customerServiceActionToolbar.clickSaveButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verify Unlock Order Button.
	 */
	@Then("^Unlock Order button is disabled$")
	public void verifyUnlockOrderButton() {
		orderEditor.verifyUnlockOrderIsNotEnabled();
	}

	@Then("^I should see the following Shipment Summary$")
	public void verifyShipmentSummary(final List<Map<String, String>> shipments) {
		List<Shipment> shipmentObjects = orderEditor.populateShipments(shipments);
		activityToolbar.clickReloadActiveEditor();
		for (Shipment shipment : shipmentObjects) {
			orderEditor.verifyShipmentSubtotal(shipment.getShipmentNumber(), shipment.getItemSubTotal());
			orderEditor.verifyShipmentDiscount(shipment.getShipmentNumber(), shipment.getLessShipmentDiscount());
			orderEditor.verifyShipmentItemTaxes(shipment.getShipmentNumber(), shipment.getItemTaxes());
			orderEditor.verifyShipmentTotal(shipment.getShipmentNumber(), shipment.getShipmentTotal());

			if (!shipment.getShipmentNumber().equals("E-shipment")) {
				orderEditor.verifyShipmentShippingCost(shipment.getShipmentNumber(), shipment.getShippingCost());
				orderEditor.verifyShipmentTotalBeforeTax(shipment.getShipmentNumber(), shipment.getTotalBeforeTax());
				orderEditor.verifyShipmentShippingTaxes(shipment.getShipmentNumber(), shipment.getShippingTaxes());
			}
		}
	}

	/**
	 * Verify Shipping cost with Promotion Applied in order details.
	 *
	 * @param promotionName promotion name
	 */
	@Then("^I should see the applied promotion of (.+) in the order details$")
	public void verifyPromotionTable(final String promotionName) {
		orderEditor.verifyPromotionColumnValue(promotionName, "Promotion Name");
	}

	/**
	 * Search order by First Name.
	 *
	 * @param firstName the First Name.
	 */
	@When("^I search for orders by First Name (.+)$")
	public void searchOrderByFirstName(final String firstName) {
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterFirstName(firstName);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by Last Name.
	 *
	 * @param lastName the Last Name.
	 */
	@When("^I search for orders by Last Name (.+)$")
	public void searchOrderByLastName(final String lastName) {
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterLastName(lastName);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by Postal Code/ZIP.
	 *
	 * @param postalCode the Postal Code.
	 */
	@When("^I search for latest orders by Postal Code (.+)$")
	public void searchOrderByPostalCode(final String postalCode) {
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterOrderNumber(getLatestOrderNumber());
		customerService.enterPostalCode(postalCode);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by Phone Number.
	 *
	 * @param phoneNumber the Phone Number.
	 */
	@When("^I search for orders by Phone Number (.+)$")
	public void searchOrderByPhoneNumber(final String phoneNumber) {
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterPhoneNumber(phoneNumber);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by store.
	 *
	 * @param storeName the Store.
	 */
	@When("^I search for latest order by Store (.+)$")
	public void searchOrderByStore(final String storeName) {
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterOrderNumber(getLatestOrderNumber());
		customerService.selectStore(storeName);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by Order Status.
	 *
	 * @param orderStatus the Order status.
	 */
	@When("^I search for latest order by Order Status (.+)$")
	public void searchLatestOrderByStatus(final String orderStatus) {
		closeOrderSearchResultPaneIfOpen();
		customerService.clearInputFieldsInOrdersTab();

		customerService.enterOrderNumber(getLatestOrderNumber());

		customerService.selectStatus(orderStatus);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Verify Order Store Column in search results pane.
	 *
	 * @param orderStore the Order Store Column.
	 */
	@Then("^I should see Order with Store (.+) in search results pane$")
	public void verifyOrderStoreNameInSearchResultsPane(final String orderStore) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(orderStore, ORDER_STORE_COLUUMNNAME);
	}


	/**
	 * Verify Order Status Column in search results pane.
	 *
	 * @param orderStatus the Order Status Column.
	 */
	@Then("^I should see Order with Order Status - (.+) in search results pane$")
	public void verifyOrderStatusNameInSearchResultsPane(final String orderStatus) {
		orderSearchResultPane.verifyOrderStatusExistInResult(orderStatus);
	}

	/**
	 * Update Billing Address for the Order.
	 *
	 * @param addressMap the address map
	 */
	@When("^I update and save the following billing address for the order$")
	public void updateOrderBillingAddress(final Map<String, String> addressMap) {
		addEditCustomerAddressDialog = orderEditor.clickEditBillingAddressButton();
		addEditCustomerAddressDialog.enterPhone(addressMap.get(ORDER_PHONE_FIELD));
		addEditCustomerAddressDialog.clickSave();
		customerServiceActionToolbar.clickSaveButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Gets the latest order number.
	 *
	 * @return orderNumber.
	 */
	private String getLatestOrderNumber() {
		String orderNumber = Purchase.getPurchaseNumber();
		LOGGER.info("ordernumber.... " + orderNumber);
		assertThat(null == orderNumber || orderNumber.isEmpty())
				.as("OrderNumber is null or empty because order was not created successfully")
				.isFalse();
		return orderNumber;
	}

	/**
	 * Search order by Order Shipment Status.
	 *
	 * @param orderShipmentStatus the Order Shipment status.
	 */
	@When("^I search for latest order by Order Shipment Status (.+)$")
	public void searchOrderByShipmentStatus(final String orderShipmentStatus) {
		closeOrderSearchResultPaneIfOpen();
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterOrderNumber(getLatestOrderNumber());
		customerService.selectShipmentStatus(orderShipmentStatus);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by SKU Code.
	 *
	 * @param skuCode the SKU Code .
	 */
	@When("^I search for latest order by SKU Code (.+)$")
	public void searchOrderBySkuCode(final String skuCode) {
		closeOrderSearchResultPaneIfOpen();
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterOrderNumber(getLatestOrderNumber());
		customerService.enterSkuCode(skuCode);
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Search order by Dates.
	 */
	@When("^I search for order by Dates between current date and next date$")
	public void searchOrderByDateRange() {
		closeOrderSearchResultPaneIfOpen();
		customerService.clearInputFieldsInOrdersTab();
		customerService.enterOrderNumber(getLatestOrderNumber());
		customerService.enterFromDate();
		customerService.enterToDate();
		orderSearchResultPane = customerService.clickOrderSearch();
	}

	/**
	 * Verify Order Date Range
	 */
	@Then("^I should see Orders search results are within Date Range$")
	public void verifyOrderSearchResultDateRange() {
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
		orderEditor.verifyOrderSearchResultDateRange();
	}

	/**
	 * Verify Order Postal Code.
	 *
	 * @param postalCode the Postal Code.
	 */
	@Then("^I should see Order with Postal Code (.+) in search results pane$")
	public void verifySearchResultOrderPostalCode(final String postalCode) {
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
		addEditCustomerAddressDialog = orderEditor.clickEditBillingAddressButton();
		addEditCustomerAddressDialog.verifyPostalCode(postalCode);
		addEditCustomerAddressDialog.clickCancel();
	}

	/**
	 * Verify Order Phone Number.
	 *
	 * @param phoneNumber the Phone Number.
	 */
	@Then("^I should see Order with Phone Number (.+) in search results pane$")
	public void verifySearchResultOrderPhoneNumber(final String phoneNumber) {
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
		orderEditor.verifyPhoneNumber(phoneNumber);
	}

	/**
	 * Verify Order shipment status.
	 *
	 * @param orderShipmentStatus the Order status.
	 */
	@Then("^I should see Order with Shipment Status (.+) in search results pane$")
	public void verifySearchResultOrderShipmentStatus(final String orderShipmentStatus) {
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
		orderEditor.clickTab(ORDER_DETAILS_TAB);
		orderEditor.verifyShipmentStatus(orderShipmentStatus);
	}

	/**
	 * Verify Order Sku Code.
	 *
	 * @param orderSkuCode the Order sku code.
	 */
	@Then("^I should see Order with SKU Code (.+) in search results pane$")
	public void verifySearchResultOrderSkuDetails(final String orderSkuCode) {
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
		orderEditor.clickTab(ORDER_DETAILS_TAB);
		orderEditor.verifyAndSelectOrderSkuCode(orderSkuCode);
	}

	/**
	 * Update Shipping Method with Payment Auth.
	 *
	 * @param newShipmentInfoMap new shipment values
	 */
	@When("^I change the Shipping Method to the following$")
	public void updateShippingMethod(final Map<String, String> newShipmentInfoMap) {
		updateShippingMethodWithShipmentNumberAndPaymentAuth(1, newShipmentInfoMap);
	}

	/**
	 * Update Shipping Method without Payment Auth.
	 *
	 * @param shippingMethod the new shipment method
	 */
	@When("^I change the Shipping Method to (.+) without authorizing payment$")
	public void updateShippingMethodWithoutAuth(final String shippingMethod) {
		updateShippingMethodWithShipmentNumberAndWithoutAuth(1, shippingMethod);
	}

	/**
	 * Update Shipping Method with Shipment Number and Payment Auth.
	 *
	 * @param newShipmentInfoMap new shipment values
	 */
	@When("^I change the shipment number (\\d+) Shipping Method to the following$")
	public void updateShippingMethodWithShipmentNumberAndPaymentAuth(final int shipmentNumber, final Map<String, String> newShipmentInfoMap) {
		updateShippingMethodWithShipmentNumberAndWithoutAuth(shipmentNumber, newShipmentInfoMap.get("Shipping Method"));
		paymentAuthorizationWizard.completePaymentAuthorization(newShipmentInfoMap.get(PAYMENT_SOURCE));
	}

	/**
	 * Update Shipping Method with Shipment Number and without Payment Auth.
	 *
	 * @param shippingMethod the new shipment method
	 * @param shipmentNumber the shipment number
	 */
	@When("^I change the shipment number (\\d+) Shipping Method to (.+) without authorizing payment$")
	public void updateShippingMethodWithShipmentNumberAndWithoutAuth(final int shipmentNumber, final String shippingMethod) {
		orderEditor.selectShippingMethodByShipmentNumber(shipmentNumber, shippingMethod);
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
	}

	/**
	 * Verify Shipping Method.
	 *
	 * @param shippingMethod the shipping method.
	 */
	@And("^the Shipping Method should be (.+)$")
	public void verifyShippingMethod(final String shippingMethod) {
		orderEditor.verifyShippingMethod(shippingMethod);
	}

	/**
	 * Update Shipping Address.
	 *
	 * @param newShipmentInfoMap new shipment values.
	 */
	@When("^I change the Shipping Information to the following$")
	public void updateShippingInformation(final Map<String, String> newShipmentInfoMap) {
		orderEditor.selectShippingAddress(newShipmentInfoMap.get("Address"));
		orderEditor.selectShippingMethod(newShipmentInfoMap.get("Shipping Method"));
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.completePaymentAuthorization(newShipmentInfoMap.get(PAYMENT_SOURCE));
	}

	/**
	 * Verify values in Items table for a Shipment in Order Details.
	 *
	 * @param shipmentItems expected values for a row in the Items table.
	 */
	@Then("I should see the following line items in the Shipment table$")
	public void verifyShimpentLineItem(final List<Map<String, String>> shipmentItems) {
		for (Map<String, String> item : shipmentItems) {
			orderEditor.verifyShipmentLineItemSkuCode(item.get("sku-code"));
			orderEditor.verifyShipmentLineItemSalePrice(item.get("sale-price"));
			orderEditor.verifyShipmentLineItemQuantity(item.get("quantity"));
			orderEditor.verifyShipmentLineItemDiscount(item.get("discount"));
			orderEditor.verifyShipmentLineItemTotalPrice(item.get("total-price"));
		}
	}

	/**
	 * Verifies values in Shipment Details table for all shipment types
	 * like physical, digital, recurring and bundle
	 *
	 * @param shipmentItems List of ShipmentTableRecord objects that represent row in the table
	 */
	@Then("^I should see the following shipment with lineitem details$")
	public void verifyLineItemByShipment(final List<ShipmentTableRecord> shipmentItems) {
		for (ShipmentTableRecord record : shipmentItems) {
			orderEditor.verifyOrderItemsTableValues(record);
		}
	}

	/**
	 * Modify shipment line item quantity
	 *
	 * @param quantity new quantity value
	 */
	@When("^I modify order shipment line item quantity to (.+)$")
	public void setShipmentLineItemQuantity(final String quantity) {
		orderEditor.setShipmentLineItemQuantity(quantity);
	}

	/**
	 * Modify shipment line item discount
	 *
	 * @param discount item discount value to be set
	 */
	@When("^I modify order shipment line item discount to (.+)$")
	public void setShipmentLineItemDiscount(final String discount) {
		orderEditor.setShipmentLineItemDiscount(discount);
		saveAllChangesAndReload();
	}

	/**
	 * Enters payment source and complete Payment Authorization
	 *
	 * @param paymentMethod payment source name
	 */
	@And("^I complete Payment Authorization with (.+) payment source$")
	public void completePaymentAuthorization(final String paymentMethod) {
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.completePaymentAuthorization(paymentMethod);
	}

	/**
	 * Enters shipment discount value for the order
	 *
	 * @param discount discount amount to be set
	 */
	@When("^I set order shipment discount to (.+)$")
	public void setOrderShipmentDiscountValue(final String discount) {
		orderEditor.setLessShipmentDiscountValue(discount);
		saveAllChangesAndReload();
	}

	/**
	 * Enters new shipping cost for the order
	 *
	 * @param cost new value to be set
	 */
	@When("^I set order Shipping Cost value to (.+)$")
	public void setOrderShippingCost(final String cost) {
		orderEditor.setShippingCostValue(cost);
		saveAllChangesAndReload();
	}

	private void saveAllChangesAndReload() {
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}

	private void closeOrderSearchResultPaneIfOpen() {
		if (orderSearchResultPane != null) {
			orderSearchResultPane.closeOrderSearchResultsPaneIfOpen();
		}
	}

}
