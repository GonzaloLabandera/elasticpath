package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Payment Authorization Wizard.
 */
public class PaymentAuthorizationWizard extends AbstractWizard {

	private static final String PAYMENT_AUTHORIZATION_PARENT_CSS = "div[widget-id='Payment Authorization'][widget-type='Shell'] ";
	private static final String PAYMENT_SOURCE_COMBO_CSS = PAYMENT_AUTHORIZATION_PARENT_CSS + "div[widget-id='Payment "
			+ "source'][widget-type='CCombo']";
	private static final String AUTHORIZE_BUTTON = PAYMENT_AUTHORIZATION_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.ReAuthWizard_Authorize_Button']";
	private static final String DONE_BUTTON = PAYMENT_AUTHORIZATION_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.ReAuthWizard_Done_Button']";
	private static final String CANCEL_REAUTHORIZATION_OK_BUTTON = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.CaptureWizard_Cancel_Title'] div[widget-id='OK']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public PaymentAuthorizationWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Selects payment source in combo box.
	 *
	 * @param paymentSource the payment source
	 */
	public void selectPaymentSource(final String paymentSource) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(PAYMENT_SOURCE_COMBO_CSS));
		assertThat(selectComboBoxItem(PAYMENT_SOURCE_COMBO_CSS, paymentSource))
				.as("Unable to find payment source - " + paymentSource)
				.isTrue();
	}

	/**
	 * Completes Payment Authorization.
	 *
	 * @param paymentSource the payment source
	 */
	public void completePaymentAuthorization(final String paymentSource) {
		selectPaymentSource(paymentSource);
		clickAuthorizeButton();
		clickDoneButton();
		waitTillElementDisappears(By.cssSelector(PAYMENT_AUTHORIZATION_PARENT_CSS));
	}

	/**
	 * Accepts Cancel Reauthorization.
	 * @param paymentSource the payment source
	 */
	public void cancelFailedPaymentAuthorization(final String paymentSource) {
		selectPaymentSource(paymentSource);
		clickAuthorizeButton();
		clickCancel();
		clickButton(CANCEL_REAUTHORIZATION_OK_BUTTON, "OK");
		waitTillElementDisappears(By.cssSelector(PAYMENT_AUTHORIZATION_PARENT_CSS));
	}

	/**
	 * Clicks Done button.
	 */
	public void clickDoneButton() {
		clickButton(DONE_BUTTON, "Done");
	}

	/**
	 * Completes Payment Authorization.
	 */
	public void clickAuthorizeButton() {
		assertThat(isAuthorizeButtonEnable())
				.as("Authorize button should be enable")
				.isTrue();
		clickNextInDialog();
	}

	/**
	 * Check is authorize button enable.
	 */
	public boolean isAuthorizeButtonEnable() {
		return isButtonEnabled(AUTHORIZE_BUTTON);
	}

	/**
	 * Check is finish button enable.
	 */
	public boolean isDoneButtonEnable() {
		return isButtonEnabled(DONE_BUTTON);
	}

	/**
	 * Verifies error message is displayed.
	 *
	 * @param errorMessage String
	 */
	public void verifyErrorMessageDisplayed(final String errorMessage) {
		setWebDriverImplicitWait(2);
		try {
			getDriver().findElement(By.cssSelector("div[widget-id*='" + errorMessage + "']"));
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message validation failed - '" + errorMessage + "'").isEqualTo(true);
		}
		setWebDriverImplicitWaitToDefault();
	}

}