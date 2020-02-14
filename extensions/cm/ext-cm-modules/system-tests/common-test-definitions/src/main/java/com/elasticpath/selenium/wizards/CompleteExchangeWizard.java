/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.wizards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Complete Return Wizard.
 */
public class CompleteExchangeWizard extends AbstractWizard {

	private static final String DIV_PREFIX = "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages";
	private static final String COMPLETE_RETURN_PARENT_CSS =
			DIV_PREFIX + ".ExchangeWizard_Complete_Title'] ";
	private static final String REFUND_TO_ORIGINAL_RADIO_BUTTON_CSS =
			DIV_PREFIX + ".RefundOptionsComposite_ReturnToOriginal_RadioButton'][widget-type='Button']";
	private static final String MANUAL_REFUND_RADIO_BUTTON_CSS =
			DIV_PREFIX + ".RefundOptionsComposite_ManualRefund_RadioButton'][widget-type='Button']";
	private static final String FINISH_BUTTON = "div[widget-id='Finish']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CompleteExchangeWizard(final WebDriver driver) {
		super(driver);
		setWizardType("Complete exchange");
		setWizardStepCss("Step %s of 2 ");
	}

	/**
	 * Completes return.
	 *
	 * @param manualRefund uses manual refund option
	 */
	public void completeExchange(final boolean manualRefund) {
		if (manualRefund) {
			selectManualRefund();
		} else {
			selectRefundToOriginal();
		}
		clickFinish();
		clickDone();
		waitTillElementDisappears(By.cssSelector(COMPLETE_RETURN_PARENT_CSS));
	}

	/**
	 * Completes return, but error is expected .
	 *
	 * @param manualRefund uses manual refund option
	 */
	public void completeExchangeWithError(final boolean manualRefund) {
		if (manualRefund) {
			selectManualRefund();
		} else {
			selectRefundToOriginal();
		}
		clickButton(FINISH_BUTTON, "Finish");
	}

	public void selectRefundToOriginal() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(REFUND_TO_ORIGINAL_RADIO_BUTTON_CSS)));
	}

	public void selectManualRefund() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MANUAL_REFUND_RADIO_BUTTON_CSS)));
	}

	/**
	 * Clicks cancel.
	 */
	public void clickCancelInDialog() {
		clickCancel();
		waitTillElementDisappears(By.cssSelector(COMPLETE_RETURN_PARENT_CSS));
	}

}