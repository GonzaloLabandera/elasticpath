package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Delete Confirm dialog.
 */
public class ConfirmDialog extends AbstractDialog {
	private static final String CONFIRM_OK_BUTTON_CSS = "div[automation-id*='%s'] div[widget-id='OK'][seeable='true']";
	private static final String NO_BUTTON_CSS = "div[automation-id*='%s'] div[widget-id='No'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ConfirmDialog(final WebDriver driver) {
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

	/**
	 * Clicks Cancel button.
	 */
	public void clickCancelButton() {
		clickButton(CANCEL_BUTTON_CSS, "Cancel");
		waitTillElementDisappears(By.cssSelector(CANCEL_BUTTON_CSS));
	}

	/**
	 * Clicks No button.
	 *
	 * @param dialogAutomationId the dialog automation id
	 */
	public void clickNoButton(final String dialogAutomationId) {
		clickButton(String.format(NO_BUTTON_CSS, dialogAutomationId), "No");
		waitTillElementDisappears(By.cssSelector(String.format(NO_BUTTON_CSS, dialogAutomationId)));
	}
}
