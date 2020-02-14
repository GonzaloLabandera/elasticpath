/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Create Refund Wizard.
 */
public class CreateRefundWizard extends AbstractWizard {

	private static final String CREATE_REFUND_PARENT_CSS = "div[widget-id='Create Refund'][widget-type='Shell'] ";
	private static final String REFUND_AMOUNT_INPUT_CSS = CREATE_REFUND_PARENT_CSS + "div[widget-id='%s'][widget-type='Text'] input";
	private static final String REFUND_NOTE_INPUT_CSS = CREATE_REFUND_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.RefundWizard_RefundNote'][widget-type='Text'] textarea";
	private static final String PAYMENT_SOURCE_COMBO_CSS = CREATE_REFUND_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient."
			+ "fulfillment.FulfillmentMessages.RefundWizard_PaymentSource']+div div[widget-type='CCombo']";
	private static final String PAYMENT_SOURCE_MANUAL_REFUND = CREATE_REFUND_PARENT_CSS + "div[widget-id='Manual refund'][widget-type='Button']";
	private static final String REFUND_BUTTON = "div[widget-id='Next >']";
	private static final String AVAILABLE_REFUND_AMOUNT = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages"
			+ ".OrderEditor_CreateRefund_AvailableRefundAmount']";
	private static final String PAYMENT_ERROR_POPUP = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages"
			+ ".RefundWizard_PaymentProceedError_Title']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateRefundWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs refund amount.
	 *
	 * @param currencyCode the currency code
	 * @param refundAmount the refund amount
	 */
	public void enterRefundAmount(final String currencyCode, final String refundAmount) {
		clearAndType(String.format(REFUND_AMOUNT_INPUT_CSS, currencyCode), refundAmount);

	}

	/**
	 * Inputs refund note.
	 *
	 * @param refundNote the refund note
	 */
	public void enterRefundNote(final String refundNote) {
		clearAndType(REFUND_NOTE_INPUT_CSS, refundNote);
	}

	/**
	 * Selects payment source in combo box.
	 *
	 * @param paymentSource the payment source
	 */
	public void selectPaymentSource(final String paymentSource) {
		if ("Manual Refund".equals(paymentSource)) {
			clickButton(PAYMENT_SOURCE_MANUAL_REFUND, paymentSource);
		} else {
			assertThat(selectComboBoxItem(PAYMENT_SOURCE_COMBO_CSS, paymentSource))
					.as("Unable to find payment source - " + paymentSource)
					.isTrue();
		}
	}

	/**
	 * Creates refund.
	 *
	 * @param refundMap map of refund item info
	 */
	public void createRefund(final Map<String, String> refundMap) {
		enterRefundAmount(refundMap.get("Currency Code"), refundMap.get("Refund Amount"));
		enterRefundNote(refundMap.get("Refund Note"));
		selectPaymentSource(refundMap.get("Payment Source"));
		verifyRefundButtonEnabled();
		clickNextInDialog();
		if (!hasError("Payment gateway or plugin issue")) {
			clickFinish();
			waitTillElementDisappears(By.cssSelector(CREATE_REFUND_PARENT_CSS));
		}
	}

	/**
	 * Verifies if dialog has an error.
	 *
	 * @return true if error dialog is displayed
	 */
	public boolean hasError(final String errorTitle) {
		setWebDriverImplicitWait(1);
		boolean hasError = false;
		try {
			if (getDriver().findElement(By.cssSelector("div[widget-id*='" + errorTitle + "']")).isDisplayed()) {
				hasError = true;
				return hasError;
			}
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message not exist - '" + errorTitle);
		}
		setWebDriverImplicitWaitToDefault();
		return hasError;
	}

	/**
	 * Verifies error message is displayed.
	 *
	 * @param expErrorMessage String
	 */
	public void verifyErrorMessageDisplayed(final String expErrorMessage) {
		setWebDriverImplicitWait(2);
		try {
			getDriver().findElement(By.cssSelector("div[widget-id*='" + expErrorMessage + "']"));
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message validation failed - '" + expErrorMessage + "'").isEqualTo(true);
		}
		setWebDriverImplicitWaitToDefault();
		clickCancel();
		waitTillElementDisappears(By.cssSelector(CREATE_REFUND_PARENT_CSS));
	}

	/**
	 * verifies payment error message.
	 *
	 * @param expErrorMessage String
	 */
	public void verifyPaymentError(final String expErrorMessage) {
		setWebDriverImplicitWait(2);
		try {
			getDriver().findElement(By.cssSelector("div[widget-id*='" + expErrorMessage + "']"));
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message validation failed - '" + expErrorMessage + "'").isEqualTo(true);
		}
		setWebDriverImplicitWaitToDefault();
		clickOk();
		waitTillElementDisappears(By.cssSelector(PAYMENT_ERROR_POPUP));
		clickCancel();
		waitTillElementDisappears(By.cssSelector(CREATE_REFUND_PARENT_CSS));
	}

	/**
	 * verifies exact payment error message. Make sense when you check if the message is not json.
	 *
	 * @param exactErrorMessage String
	 */
	public void verifyExactPaymentError(final String exactErrorMessage) {
		setWebDriverImplicitWait(2);
		try {
			getDriver().findElement(By.cssSelector("div[widget-id='" + exactErrorMessage + "']"));
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message validation failed - '" + exactErrorMessage + "'").isEqualTo(true);
		}
		setWebDriverImplicitWaitToDefault();
		clickOk();
		waitTillElementDisappears(By.cssSelector(PAYMENT_ERROR_POPUP));
		clickCancel();
		waitTillElementDisappears(By.cssSelector(CREATE_REFUND_PARENT_CSS));
	}

	/**
	 * Fill order refund form.
	 *
	 * @param refundMap map of refund item info
	 */
	public void fillOrderRefundForm(final Map<String, String> refundMap) {
		enterRefundAmount(refundMap.get("Currency Code"), refundMap.get("Refund Amount"));
		enterRefundNote(refundMap.get("Refund Note"));
		selectPaymentSource(refundMap.get("Payment Source"));
	}

	/**
	 * Verify is refund button disabled.
	 */
	public void verifyRefundButtonDisabled() {
		verifyButtonIsDisabled(REFUND_BUTTON, "Refund");
	}

	/**
	 * Verify is refund button enabled.
	 */
	public void verifyRefundButtonEnabled() {
		verifyButtonIsEnabled(REFUND_BUTTON, "Refund");
	}

	/**
	 * Verify available refund amount.
	 *
	 * @param refundAmount String
	 */
	public void verifyAvailableRefundAmount(final String refundAmount) {
		assertThat(getDriver().findElement(By.cssSelector(AVAILABLE_REFUND_AMOUNT)).getText())
				.as("Available refund amount does not match ")
				.contains(refundAmount);
	}

}