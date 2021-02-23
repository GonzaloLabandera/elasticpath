package com.elasticpath.selenium.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.util.Constants;

/**
 * Payment Authorization Wizard.
 */
public class PaymentAuthorizationWizard extends AbstractWizard {

	private static final String PAYMENT_AUTHORIZATION_PARENT_CSS = "div[widget-id='Payment Reservation'][widget-type='Shell'] ";
	private static final String PAYMENT_SOURCE_COMBO_CSS = PAYMENT_AUTHORIZATION_PARENT_CSS + "div[widget-id='Payment "
			+ "source'][widget-type='CCombo']";
	private static final String AUTHORIZE_BUTTON = PAYMENT_AUTHORIZATION_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.ReAuthWizard_Authorize_Button']";
	private static final String DONE_BUTTON = PAYMENT_AUTHORIZATION_PARENT_CSS + "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.ReAuthWizard_Done_Button']";
	private static final String CANCEL_REAUTHORIZATION_OK_BUTTON = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.CaptureWizard_Cancel_Title'] div[widget-id='OK']";
	private static final String RESERVATIONS_TABLE_CSS = "div[widget-id='Auth Summary Page Table'][widget-type='Table'][seeable='true']";
	private static final String RESERVATIONS_TABLE_ROW_CSS = RESERVATIONS_TABLE_CSS + " div[widget-type='table_row']";
	private static final String PAYMENT_SOURCE_COLUMN_NAME = "Payment source";
	private static final String STATUS_COLUMN_NAME = "Status";
	private static final String COLUMN_NUM_CSS = "div[column-num='%d']";

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

	/**
	 * Verifies payment reservations.
	 *
	 * @param expectedReservations the expected reservations.
	 */
	public void verifyReservations(final Map<String, String> expectedReservations) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		final List<WebElement> reservations = getDriver().findElements(By.cssSelector(RESERVATIONS_TABLE_ROW_CSS));
		for (WebElement reservation : reservations) {
			final String paymentSource = getReservationColumnValue(reservation, 0);
			final String status = getReservationColumnValue(reservation, 2);
			final boolean paymentSourceMatch = !expectedReservations.containsKey(PAYMENT_SOURCE_COLUMN_NAME)
					|| expectedReservations.get(PAYMENT_SOURCE_COLUMN_NAME).equals(paymentSource);
			final boolean statusMatch = !expectedReservations.containsKey(STATUS_COLUMN_NAME)
					|| expectedReservations.get(STATUS_COLUMN_NAME).equals(status);
			assertThat(paymentSourceMatch && statusMatch)
					.as("Could not find expected payment reservation")
					.isTrue();
		}
	}

	private String getReservationColumnValue(final WebElement paymentTransaction, final int columnNumber) {
		setWebDriverImplicitWait(0);
		final List<WebElement> cellElement = paymentTransaction.findElements(By.cssSelector(String.format(COLUMN_NUM_CSS, columnNumber)));
		setWebDriverImplicitWaitToDefault();
		return cellElement.isEmpty()
				? StringUtils.EMPTY
				: cellElement.get(0).getText();
	}
}