/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.SelectASkuDialog;

/**
 * Create Exchange Wizard.
 */
public class CreateExchangeWizard extends AbstractWizard {

	private static final String CREATE_EXCHANGE_PARENT_CSS = "div[widget-id='Create exchange'][widget-type='Shell'] ";
	private static final String EXCHANGE_RETURN_PARENT_TABLE_CSS = CREATE_EXCHANGE_PARENT_CSS + "div[widget-id='Return Subject Page "
			+ "Table'][widget-type='Table'] ";
	private static final String EXCHANGE_RETURN_COLUMN_CSS = EXCHANGE_RETURN_PARENT_TABLE_CSS + "div[row-id='%s'] div[column-id='0']";
	private static final String EXCHANGE_RETURN_COLUMN_INPUT_CSS = EXCHANGE_RETURN_PARENT_TABLE_CSS + " input:not([readonly])";
	private static final String EXCHANGE_ORDER_PARENT_TABLE_CSS = CREATE_EXCHANGE_PARENT_CSS + "div[widget-id='Exchange Order Items "
			+ "Table'][widget-type='Table'][seeable='true'] ";
	private static final String EXCHANGE_ORDER_COLUMN_CSS = EXCHANGE_ORDER_PARENT_TABLE_CSS + "div[column-id='%s']";
	private static final String ADD_ITEM_BUTTON_CSS = CREATE_EXCHANGE_PARENT_CSS + "div[widget-id='Add item...'][seeable='true']";
	private static final String REMOVE_ITEM_BUTTON_CSS = CREATE_EXCHANGE_PARENT_CSS + "div[widget-id='Remove item...'][seeable='true']";
	private static final String SHIPPING_ADDRESS_COMBO_CSS = CREATE_EXCHANGE_PARENT_CSS + "div[widget-id='Shipping method'][widget-type='CCombo']";
	private static final String SHIPPING_METHOD_COMBO_CSS = SHIPPING_ADDRESS_COMBO_CSS + "+div+div[widget-type='CCombo']";
	private static final String PAYMENT_SOURCE_COMBO_CSS = "+div div[widget-type='CCombo']";
	private static final String REQUIRED_RETURN_CHECKBOX_CSS = CREATE_EXCHANGE_PARENT_CSS + "div[appearance-id='check-box']";
	private static final String DIV_PREFIX_CSS = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String MANUAL_REFUND_RADIO_BUTTON_CSS
			= DIV_PREFIX_CSS + ".RefundOptionsComposite_ManualRefund_RadioButton'][widget-type='Button']";
	private static final String PAYMENT_SOURCE_RADIO_BUTTON = DIV_PREFIX_CSS + ".RefundWizard_PaymentSource'][appearance-id='radio-button']";
	private static final String ALTERNATE_PAYMENT_RADIO_BUTTON = DIV_PREFIX_CSS + ".RefundWizard_AlternatePaymentSource']"
			+ "[appearance-id='radio-button']";
	private static final String FINISH_BUTTON = "div[widget-id='Finish']";
	private static final String CANCEL_BUTTON = "div[widget-id='Cancel']";
	private static final String SHIPPING_COST_INPUT = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages"
			+ ".ExchangeWizard_ShippingCost_Label'] + div + div > input";
	private static final String NEW_ORDER_CREATED_LABEL = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages"
			+ ".ExchangeWizard_NewOrderCreatedCancelled_Label']";
	private static final String FALSE = "false";
	private static final String TRUE = "true";
	private static final String AUTHORIZE_BUTTON_LABEL = "Authorize";
	private String exchangeOrderNumber;


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateExchangeWizard(final WebDriver driver) {
		super(driver);
		setWizardType("Create exchange");
		setWizardStepCss("Step %s of 3 ");
	}

	/**
	 * Enters exchange return quantity.
	 *
	 * @param quantity int
	 * @param skuCode  String
	 */
	public void enterExchangeReturnQuantity(final int quantity, final String skuCode) {
		click(getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(EXCHANGE_RETURN_COLUMN_CSS, skuCode))));
		clearAndType(EXCHANGE_RETURN_COLUMN_INPUT_CSS, String.valueOf(quantity));
	}

	/**
	 * Clicks Add Item button.
	 *
	 * @return SelectASkuDialog
	 */
	public SelectASkuDialog clickAddItemButton() {
		clickButton(ADD_ITEM_BUTTON_CSS, "Add item...", SelectASkuDialog.SELECT_A_SKU_PARENT_CSS);
		return new SelectASkuDialog(getDriver());
	}

	/**
	 * Clicks Remove Item button.
	 */
	public void clickRemoveItemButton() {
		clickButton(REMOVE_ITEM_BUTTON_CSS, "Remove item...");
		new ConfirmDialog(getDriver()).clickOKButton("ExchangeWizard_RemoveLineItemConfirm");
	}

	/**
	 * Clicks Remove Item button.
	 *
	 * @param skuCode the sku code
	 */
	public void selectItemToExchange(final String skuCode) {
		getWaitDriver().waitForElementToBePresent(By.cssSelector(EXCHANGE_ORDER_PARENT_TABLE_CSS));
		assertThat(selectItemInDialog(EXCHANGE_ORDER_PARENT_TABLE_CSS, EXCHANGE_ORDER_COLUMN_CSS, skuCode, "SKU CODE"))
				.as("Unable to find sku - " + skuCode)
				.isTrue();
	}

	/**
	 * Selects Shipping Address.
	 *
	 * @param shippingAddress String
	 */
	public void selectShippingAddress(final String shippingAddress) {
		assertThat(selectComboBoxItem(SHIPPING_ADDRESS_COMBO_CSS, shippingAddress))
				.as("Unable to find shipping address - " + shippingAddress)
				.isTrue();
	}

	/**
	 * Selects Shipping Method.
	 *
	 * @param shippingMethod String
	 */
	public void selectShippingMethod(final String shippingMethod) {
		assertThat(selectComboBoxItem(SHIPPING_METHOD_COMBO_CSS, shippingMethod))
				.as("Unable to find shipping method - " + shippingMethod)
				.isTrue();
	}

	//TODO need isChecked method for checkBox

	/**
	 * Clicks Physical Return Required Before Refund checkbox.
	 */
	public void clickRequiredReturnCheckbox() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(REQUIRED_RETURN_CHECKBOX_CSS)));
	}

	/**
	 * Selects "Manual refund" radio button.
	 */
	public void selectManualRefund() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MANUAL_REFUND_RADIO_BUTTON_CSS)));
	}

	/**
	 * Selects payment source in combo box for new Reserve.
	 *
	 * @param exchangeMap exchange info map
	 */
	public void selectReservePaymentSource(final Map<String, String> exchangeMap) {
		String paymentSource = exchangeMap.get("Payment Source");
		if (exchangeMap.get("Payment Options").equals("Alternate payment source")) {
			click(ALTERNATE_PAYMENT_RADIO_BUTTON);
			assertThat(selectComboBoxItem(ALTERNATE_PAYMENT_RADIO_BUTTON + PAYMENT_SOURCE_COMBO_CSS, paymentSource))
					.as("Unable to find payment source - " + paymentSource)
					.isTrue();
		} else {
			click(PAYMENT_SOURCE_RADIO_BUTTON);
			assertThat(selectComboBoxItem(PAYMENT_SOURCE_RADIO_BUTTON + PAYMENT_SOURCE_COMBO_CSS, paymentSource))
					.as("Unable to find payment source - " + paymentSource)
					.isTrue();
		}
	}

	/**
	 * Creates Exchange.
	 *
	 * @param exchangeMap exchange info map
	 */
	public void createExchange(final Map<String, String> exchangeMap) {
		String returnSku = exchangeMap.get("Return Sku Code");
		enterExchangeReturnQuantity(Integer.parseInt(exchangeMap.get("Return Qty")), returnSku);
		clickNextInDialog();
		waitForWizardStep("2");

		String exchangeSku = exchangeMap.get("Exchange Sku Code");
		if (exchangeSku != null) {
			selectItemToExchange(returnSku);
			clickRemoveItemButton();

			SelectASkuDialog selectASkuDialog = clickAddItemButton();
			selectASkuDialog.selectSkuAndPriceList(exchangeSku, exchangeMap.get("Price List Name"));
		}
		selectShippingAddress(exchangeMap.get("Shipping Address"));
		selectShippingMethod(exchangeMap.get("Shipping Method"));
		clickNextInDialog();
		waitForWizardStep("3");

		final boolean returnRequired = Boolean.parseBoolean(exchangeMap.getOrDefault("Return Required", TRUE));
		final boolean manualRefund = Boolean.parseBoolean(exchangeMap.getOrDefault("Manual Refund", FALSE));
		final boolean paymentProcessingError = Boolean.parseBoolean(exchangeMap.getOrDefault("Error processing payment", FALSE));
		if (!returnRequired || manualRefund) {
			clickRequiredReturnCheckbox();
		}
		if (manualRefund) {
			selectManualRefund();
		}

		checkErrorMessageForAlternatePaymentSource(exchangeMap);
		selectReservePaymentSource(exchangeMap);
		clickAuthorizeInDialog();
		if (paymentProcessingError) {
			return;
		}
		final boolean willExchangeOrderCancelled = Boolean.parseBoolean(exchangeMap.getOrDefault("Exchange order will be cancelled", FALSE));
		if (willExchangeOrderCancelled) {
			String orderCreatedLabel = getDriver().findElement(By.cssSelector(NEW_ORDER_CREATED_LABEL)).getText();
			exchangeOrderNumber = orderCreatedLabel.substring(orderCreatedLabel.lastIndexOf(' ') + 1);
		}

		clickDone();
		waitTillElementDisappears(By.cssSelector(CREATE_EXCHANGE_PARENT_CSS));
	}

	/**
	 * Prepares Exchange.
	 *
	 * @param exchangeMap exchange info map
	 */
	public void prepareExchange(final Map<String, String> exchangeMap) {
		String returnSku = exchangeMap.get("Return Sku Code");
		enterExchangeReturnQuantity(Integer.parseInt(exchangeMap.get("Return Qty")), returnSku);
		verifyAuthorizeButtonIsDisabled();
		clickNextInDialog();
		waitForWizardStep("2");

		String exchangeSku = exchangeMap.get("Exchange Sku Code");
		if (exchangeSku != null) {
			selectItemToExchange(returnSku);
			clickRemoveItemButton();

			SelectASkuDialog selectASkuDialog = clickAddItemButton();
			selectASkuDialog.selectSkuAndPriceList(exchangeSku, exchangeMap.get("Price List Name"));
		}
		selectShippingAddress(exchangeMap.get("Shipping Address"));
		selectShippingMethod(exchangeMap.get("Shipping Method"));
		verifyAuthorizeButtonIsDisabled();
		clickNextInDialog();
		waitForWizardStep("3");
	}

	/**
	 * Verifies Authorize Exchange button is enabled.
	 */
	public void verifyAuthorizeButtonIsEnabled() {
		verifyButtonIsEnabled(FINISH_BUTTON, AUTHORIZE_BUTTON_LABEL);
	}

	/**
	 * Verifies Authorize Exchange button is disabled.
	 */
	public void verifyAuthorizeButtonIsDisabled() {
		verifyButtonIsDisabled(FINISH_BUTTON, AUTHORIZE_BUTTON_LABEL);
	}

	private void checkErrorMessageForAlternatePaymentSource(final Map<String, String> exchangeMap) {
		final boolean notSelectedPIMessage = Boolean.parseBoolean(exchangeMap.getOrDefault("Not Selected Payment Source error message", FALSE));
		if (notSelectedPIMessage) {
			click(ALTERNATE_PAYMENT_RADIO_BUTTON);
			clickAuthorizeInDialog();
			verifyErrorMessageDisplayed("Payment source should be selected.");
		}
	}

	private void verifyErrorMessageDisplayed(final String expErrorMessage) {
		try {
			getDriver().findElement(By.cssSelector(String.format("div[widget-id*='" + expErrorMessage + "'] ", expErrorMessage)));
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message not present - " + expErrorMessage)
					.isEqualTo(true);
		}
	}

	/**
	 * Create exchange for free item with no new reservation for exchange order.
	 *
	 * @param exchangeMap exchange info map
	 */
	public void createFreeItemExchange(final Map<String, String> exchangeMap) {
		String returnSku = exchangeMap.get("Return Sku Code");
		enterExchangeReturnQuantity(Integer.parseInt(exchangeMap.get("Return Qty")), returnSku);
		clickNextInDialog();
		waitForWizardStep("2");

		String exchangeSku = exchangeMap.get("Exchange Sku Code");
		if (exchangeSku != null) {
			selectItemToExchange(returnSku);
			clickRemoveItemButton();

			SelectASkuDialog selectASkuDialog = clickAddItemButton();
			selectASkuDialog.selectSkuAndPriceList(exchangeSku, exchangeMap.get("Price List Name"));
		}
		selectShippingAddress(exchangeMap.get("Shipping Address"));
		selectShippingMethod(exchangeMap.get("Shipping Method"));
		if (null != exchangeMap.getOrDefault("Adjusted Shipping Cost", "0")) {
			setShippingCostValue(exchangeMap.getOrDefault("Adjusted Shipping Cost", "0"));
		}
		clickNextInDialog();
		waitForWizardStep("3");

		final boolean returnRequired = Boolean.parseBoolean(exchangeMap.getOrDefault("Return Required", TRUE));
		final boolean manualRefund = Boolean.parseBoolean(exchangeMap.getOrDefault("Manual Refund", FALSE));
		if (!returnRequired || manualRefund) {
			clickRequiredReturnCheckbox();
		}
		if (manualRefund) {
			selectManualRefund();
		}
		clickAuthorizeInDialog();
		clickDone();
		waitTillElementDisappears(By.cssSelector(CREATE_EXCHANGE_PARENT_CSS));
	}

	/**
	 * Set new value in order Shipping Cost field
	 *
	 * @param cost new value to be set
	 */
	public void setShippingCostValue(final String cost) {
		clearAndTypeNonJSCheck(SHIPPING_COST_INPUT, cost);
		click(SHIPPING_COST_INPUT);
		WebElement element = getDriver().findElement(By.cssSelector(SHIPPING_COST_INPUT));
		((JavascriptExecutor) getDriver()).executeScript(String.format("arguments[0].value=\"%s\";", cost), element);
	}

	/**
	 * Clicks Next.
	 */
	public void clickAuthorizeInDialog() {
		clickButton(FINISH_BUTTON, AUTHORIZE_BUTTON_LABEL);
	}

	/**
	 * Clicks cancel.
	 */
	public void clickCancelInDialog() {
		clickButton(CANCEL_BUTTON, "Cancel");
	}

	public String getExchangeOrderNumber() {
		return exchangeOrderNumber;
	}
}