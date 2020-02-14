package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Delete Confirm dialog.
 */
public class ShipmentCompletionErrorDialog extends AbstractDialog {
	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String DIALOG_CSS = "div[automation-id*='%s'] ";
	public static final String CONFIRM_OK_BUTTON_CSS = DIALOG_CSS + "div[widget-id='OK'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ShipmentCompletionErrorDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks OK in confirmation dialog.
	 *
	 * @param dialogAutomationId the dialog automation id
	 */
	public void clickOKButton(final String dialogAutomationId) {
		clickButton(String.format(CONFIRM_OK_BUTTON_CSS, dialogAutomationId), "Confirm_OK");
		waitTillElementDisappears(By.cssSelector(String.format(CONFIRM_OK_BUTTON_CSS, dialogAutomationId)));
	}
}
