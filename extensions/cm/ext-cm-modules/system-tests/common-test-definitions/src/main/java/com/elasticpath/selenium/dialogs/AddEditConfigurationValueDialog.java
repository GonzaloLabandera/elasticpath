package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;

/**
 *  Add Configuration Value dialog
 */
public class AddEditConfigurationValueDialog extends AbstractDialog {

	private static final String ADD_CONFIGURATION_VALUE_PARENT_CSS = "div[widget-id*=' Configuration Value'][widget-type='Shell']";
	private static final String CONTEXT_INPUT_FIELD_CSS = ADD_CONFIGURATION_VALUE_PARENT_CSS + " div[widget-id='Context'] > input";
	private static final String CONFIGURATION_VALUE_TEXT_AREA_CSS = ADD_CONFIGURATION_VALUE_PARENT_CSS + " textarea:not([readonly=''])";
	private static final String SAVE_BUTTON_CSS = "div[appearance-id='push-button'][widget-id='Save'][widget-type='Button']";
	private static final String CANCEL_BUTTON_CSS = "div[appearance-id='push-button'][widget-id='Cancel'][widget-type='Button']";
	private static final String EXCLAMATION_IMAGE_CSS = "div[widget-id='Must be a valid boolean value']";

	/**
	 *  Constructor
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public AddEditConfigurationValueDialog(final WebDriver driver) { super(driver); }

	/**
	 *  Clicks Save button
	 */
	public ConfigurationActionToolbar clickSaveButton() {
		getWaitDriver().waitForButtonToBeEnabled(SAVE_BUTTON_CSS);
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(SAVE_BUTTON_CSS)));
		return new ConfigurationActionToolbar(getDriver());
	}

	/**
	 *  Clicks Cancel button
	 */
	public ConfigurationActionToolbar clickCancelButton() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CANCEL_BUTTON_CSS)));
		return new ConfigurationActionToolbar(getDriver());
	}

	/**
	 *  Enters new value into Context field
	 *
	 * @param context value to be entered
	 */
	public void enterContextValue(final String context) {
		clearAndType(getWaitDriver().waitForElementToBeVisible(By.cssSelector(CONTEXT_INPUT_FIELD_CSS)), context);
	}

	/**
	 *  Enters new value into the Textarea WebElement
	 *
	 * @param text value to be entered
	 */
	public void enterTextareaValue(final String text) {
		clearAndType(getWaitDriver().waitForElementToBeVisible(By.cssSelector(CONFIGURATION_VALUE_TEXT_AREA_CSS)), text);
	}

	/**
	 *  Verify that Exclamation mark image displayed
	 *  in case of incorrect text value entered
	 *
	 * @return true/false
	 */
	public boolean verifyWarningImageDisplayed() {
		try {
			getDriver().findElement(By.cssSelector(EXCLAMATION_IMAGE_CSS));
		} catch (NoSuchElementException e) {
			return false;
		}
		return true;
	}
}
