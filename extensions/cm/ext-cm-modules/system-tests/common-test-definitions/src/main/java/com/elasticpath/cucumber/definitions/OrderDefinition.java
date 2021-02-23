/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cucumber.definitions;

import static com.elasticpath.selenium.dialogs.PaymentProcessingErrorDialog.OK_BUTTON_CSS;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cortexTestObjects.Purchase;
import com.elasticpath.selenium.dialogs.AddEditCustomerAddressDialog;
import com.elasticpath.selenium.dialogs.CompleteShipmentDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditItemDetailsDialog;
import com.elasticpath.selenium.dialogs.MoveItemDialog;
import com.elasticpath.selenium.dialogs.OpenNoteDialog;
import com.elasticpath.selenium.dialogs.PaymentProcessingErrorDialog;
import com.elasticpath.selenium.dialogs.SelectASkuDialog;
import com.elasticpath.selenium.dialogs.ShipmentCompletionErrorDialog;
import com.elasticpath.selenium.dialogs.TransactionDetailsDialog;
import com.elasticpath.selenium.domainobjects.PaymentConfiguration;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.Shipment;
import com.elasticpath.selenium.domainobjects.ShipmentTableRecord;
import com.elasticpath.selenium.editor.CustomerEditor;
import com.elasticpath.selenium.editor.OrderEditor;
import com.elasticpath.selenium.editor.RmaEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.CustomerServiceNavigation;
import com.elasticpath.selenium.navigations.ShippingReceiving;
import com.elasticpath.selenium.resultspane.OrderSearchResultPane;
import com.elasticpath.selenium.resultspane.RmaSearchResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.CustomerServiceActionToolbar;
import com.elasticpath.selenium.toolbars.ShippingReceivingActionToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.CompleteExchangeWizard;
import com.elasticpath.selenium.wizards.CompleteReturnWizard;
import com.elasticpath.selenium.wizards.CreateExchangeWizard;
import com.elasticpath.selenium.wizards.CreateRefundWizard;
import com.elasticpath.selenium.wizards.CreateReturnWizard;
import com.elasticpath.selenium.wizards.PaymentAuthorizationWizard;


/**
 * Order step definitions.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveClassLength", "PMD.ExcessivePublicCount"})
public class OrderDefinition {

	private static final String CUSTOMERNAME_COLUMNNAME = "Customer Name";
	private static final String ACCOUNTNAME_COLUMNNAME = "Account Name";
	private static final String ORDER_NUMBER_COLUMNNAME = "Order #";
	private static final String ORDER_STORE_COLUMNNAME = "Store";
	private static final String ORDER_PHONE_FIELD = "phone";
	private static final String ORIGINAL_PAYMENT_SOURCE = "Original payment source";
	private static final String LESS_SHIPMENT_DISCOUNT = "Less shipment discount";
	private static final String LESS_RE_STOCKING_FEE = "Less re-stocking fee";
	private static final String SHIPPING_COST = "Shipping Cost";
	private static final String EXPRESS_RETURN = "Express Return";
	private static final String REFUND_OPTION = "Refund Option";
	private static final String MANUAL_REFUND = "Manual Refund";


	private static final Logger LOGGER = Logger.getLogger(OrderDefinition.class);
	private static final String HOLD_ALL_ORDERS_FOR_STORE_SETTING = "COMMERCE/SYSTEM/ONHOLD/holdAllOrdersForStore";

	private final CustomerServiceNavigation customerServiceNavigation;
	private final ActivityToolbar activityToolbar;
	private final ShippingReceivingActionToolbar shippingReceivingActionToolbar;
	private final CustomerServiceActionToolbar customerServiceActionToolbar;
	private final Product product;
	private OrderSearchResultPane orderSearchResultPane;
	private OrderEditor orderEditor;
	private RmaEditor rmaEditor;
	private CustomerEditor customerEditor;
	private CreateRefundWizard createRefundWizard;
	private CreateReturnWizard createReturnWizard;
	private AddEditCustomerAddressDialog addEditCustomerAddressDialog;
	private ConfirmDialog confirmDialog;
	private EditItemDetailsDialog editItemDetailsDialog;
	private String exchangeOrderNumber;
	private String addressNameToCheck;
	private String addressPhoneToCheck;
	private final PaymentConfiguration paymentConfiguration;
	private TransactionDetailsDialog transactionDetailsDialog;
	private CreateExchangeWizard createExchangeWizard;
	private PaymentProcessingErrorDialog paymentProcessingErrorDialog;
	private CompleteExchangeWizard completeExchangeWizard;
	private PaymentAuthorizationWizard paymentAuthorizationWizard;
	private final WebDriver driver;

	/**
	 * Constructor.
	 *
	 * @param product Product.
	 */
	public OrderDefinition(final Product product, final PaymentConfiguration paymentConfiguration) {
		this.driver = SeleniumDriverSetup.getDriver();
		activityToolbar = new ActivityToolbar(driver);
		shippingReceivingActionToolbar = new ShippingReceivingActionToolbar(driver);
		customerServiceNavigation = new CustomerServiceNavigation(driver);
		customerServiceActionToolbar = new CustomerServiceActionToolbar(driver);
		this.product = product;
		this.paymentConfiguration = paymentConfiguration;
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
		customerServiceNavigation.enterOrderNumber(orderNum);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();

		int index = 0;
		while (!orderSearchResultPane.isOrderInList(orderNum, "Order #") && index < Constants.UUID_END_INDEX) {
			orderSearchResultPane.sleep(Constants.SLEEP_HALFSECOND_IN_MILLIS);
			customerServiceNavigation.clickOrderSearch();
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
		customerServiceNavigation.enterEmail(email);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search latest order by email.
	 */
	@When("^I search for the latest orders by email$")
	public void searchLatestByEmail() {
		searchAndOpenLatestOrderEditor();
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterEmail(orderEditor.getCustomerEmail());
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Verify order number in search results pane.
	 *
	 * @param orderNumber the order number.
	 */
	@Then("^I select the row with order number (.+) in search results pane$")
	public void verifyOrderNumberInSearchResultsPane(final String orderNumber) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(orderNumber, ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Verifies latest successful order in search results pane.
	 */
	@Then("^I select the row with the latest successful order in results page$")
	public void verifyLatestSuccessfulOrderInResultsPane() {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(getLatestOrderNumber(), ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Verifies the order after latest successful order in search results pane.
	 */
	@Then("^I select the row with the order after the latest successful order in results page$")
	public void verifyOrderAfterLatestSuccessfulOrderInResultsPane() {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(getOrderAfterLatestOrderNumber(),
				ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Verify customer name in search results pane.
	 *
	 * @param customerName the customer name.
	 */
	@Then("^I select the row with customer name (.+) in search results pane$")
	public void verifyCustomerNameInSearchResultsPane(final String customerName) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(customerName, CUSTOMERNAME_COLUMNNAME);
	}

	/**
	 * Verify account name in search results pane.
	 *
	 * @param accountName the account name.
	 */
	@Then("^I select the row with account name (.+) in search results pane$")
	public void verifyAccountNameInSearchResultsPane(final String accountName) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(accountName, ACCOUNTNAME_COLUMNNAME);
	}

	/**
	 * Cancel order.
	 */
	@And("^I cancel the order$")
	public void cancelOrder() {
		clickSummaryTab();
		confirmDialog = orderEditor.clickCancelOrderButton();
		confirmDialog.clickOKButton("FulfillmentMessages.OrderSummaryOverviewSection_DialogCancel");
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Cancel shipment by shipment number.
	 *
	 * @param shipmentID the shipment number
	 */
	@And("^I cancel shipment by shipment number (.+)$")
	public void cancelShipmentByShipmentNumber(final String shipmentID) {
		clickDetailsTab();
		activityToolbar.clickReloadActiveEditor();
		String shipmentNumber = getLatestOrderNumber() + "-" + shipmentID;
		orderEditor.cancelShipmentByShipmentNumber(shipmentNumber);
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
		clickDetailsTab();

		int counter = 0;
		while (!orderEditor.isReleaseShipmentButtonInViewport() && counter < Constants.RETRY_COUNTER_3) {
			counter++;
		}

		orderEditor.clickReleaseShipmentButton();
		activityToolbar.clickShippingReceivingButton();
		CompleteShipmentDialog completeShipmentDialog = shippingReceivingActionToolbar.clickCompleteShipmentButton();
		completeShipmentDialog.completeShipment(Purchase.getPurchaseNumber() + "-1");
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verifies is release shipment button disabled.
	 */
	@And("^Release shipment button is disabled$")
	public void verifyReleaseShipmentButtonDisabled() {
		clickDetailsTab();
		orderEditor.verifyReleaseShipmentButtonDisabled();
	}

	/**
	 * Completes the order.
	 */
	@And("^I (?:can release|release) the order shipment")
	public void releaseOrderShipment() {
		activityToolbar.clickShippingReceivingButton();
		CompleteShipmentDialog completeShipmentDialog = shippingReceivingActionToolbar.clickCompleteShipmentButton();
		completeShipmentDialog.completeShipment(Purchase.getPurchaseNumber() + "-1");
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Complete order shipment by ID
	 *
	 * @param shipmentID String
	 */
	@And("^I complete the shipment for shipment ID (.+)$")
	public void completeShipmentByID(final String shipmentID) {
		clickDetailsTab();
		String shipmentNumber = getLatestOrderNumber() + "-" + shipmentID;
		orderEditor.releaseShipmentByShipmentID(shipmentNumber);
		activityToolbar.clickShippingReceivingButton();
		CompleteShipmentDialog completeShipmentDialog = shippingReceivingActionToolbar.clickCompleteShipmentButton();
		completeShipmentDialog.completeShipment(Purchase.getPurchaseNumber() + "-" + shipmentID);
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Force complete order shipment by ID.
	 *
	 * @param shipmentID String
	 */
	@When("^I force complete shipment for shipment ID (.+)$")
	public void forceCompleteShipmentByID(final String shipmentID) {
		clickDetailsTab();
		String shipmentNumber = getLatestOrderNumber() + "-" + shipmentID;
		orderEditor.releaseShipmentByShipmentID(shipmentNumber);
		activityToolbar.clickShippingReceivingButton();
		CompleteShipmentDialog completeShipmentDialog = shippingReceivingActionToolbar.clickCompleteShipmentButton();
		ShipmentCompletionErrorDialog shipmentCompletionErrorDialog = completeShipmentDialog
				.forceCompleteShipment(Purchase.getPurchaseNumber() + "-" + shipmentID);
		shipmentCompletionErrorDialog.clickOK();
		completeShipmentDialog.clickForceCompletionButton();
	}

	/**
	 * Verifies the completion of OrderShipment is impossible.
	 */
	@And("^I cannot complete the order shipment with error message (.+)$")
	public void verifyCompletionOrderShipmentImpossible(final String errorMessage) {
		clickDetailsTab();

		int counter = 0;
		while (!orderEditor.isReleaseShipmentButtonInViewport() && counter < Constants.RETRY_COUNTER_3) {
			counter++;
		}

		orderEditor.clickReleaseShipmentButton();
		activityToolbar.clickShippingReceivingButton();
		CompleteShipmentDialog completeShipmentDialog = shippingReceivingActionToolbar.clickCompleteShipmentButton();
		completeShipmentDialog.enterShipmentId(Purchase.getPurchaseNumber() + "-1");
		completeShipmentDialog.clickValidateButton();
		completeShipmentDialog.clickCompleteButton();
		completeShipmentDialog.verifyErrorMessageDisplayed(errorMessage);
		completeShipmentDialog.clickOK();
		completeShipmentDialog.clickCancel();
	}

	/**
	 * Verify order status.
	 *
	 * @param orderStatus the order status
	 */
	@And("^the order status should be (.+)$")
	public void verifyOrderStatus(final String orderStatus) {
		clickSummaryTab();
		orderEditor.verifyOrderStatus(orderStatus);
	}

	/**
	 * Verifies the order balance.
	 *
	 * @param balanceDue the order balance
	 */
	@Then("^the order balance due is (.+)$")
	public void verifyOrderBalanceDue(final String balanceDue) {
		clickSummaryTab();
		orderEditor.verifyOrderBalance(balanceDue);
	}

	/**
	 * Verify shipment status.
	 *
	 * @param shipmentStatus the shipment status.
	 */
	@And("^the shipment status should be (.+)$")
	public void verifyShipmentStatus(final String shipmentStatus) {
		clickDetailsTab();
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
	 * Select Show skipped payment events.
	 */
	@And("^I click on Show Skipped Payment Events$")
	public void selectShowSkippedPaymentEvents() {
		clickPaymentsTab();
		orderEditor.selectSkippedPaymentEvents();
	}

	/**
	 * Verify Order Payment Transaction with skipped events and Payment Details.
	 *
	 * @param paymentMap the payment transaction.
	 */
	@Then("^I should see following order payment transaction in the Payment History with skipped events$")
	public void verifyOrderPaymentDetailsWithSkippedEvents(final Map<String, String> paymentMap) {
		clickPaymentsTab();
		if (null == paymentConfiguration.getConfigurationName()) {
			paymentConfiguration.setConfigurationName("");
		}
		orderEditor.verifyPaymentTransactionWithSkippedEvents(paymentConfiguration, paymentMap);
	}

	/**
	 * Verify Order Payment Transaction and Payment Details.
	 *
	 * @param paymentMap the payment transaction.
	 */
	@Then("^I should see following order payment transaction in the Payment History$")
	public void verifyOrderPaymentDetails(final Map<String, String> paymentMap) {
		clickDetailsTab();
		clickPaymentsTab();
		if (null == paymentConfiguration.getConfigurationName()) {
			paymentConfiguration.setConfigurationName("");
		}
		orderEditor.verifyPaymentTransaction(paymentConfiguration, paymentMap);
	}

	/**
	 * Verifies order payment data in transaction details.
	 *
	 * @param orderPaymentDataMap map
	 */
	@Then("^I should see following order payment data$")
	public void orderPaymentData(final Map<String, String> orderPaymentDataMap) {
		clickPaymentsTab();
		transactionDetailsDialog = orderEditor.clickViewPaymentDetails();
		transactionDetailsDialog.verifyOrderPaymentData(orderPaymentDataMap);
		transactionDetailsDialog.clickClose();
	}

	/**
	 * Verify error message is displayed in the refund dialog.
	 *
	 * @param expErrorMessage expected error message
	 */
	@Then("^Refund dialog should display an error message (.*)$")
	public void verifyErrorMessageDisplayed(final String expErrorMessage) {
		createRefundWizard.verifyErrorMessageDisplayed(expErrorMessage);
	}

	/**
	 * Verify payment plugin error message.
	 *
	 * @param paymentError expected error
	 */
	@Then("^Refund should respond with payment error (.*)$")
	public void verifyPaymentPluginError(final String paymentError) {
		createRefundWizard.verifyPaymentError(paymentError);
	}

	/**
	 * Verify exact payment plugin error message.
	 *
	 * @param paymentError expected error
	 */
	@Then("^Refund should respond with exact payment error (.*)$")
	public void verifyExactPaymentPluginError(final String paymentError) {
		createRefundWizard.verifyExactPaymentError(paymentError);
	}

	/**
	 * Verify Order Payment Transactions is no present in Payment History.
	 */
	@And("^I should NOT see order payment transactions in the Payment History$")
	public void verifyOrderPaymentTransactionNotPresentInPaymentHistory() {
		clickPaymentsTab();
		orderEditor.verifyPaymentTransactionTableIsEmpty();
	}

	/**
	 * Verify Order Payment Transaction is absent in Payment History with skipped events.
	 *
	 * @param paymentMap the payment transaction.
	 */
	@Then("^I should NOT see following order payment transaction in the Payment History with skipped events$")
	public void verifyNoSuchOrderPaymentDetailsInPaymentsHistoryWithSkippedEvents(final Map<String, String> paymentMap) {
		clickPaymentsTab();
		orderEditor.verifyNoSuchPaymentTransactionInPaymentHistoryWithSkippedEvents(paymentMap);
	}

	/**
	 * Verify Order Payment Transaction is absent.
	 *
	 * @param paymentMap the payment transaction.
	 */
	@Then("^I should NOT see following order payment transaction in the Payment History$")
	public void verifyNoSuchOrderPaymentDetails(final Map<String, String> paymentMap) {
		clickPaymentsTab();
		orderEditor.verifyNoSuchPaymentTransaction(paymentMap);
	}

	/**
	 * Verify Order Payment Transaction type is absent.
	 *
	 * @param transactionType the type of order payment transaction.
	 */
	@Then("^I should NOT see (.+) order payment transaction type in the Payment History$")
	public void verifyNoSuchOrderPaymentDetails(final String transactionType) {
		clickPaymentsTab();
		orderEditor.verifyNoSuchPaymentTransactionType(transactionType);
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
		customerServiceNavigation.clearInputFieldsInCustomersTab();
	}

	/**
	 * Clear input fields.
	 */
	@When("^I clear the input field in orders tabs$")
	public void clearInputFieldsInOrdersTab() {
		customerServiceNavigation.clearInputFieldsInOrdersTab();
	}

	/**
	 * Searches order and opens order editor.
	 */
	@When("^I (?:search|can search) and open order editor for the latest order$")
	public void searchAndOpenLatestOrderEditor() {
		activityToolbar.clickCustomerServiceButton();
		customerServiceNavigation.enterOrderNumber(getLatestOrderNumber());
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(getLatestOrderNumber(), ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Searches order and opens order editor.
	 */
	@When("^I (?:search|can search) and open order editor for the latest exchange order$")
	public void searchAndOpenLatestExchangeOrderEditor() {
		activityToolbar.clickCustomerServiceButton();
		customerServiceNavigation.enterOrderNumber(createExchangeWizard.getExchangeOrderNumber());
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(createExchangeWizard.getExchangeOrderNumber(), ORDER_NUMBER_COLUMNNAME);
	}

	/**
	 * Searches and open the latest order and create refund for it.
	 */
	@When("^I (?:search|can search) and open the latest order and create a refund with following values$")
	public void searchAndOpenLatestOrderAndCreateRefund(final Map<String, String> refundMap) {
		searchAndOpenLatestOrderEditor();
		createRefund(refundMap);
	}

	/**
	 * Searches order by number.
	 */
	@When("^I search the latest successful order by number$")
	public void searchLatestSuccessfulOrderByNumber() {
		LOGGER.info("searching for ordernumber.... " + getLatestOrderNumber());
		searchOrderByNumber(getLatestOrderNumber());
	}

	/**
	 * Searches order by number.
	 */
	@When("^I search the order after the latest successful order by number$")
	public void searchOrderAfterLatestSuccessfulOrderByNumber() {
		LOGGER.info("searching for ordernumber.... " + getOrderAfterLatestOrderNumber());
		searchOrderByNumber(getOrderAfterLatestOrderNumber());
	}

	/**
	 * Fills order refund form.
	 *
	 * @param refundMap refund item values
	 */
	@When("^I fill order refund form with following values$")
	public void fillOrderRefundForm(final Map<String, String> refundMap) {
		clickDetailsTab();
		orderEditor.closePane("#" + Purchase.getPurchaseNumber());
		activityToolbar.clickCustomerServiceButton();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
		createRefundWizard = orderEditor.clickCreateRefundButton();
		createRefundWizard.verifyAvailableRefundAmount(refundMap.get("Available Refund Amount"));
		createRefundWizard.fillOrderRefundForm(refundMap);
	}

	/**
	 * Verify is refund button disabled.
	 */
	@When("^Refund button should be disabled")
	public void verifyRefundButtonDisabled() {
		createRefundWizard.verifyRefundButtonDisabled();
	}

	/**
	 * Verify is refund button enabled.
	 */
	@When("^Refund button should be enabled$")
	public void verifyRefundButtonEnabled() {
		createRefundWizard.verifyRefundButtonEnabled();
	}

	/**
	 * Creates refund.
	 *
	 * @param refundMap refund item values
	 */
	@When("^I create a refund with following values$")
	public void createRefund(final Map<String, String> refundMap) {
		clickDetailsTab();
		orderEditor.closePane("#" + Purchase.getPurchaseNumber());
		activityToolbar.clickCustomerServiceButton();
		orderEditor = orderSearchResultPane.selectOrderAndOpenOrderEditor(Purchase.getPurchaseNumber(), ORDER_NUMBER_COLUMNNAME);
		createRefundWizard = orderEditor.clickCreateRefundButton();
		createRefundWizard.verifyAvailableRefundAmount(refundMap.get("Available Refund Amount"));
		createRefundWizard.createRefund(refundMap);
	}

	/**
	 * Creates exchange.
	 *
	 * @param exchangeMap exchange item values
	 */
	@When("^I create a exchange with following values$")
	public void createExchange(final Map<String, String> exchangeMap) {
		clickDetailsTab();
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		createExchangeWizard = orderEditor.clickCreateExchangeButton();
		createExchangeWizard.createExchange(exchangeMap);
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		clickReturnsAndExchangeTab();
		exchangeOrderNumber = orderEditor.verifyExchangeOrderNumberIsPresent();
		LOGGER.info("exchange order number: " + exchangeOrderNumber);
	}

	/**
	 * Prepares exchange.
	 *
	 * @param exchangeMap exchange item values
	 */
	@When("^I prepare an exchange with following values$")
	public void prepareExchange(final Map<String, String> exchangeMap) {
		clickDetailsTab();
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		createExchangeWizard = orderEditor.clickCreateExchangeButton();
		createExchangeWizard.prepareExchange(exchangeMap);
	}

	/**
	 * Cancel exchange.
	 */
	@When("^I cancel the exchange under the original order$")
	public void cancelExchange() {
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		confirmDialog = orderEditor.clickCancelExchangeButton();
		confirmDialog.clickOKButton("FulfillmentMessages.OrderReturnSection_CancelExchangeTitle");
	}

	/**
	 * Verifies status of exchange order.
	 *
	 * @param expectedStatus the expected status of exchange order.
	 */
	@When("^The status of exchange order is (.+)$")
	public void verifyExchangeOrderStatus(final String expectedStatus) {
		orderEditor.verifyExchangeOrderStatus(expectedStatus);
	}

	/**
	 * Creates exchange.
	 *
	 * @param exchangeMap exchange item values
	 */
	@When("^I create a exchange with error$")
	public void createExchangeWithError(final Map<String, String> exchangeMap) {
		clickDetailsTab();
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		createExchangeWizard = orderEditor.clickCreateExchangeButton();
		paymentProcessingErrorDialog = orderEditor.createPaymentProcessingErrorDialog();
		createExchangeWizard.createExchange(exchangeMap);
	}

	/**
	 * Click cancel button.
	 */
	@When("^I click cancel in exchange window$")
	public void clickCancel() {
		completeExchangeWizard.clickCancelInDialog();
	}

	/**
	 * Verifies error message is displayed.
	 *
	 * @param errorMessage String
	 */
	@Then("Error message (.*) appears")
	public void hasError(final String errorMessage) {
		paymentProcessingErrorDialog.verifyErrorMessageDisplayedInPaymentProcessingErrorDialog(errorMessage);
		paymentProcessingErrorDialog.clickButton(OK_BUTTON_CSS, "OK");
		shippingReceivingActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Creates free item exchange with no new payment Reservation event.
	 *
	 * @param exchangeMap exchange item values
	 */
	@When("^I create exchange with adjusted shipping costs$")
	public void createExchangeWithAdjustShippingCost(final Map<String, String> exchangeMap) {
		clickDetailsTab();
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		createExchangeWizard = orderEditor.clickCreateExchangeButton();
		createExchangeWizard.createFreeItemExchange(exchangeMap);
		shippingReceivingActionToolbar.clickReloadActiveEditor();
		clickReturnsAndExchangeTab();
		exchangeOrderNumber = orderEditor.verifyExchangeOrderNumberIsPresent();
		LOGGER.info("exchange order number: " + exchangeOrderNumber);
	}

	/**
	 * Resets shipping cost value.
	 *
	 * @param shippingCost String
	 */
	@When("^I reset order exchange shipping cost to (.+)$")
	public void resetExchangeShippingCost(final String shippingCost) {
		createExchangeWizard.setShippingCostValue(shippingCost);
	}

	/**
	 * Verify sku code.
	 *
	 * @param skuCode the sku code.
	 */
	@Then("^I should see the returned sku code (.+)$")
	public void verifyReturnedSkuCode(final String skuCode) {
		clickReturnsAndExchangeTab();
		orderEditor.verifyReturnSkuCode(skuCode);
	}

	/**
	 * Opens exchange order editor.
	 */
	@When("^I open the exchange order editor$")
	public void openExchangeOrderEditor() {
		clickReturnsAndExchangeTab();
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
		clickDetailsTab();
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
		clickDetailsTab();
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
		clickDetailsTab();
		for (Map.Entry<String, String> entry : shipmentItemMap.entrySet()) {
			orderEditor.verifyEShipmentSkuCodeAndQuantity(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Verifies the order contains newly created product sku.
	 */
	@Then("^the order contains the newly created product$")
	public void verfiyNewlyCreatedSkuInOrder() {
		clickDetailsTab();
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
		clickDetailsTab();
		orderEditor.verifyAndSelectOrderSkuCode(skuCode);
		MoveItemDialog moveItemDialog = orderEditor.clickMoveItemButton();
		moveItemDialog.moveItem(newShipmentInfoMap.get("Address"), newShipmentInfoMap.get("Shipment Method"));
	}

	/**
	 * Split shipment by shipment number and sku.
	 *
	 * @param skuCode            String
	 * @param shipmentID         String
	 * @param newShipmentInfoMap Map
	 */
	@When("^I split a shipment for sku (.+) for shipment number (.+) with following values$")
	public void createNewShipmentByID(final String skuCode, final String shipmentID, final Map<String, String> newShipmentInfoMap) {
		clickDetailsTab();
		customerServiceActionToolbar.clickReloadActiveEditor();
		String shipmentNumber = getLatestOrderNumber() + "-" + shipmentID;
		MoveItemDialog moveItemDialog = orderEditor.moveItemByShipmentNumber(skuCode, shipmentNumber);
		moveItemDialog.moveItem(newShipmentInfoMap.get("Address"), newShipmentInfoMap.get("Shipment Method"));

	}

	/**
	 * Adds an item to the shipment.
	 *
	 * @param skuCode        the sku code
	 * @param addItemInfoMap item values
	 */
	@When("^I add sku (.+) to the shipment with following values$")
	public void addItemToShipment(final String skuCode, final Map<String, String> addItemInfoMap) {
		clickDetailsTab();
		SelectASkuDialog selectASkuDialog = orderEditor.clickAddItemButton();
		selectASkuDialog.selectSkuAndPriceList(skuCode, addItemInfoMap.get("Price List Name"));
	}

	/**
	 * Removes an item from shipment.
	 *
	 * @param skuCode the sku code
	 */
	@And("^I remove sku (.+) from the shipment$")
	public void addItemToShipment(final String skuCode) {
		clickDetailsTab();
		orderEditor.verifyAndSelectOrderSkuCode(skuCode);
		orderEditor.clickRemoveItemButton();
		completePaymentAuthorization(ORIGINAL_PAYMENT_SOURCE);
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
	 * @param quantity      the quantity
	 * @param skuCode       the sku code
	 * @param returnInfoMap return form values
	 */
	@And("^I create digital item return with quantity (\\d+) for sku (.+)$")
	public void createReturnForDigital(final int quantity, final String skuCode, final Map<String, String> returnInfoMap) {
		searchAndOpenLatestOrderEditor();
		clickDetailsTab();
		clickCreateReturnButton();
		final BigDecimal shipmentDiscount = new BigDecimal(returnInfoMap.getOrDefault(LESS_SHIPMENT_DISCOUNT, "0"));
		final String refundOption = returnInfoMap.getOrDefault(REFUND_OPTION, ORIGINAL_PAYMENT_SOURCE);
		createReturnWizard.createDigitalReturn(quantity, skuCode, shipmentDiscount, refundOption.equals(MANUAL_REFUND));
	}

	/**
	 * Creates return for physical item.
	 *
	 * @param quantity      the quantity
	 * @param skuCode       the sku code
	 * @param returnInfoMap return form values
	 */
	@And("^I create physical item return with quantity (\\d+) for sku (.+)$")
	public void createReturnForPhysical(final int quantity, final String skuCode, final Map<String, String> returnInfoMap) {
		searchAndOpenLatestOrderEditor();
		clickDetailsTab();
		completeOrderShipment();
		clickCreateReturnButton();
		final BigDecimal shippingCost = new BigDecimal(returnInfoMap.getOrDefault(SHIPPING_COST, "0"));
		final BigDecimal shipmentDiscount = new BigDecimal(returnInfoMap.getOrDefault(LESS_SHIPMENT_DISCOUNT, "0"));
		final BigDecimal restockingFee = new BigDecimal(returnInfoMap.getOrDefault(LESS_RE_STOCKING_FEE, "0"));
		final String expressReturn = returnInfoMap.getOrDefault(EXPRESS_RETURN, "false");
		final String refundOption = returnInfoMap.getOrDefault(REFUND_OPTION, ORIGINAL_PAYMENT_SOURCE);
		createReturnWizard.createPhysicalReturn(quantity, skuCode, shippingCost, shipmentDiscount, restockingFee,
				!"true".equalsIgnoreCase(expressReturn), refundOption.equals(MANUAL_REFUND));
	}

	@And("^return dialog must have an error (.+)$")
	public void verifyReturnErrorMessage(final String message) {
		assertThat(createReturnWizard.getErrorMessage().contains(message))
				.as("Error message \"" + message + "\" was not found")
				.isTrue();
		createReturnWizard.clickCancel();
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
		clickDetailsTab();
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
		ShippingReceiving shippingReceiving = activityToolbar.clickShippingReceivingButton();
		shippingReceiving.clickReturnsTab();
		String orderNumber = Purchase.getPurchaseNumber();
		shippingReceiving.enterOrderNumber(orderNumber);
		RmaSearchResultPane rmaSearchResultPane = shippingReceiving.clickReturnsSearch();
		rmaEditor = rmaSearchResultPane.selectOrderAndOpenRmaEditor(orderNumber, ORDER_NUMBER_COLUMNNAME);
		rmaEditor.setReturnedQuantity(receivedSku, receivedQuantity);
		customerServiceActionToolbar.clickSaveButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
	}

	/**
	 * Verifies RMA status.
	 *
	 * @param expectedRMAStatus the expected RMA status.
	 */
	@When("^The RMA status is (.+)$")
	public void verifyRMAStatus(final String expectedRMAStatus) {
		ShippingReceiving shippingReceiving = activityToolbar.clickShippingReceivingButton();
		shippingReceiving.clickReturnsTab();
		String orderNumber = Purchase.getPurchaseNumber();
		shippingReceiving.enterOrderNumber(orderNumber);
		RmaSearchResultPane rmaSearchResultPane = shippingReceiving.clickReturnsSearch();
		rmaEditor = rmaSearchResultPane.selectOrderAndOpenRmaEditor(orderNumber, ORDER_NUMBER_COLUMNNAME);
		rmaEditor.verifyRMAStatus(expectedRMAStatus);
	}

	/**
	 * Completes the return.
	 */
	@When("^I complete the return refunding ((?:manually|to original source))$")
	public void completeReturn(final String refundOption) {
		orderEditor = rmaEditor.clickOpenOriginalOrderButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
		clickReturnsAndExchangeTab();
		CompleteReturnWizard completeReturnWizard = orderEditor.clickCompleteReturnButton();
		completeReturnWizard.completeReturn("manually".equals(refundOption));
	}

	/**
	 * Completes the exchange.
	 */
	@When("^I complete the exchange refunding ((?:manually|to original source))$")
	public void completeExchange(final String refundOption) {
		orderEditor = rmaEditor.clickOpenOriginalOrderButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
		clickReturnsAndExchangeTab();
		completeExchangeWizard = orderEditor.clickCompleteExchangeButton();
		completeExchangeWizard.completeExchange("manually".equals(refundOption));
	}

	/**
	 * Completes the exchange.
	 */
	@When("^I complete the exchange refunding ((?:manually|to original source)) with error$")
	public void completeExchangeWithError(final String refundOption) {
		orderEditor = rmaEditor.clickOpenOriginalOrderButton();
		customerServiceActionToolbar.clickReloadActiveEditor();
		clickReturnsAndExchangeTab();
		completeExchangeWizard = orderEditor.clickCompleteExchangeButton();
		completeExchangeWizard.completeExchangeWithError("manually".equals(refundOption));
	}

	/**
	 * Verifies Order Note.
	 *
	 * @param noteMap the properties of the note
	 */
	@Then("^I should see following note in the Order Notes$")
	public void verifyOrderNoteDetails(final Map<String, String> noteMap) {
		clickNotesTab();

		final Map<String, String> newMap = new HashMap<>(noteMap);
		final String originatorKey = "Originator";
		String originator = noteMap.get(originatorKey);
		newMap.remove(originatorKey, originator);

		orderEditor.verifyOrderNote(newMap.values());

		final OpenNoteDialog openNoteDialog = orderEditor.clickOpenNote();
		if (originator != null) {
			openNoteDialog.verifyOriginator(originator);
		}
		openNoteDialog.verifyNote(newMap.values());
		openNoteDialog.clickCancel();
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
		clickDetailsTab();
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
		clickDetailsTab();
		addEditCustomerAddressDialog = orderEditor.clickEditAddressButton();
		addEditCustomerAddressDialog.selectCountry(addressMap.get("Country"));
		addEditCustomerAddressDialog.selectState(addressMap.get("State/Province"));
		addEditCustomerAddressDialog.enterCity(addressMap.get("City"));
		addEditCustomerAddressDialog.enterZip(addressMap.get("Zip/Postal Code"));
		addEditCustomerAddressDialog.enterAddressLine1(addressMap.get("Address Line 1"));
		addEditCustomerAddressDialog.enterPhone(addressMap.get("Phone"));
		addEditCustomerAddressDialog.clickSave();
		orderEditor.selectShippingMethod(shippingMethod);
		completePaymentAuthorization(ORIGINAL_PAYMENT_SOURCE);
	}

	/**
	 * Verify Unlock Order Button.
	 */
	@Then("^Unlock Order button is disabled$")
	public void verifyUnlockOrderButton() {
		orderEditor.verifyUnlockOrderIsNotEnabled();
	}

	/**
	 * Verify shipment summary.
	 *
	 * @param shipments the properties of each shipment belonging to an order
	 */
	@Then("^I should see the following Shipment Summary$")
	public void verifyShipmentSummary(final List<Map<String, String>> shipments) {
		clickDetailsTab();
		List<Shipment> shipmentObjects = orderEditor.populateShipments(shipments);
		activityToolbar.clickReloadActiveEditor();
		for (Shipment shipment : shipmentObjects) {
			orderEditor.verifyShipmentSubtotal(shipment.getShipmentNumber(), shipment.getItemSubTotal());
			orderEditor.verifyShipmentDiscount(shipment.getShipmentNumber(), shipment.getLessShipmentDiscount());
			orderEditor.verifyShipmentItemTaxes(shipment.getShipmentNumber(), shipment.getItemTaxes());
			orderEditor.verifyShipmentTotal(shipment.getShipmentNumber(), shipment.getShipmentTotal());

			if (!"E-shipment".equals(shipment.getShipmentNumber())) {
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
//		Clicking summary tab and back to details tab to prevent from double clicking tab causing editor unscrollable.
		clickSummaryTab();
		clickDetailsTab();
		orderEditor.verifyPromotionColumnValue(promotionName, "Promotion Name");
	}

	/**
	 * Search order by First Name.
	 *
	 * @param firstName the First Name.
	 */
	@When("^I search for orders by First Name (.+)$")
	public void searchOrderByFirstName(final String firstName) {
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterFirstName(firstName);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by Last Name.
	 *
	 * @param lastName the Last Name.
	 */
	@When("^I search for orders by Last Name (.+)$")
	public void searchOrderByLastName(final String lastName) {
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterLastName(lastName);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by Business Name.
	 *
	 * @param businessName the account name.
	 */
	@When("^I search for orders by Business Name (.+)$")
	public void searchOrderByAccountName(final String businessName) {
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterBusinessName(businessName);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by Postal Code/ZIP.
	 *
	 * @param postalCode the Postal Code.
	 */
	@When("^I search for latest successful order by Postal Code (.+)$")
	public void searchOrderByPostalCode(final String postalCode) {
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterOrderNumber(getLatestOrderNumber());
		customerServiceNavigation.enterPostalCode(postalCode);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by Phone Number.
	 *
	 * @param phoneNumber the Phone Number.
	 */
	@When("^I search for orders by Phone Number (.+)$")
	public void searchOrderByPhoneNumber(final String phoneNumber) {
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterPhoneNumber(phoneNumber);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by store.
	 *
	 * @param storeName the Store.
	 */
	@When("^I search for latest successful order by Store (.+)$")
	public void searchOrderByStore(final String storeName) {
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterOrderNumber(getLatestOrderNumber());
		customerServiceNavigation.selectStore(storeName);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by Order Status.
	 *
	 * @param orderStatus the Order status.
	 */
	@When("^I search for latest successful order by Order Status (.+)$")
	public void searchLatestOrderByStatus(final String orderStatus) {
		closeOrderSearchResultPaneIfOpen();
		customerServiceNavigation.clearInputFieldsInOrdersTab();

		customerServiceNavigation.enterOrderNumber(getLatestOrderNumber());

		customerServiceNavigation.selectStatus(orderStatus);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Verify Order Store Column in search results pane.
	 *
	 * @param orderStore the Order Store Column.
	 */
	@Then("^I select the row with store (.+) in search results pane$")
	public void verifyOrderStoreNameInSearchResultsPane(final String orderStore) {
		orderSearchResultPane.verifyOrderColumnValueAndSelectRow(orderStore, ORDER_STORE_COLUMNNAME);
	}

	/**
	 * Verify Order Status Column in search results pane.
	 *
	 * @param orderStatus the Order Status Column.
	 */
	@Then("^The selected row has order status (.+) in search results pane$")
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
	 * Gets the order number after the latest successful order number.
	 *
	 * @return orderNumber.
	 */
	private String getOrderAfterLatestOrderNumber() {
		return String.valueOf(Long.valueOf(getLatestOrderNumber()) + 1);
	}

	/**
	 * Gets the latest successful order number.
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
	@When("^I search for latest successful order by Order Shipment Status (.+)$")
	public void searchOrderByShipmentStatus(final String orderShipmentStatus) {
		closeOrderSearchResultPaneIfOpen();
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterOrderNumber(getLatestOrderNumber());
		customerServiceNavigation.selectShipmentStatus(orderShipmentStatus);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by SKU Code.
	 *
	 * @param skuCode the SKU Code .
	 */
	@When("^I search for latest successful order by SKU Code (.+)$")
	public void searchOrderBySkuCode(final String skuCode) {
		closeOrderSearchResultPaneIfOpen();
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterOrderNumber(getLatestOrderNumber());
		customerServiceNavigation.enterSkuCode(skuCode);
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
	}

	/**
	 * Search order by Dates.
	 */
	@When("^I search for order by Dates between current date and next date$")
	public void searchOrderByDateRange() {
		closeOrderSearchResultPaneIfOpen();
		customerServiceNavigation.clearInputFieldsInOrdersTab();
		customerServiceNavigation.enterOrderNumber(getLatestOrderNumber());
		customerServiceNavigation.enterFromDate();
		customerServiceNavigation.enterToDate();
		orderSearchResultPane = customerServiceNavigation.clickOrderSearch();
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
		clickDetailsTab();
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
		clickDetailsTab();
		orderEditor.verifyAndSelectOrderSkuCode(orderSkuCode);
	}

	/**
	 * Update Shipping Method with Payment Auth.
	 *
	 * @param newShipmentInfoMap new shipment values
	 */
	@When("^I change the Shipping Method to the following$")
	public void updateShippingMethod(final Map<String, String> newShipmentInfoMap) {
		clickDetailsTab();
		updateShippingMethodWithShipmentNumberAndPaymentAuth(1, newShipmentInfoMap);
	}

	/**
	 * Update Shipping Method with Shipment Number and Payment Auth.
	 *
	 * @param shipmentNumber     shipment ordinal number
	 * @param newShipmentInfoMap new shipment values
	 */
	@When("^I change the shipment number (\\d+) Shipping Method to the following$")
	public void updateShippingMethodWithShipmentNumberAndPaymentAuth(final int shipmentNumber, final Map<String, String> newShipmentInfoMap) {
		orderEditor.selectShippingMethodByShipmentNumber(shipmentNumber, newShipmentInfoMap.get("Shipping Method"));
	}

	/**
	 * Verify Shipping Method.
	 *
	 * @param shippingMethod the shipping method.
	 */
	@And("^the Shipping Method should be (.+)$")
	public void verifyShippingMethod(final String shippingMethod) {
		clickDetailsTab();
		orderEditor.verifyShippingMethod(shippingMethod);
	}

	/**
	 * Update Shipping Address.
	 *
	 * @param newShipmentInfoMap new shipment values.
	 */
	@When("^I change the Shipping Information to the following$")
	public void updateShippingInformation(final Map<String, String> newShipmentInfoMap) {
		clickDetailsTab();
		orderEditor.selectShippingAddress(newShipmentInfoMap.get("Address"));
		orderEditor.selectShippingMethod(newShipmentInfoMap.get("Shipping Method"));
	}

	/**
	 * Verify values in Items table for a Shipment in Order Details.
	 *
	 * @param shipmentItems expected values for a row in the Items table.
	 */
	@Then("I should see the following line items in the Shipment table$")
	public void verifyShipmentLineItem(final List<Map<String, String>> shipmentItems) {
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
		clickDetailsTab();
		for (ShipmentTableRecord record : shipmentItems) {
			orderEditor.verifyOrderItemsTableValues(record);
		}
	}

	/**
	 * Modify order shipment quantity
	 *
	 * @param quantity new quantity value
	 */
	@When("^I enter (.+) for order shipment quantity$")
	public void setShipmentLineItemQuantity(final String quantity) {
		clickDetailsTab();
		orderEditor.setShipmentLineItemQuantity(quantity);
	}

	/**
	 * Modify shipment line item quantity and complete payment.
	 *
	 * @param quantity new quantity value
	 */
	@When("^I modify order shipment line item quantity to (.+)$")
	public void setAndSaveShipmentLineItemQuantity(final String quantity) {
		clickDetailsTab();
		orderEditor.setShipmentLineItemQuantity(quantity);
		completePaymentAuthorization(ORIGINAL_PAYMENT_SOURCE);
	}

	/**
	 * Set shipment line item quantity to trigger failure.
	 *
	 * @param quantity quantity
	 */
	@When("^I modify the order to increase order total by setting line item quantity to (.+)$")
	public void setShipmentLineItemQuantityToFailReAuthorization(final String quantity) {
		clickDetailsTab();
		orderEditor.setShipmentLineItemQuantity(quantity);
		acceptCancelReAuthorization(ORIGINAL_PAYMENT_SOURCE);
	}

	/**
	 * Modify shipment line item discount
	 *
	 * @param discount item discount value to be set
	 */
	@When("^I modify order shipment line item discount to (.+)$")
	public void setShipmentLineItemDiscount(final String discount) {
		clickDetailsTab();
		orderEditor.setShipmentLineItemDiscount(discount);
		completePaymentAuthorization(ORIGINAL_PAYMENT_SOURCE);
	}

	/**
	 * Modifies shipment line item discount, saves the order and reserve a new amount.
	 *
	 * @param discount item discount value to be set.
	 */
	@When("^I set order shipment line item discount to (.+), save the order and reserve a new amount$")
	public void setShipmentLineItemDiscountAndReserveNewAmount(final String discount) {
		clickDetailsTab();
		orderEditor.setShipmentLineItemDiscount(discount);
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.clickAuthorizeButton();
	}

	/**
	 * Verifies completion of Payment Reservation should be impossible.
	 *
	 * @param errorMessage the expected error message.
	 */
	@And("^I cannot complete Payment Reservation with error message (.+)$")
	public void verifyThatCompletionPaymentAuthorizationIsImpossible(final String errorMessage) {
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.clickAuthorizeButton();
		assertThat(paymentAuthorizationWizard.isDoneButtonEnable())
				.as("Done button should be disabled")
				.isFalse();
		paymentAuthorizationWizard.verifyErrorMessageDisplayed(errorMessage);
	}

	/**
	 * Checks payment reservations.
	 *
	 * @param reservations the expected reservations.
	 */
	@And("^I see new reservations$")
	public void checkNewReservations(final Map<String, String> reservations) {
		paymentAuthorizationWizard.verifyReservations(reservations);
	}

	/**
	 * Finishes the Payment Reservation.
	 */
	@And("^I can finish Payment Reservation$")
	public void finishPaymentReservation() {
		assertThat(paymentAuthorizationWizard.isDoneButtonEnable())
				.as("Done button should be enabled")
				.isTrue();
		paymentAuthorizationWizard.clickDoneButton();
	}

	/**
	 * Cancels the Payment Reservation.
	 */
	@And("^I cancel Payment Reservation$")
	public void cancelPaymentReservation() {
		paymentAuthorizationWizard.clickCancel();
		paymentAuthorizationWizard.clickOk();
	}

	/**
	 * Enters payment source and complete Payment Authorization.
	 *
	 * @param paymentMethod payment source name
	 */
	@And("^I complete Payment Authorization with (.+) payment source$")
	public void completePaymentAuthorization(final String paymentMethod) {
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.completePaymentAuthorization(paymentMethod);
	}

	/**
	 * Accepts Cancel Reauthroization.
	 *
	 * @param paymentMethod payment source name
	 */
	@And("^I accept Cancel Reauthorization warning$")
	public void acceptCancelReAuthorization(final String paymentMethod) {
		paymentAuthorizationWizard = customerServiceActionToolbar.clickSaveAllButton();
		paymentAuthorizationWizard.cancelFailedPaymentAuthorization(paymentMethod);
	}

	/**
	 * Enters shipment discount value for the order
	 *
	 * @param discount discount amount to be set
	 */
	@When("^I set order shipment discount to (.+)$")
	public void setOrderShipmentDiscountValue(final String discount) {
		orderEditor.setLessShipmentDiscountValue(discount);
		completePaymentAuthorization(ORIGINAL_PAYMENT_SOURCE);
	}

	/**
	 * Enters new shipping cost for the order
	 *
	 * @param cost new value to be set
	 */
	@When("^I set order Shipping Cost value to (.+)$")
	public void setOrderShippingCost(final String cost) {
		orderEditor.setShippingCostValue(cost);
		completePaymentAuthorization(ORIGINAL_PAYMENT_SOURCE);
	}

	/**
	 * Resets order Shipping Cost value to 0
	 */
	@When("^I reset order Shipping Cost value to 0$")
	public void resetOrderShippingCost() {
		orderEditor.setShippingCostValue("0");
		customerServiceActionToolbar.clickSaveAllButton();
	}

	private void closeOrderSearchResultPaneIfOpen() {
		if (orderSearchResultPane != null) {
			orderSearchResultPane.closeOrderSearchResultsPaneIfOpen();
		}
	}

	/**
	 * Verifies table containing information of B2B shoppers who placed the order.
	 *
	 * @param orderDataMap table containing order data
	 */
	@Then("^I should see the following in the order data table$")
	public void verifyOrderData(final Map<String, String> orderDataMap) {
		for (Map.Entry<String, String> entry : orderDataMap.entrySet()) {
			orderEditor.verifyOrderDataTableRow(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Verifies Payment Summary values.
	 *
	 * @param paymentSummaryMap payment summary map.
	 */
	@Then("^Payment Summary should have the following totals$")
	public void verifyPaymentSummary(final Map<String, String> paymentSummaryMap) {
		clickPaymentsTab();
		orderEditor.verifyPaymentSummary(paymentSummaryMap);
	}

	/**
	 * Verifies the completion of refund is impossible.
	 *
	 * @param errorMessage expected error message
	 */
	@And("^I cannot complete refund with error message (.+)$")
	public void verifyRefundCompletionImpossible(final String errorMessage) {
		createRefundWizard.verifyRefundButtonEnabled();
		createRefundWizard.clickNextInDialog();
		createRefundWizard.verifyPaymentError(errorMessage);
	}

	/**
	 * Verifies Authorize Exchange button is enabled.
	 */
	@Then("^Authorize Exchange button should be enabled$")
	public void verifyAuthorizeExchangeButtonIsEnabled() {
		createExchangeWizard.verifyAuthorizeButtonIsEnabled();
	}

	/**
	 * Verifies Authorize Exchange button is disabled on previous page.
	 */
	@Then("^Authorize Exchange button should be disabled on previous page$")
	public void verifyAuthorizeExchangeButtonIsDisabledOnPreviousPage() {
		createExchangeWizard.clickBackInDialog();
		createExchangeWizard.verifyAuthorizeButtonIsDisabled();
	}

	/**
	 * Verify an order hold exists with proper rule, status and resolved by.
	 * @param orderHoldDataMap - should include keys of "Hold Rule", "Status" and "Resolved By"
	 */
	@And("^An order hold exists with the following")
	public void orderHoldStatus(final Map<String, String> orderHoldDataMap) {
		clickHoldTab();
		orderEditor.verifyOrderHold(orderHoldDataMap.get("Hold Rule"), orderHoldDataMap.get("Status"), orderHoldDataMap.get("Resolved By"));
	}

	/**
	 * Resolves active order holds.
	 */
	@When("^I resolve all order holds$")
	public void resolveAllOrderHolds() {
		orderEditor.resolveAllHolds();
	}

	/**
	 * Marks the first active order hold as unresolvable.
	 */
	@When("^I mark the first possible order hold unresolvable$")
	public void markFirstOrderHoldUnresolvable() {
		orderEditor.markHoldUnresolvable();
	}

	/**
	 * Verify the order hold has the specified status.
	 * @param status the expected status of the order hold.
	 */
	@Then("^The order hold status should be (.+)$")
	public void verifyOrderHoldStatus(final String status) {
		customerServiceActionToolbar.clickReloadActiveEditor();
		orderEditor.verifyOrderHoldStatus(status);
	}

	/**
	 * Verify that the order hold is not resolvable (ie cannot be approved or rejected) by the current user.
	 */
	@Then("^I should be unable to resolve the hold$")
	public void verifyOrderHoldUneditable() {
		orderEditor.verifyOrderHoldUneditable();
	}

	/**
	 * Removes the the hold all orders setting for the test store
	 */
	@After(value = "@disableOrderHold", order = Constants.CLEANUP_ORDER_THIRD)
	public void disableOrderHold() {
		NavigationDefinition navigationDefinition = new NavigationDefinition();
		navigationDefinition.clickConfiguration();
		SystemConfigurationDefinition systemConfig = new SystemConfigurationDefinition();
		systemConfig.openSystemConfiguration();
		systemConfig.enterSettingName(HOLD_ALL_ORDERS_FOR_STORE_SETTING);
		systemConfig.removeDefinedValueRecord(ImmutableMap.of("setting", HOLD_ALL_ORDERS_FOR_STORE_SETTING, "context", "null",
				"value", "true"));

	}

	private void clickSummaryTab() {
		orderEditor.clickTab("Summary");
	}

	private void clickDetailsTab() {
		orderEditor.clickTab("Details");
	}

	private void clickPaymentsTab() {
		orderEditor.clickTab("Payments");
	}

	private void clickReturnsAndExchangeTab() {
		orderEditor.clickTab("Returns and exchanges");
	}

	private void clickNotesTab() {
		orderEditor.clickTab("Notes");
	}

	private void clickHoldTab() { orderEditor.clickTab("Holds"); }
}
