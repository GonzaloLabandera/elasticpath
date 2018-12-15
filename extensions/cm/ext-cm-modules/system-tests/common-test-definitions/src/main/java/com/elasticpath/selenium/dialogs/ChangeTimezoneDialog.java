package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Change Timezone Dialog.
 */
public class ChangeTimezoneDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String CHANGE_TIMEZONE_PARENT = "div[widget-id='Change Time Zone'][widget-type='Shell'] ";
	private static final String USE_CUSTOM_TIMEZONE = CHANGE_TIMEZONE_PARENT + "div[widget-id='Use custom time zone']";
	private static final String USE_BROWSER_TIMEZONE = CHANGE_TIMEZONE_PARENT
			+ "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.ChangeTimezoneDialog_Browser']";
	private static final String CUSTOM_TIMEZONE_COMBOBOX = CHANGE_TIMEZONE_PARENT
			+ "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.ChangeTimezoneDialog_Description'][appearance-id='ccombo']";
	private static final String COMBOBOX_OPTION_INPUT = CUSTOM_TIMEZONE_COMBOBOX + ">div>input";
	private static final String SAVE_BUTTON_CSS = CHANGE_TIMEZONE_PARENT + "div[widget-id='Save']";
	private static final String CANCEL_BUTTON_CSS = CHANGE_TIMEZONE_PARENT + "div[widget-id='Cancel']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ChangeTimezoneDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Selects timezone from the dropdown.
	 *
	 * @param timeZone which should be chosen
	 */
	public void selectTimezone(final String timeZone) {
		click(By.cssSelector(USE_CUSTOM_TIMEZONE));
		selectComboBoxItem(CUSTOM_TIMEZONE_COMBOBOX, timeZone);
	}

	/**
	 * Selects a browser timezone from the dropdown.
	 */
	public void selectBrowserTimezone() {
		click(By.cssSelector(USE_BROWSER_TIMEZONE));
	}

	/**
	 * Clicks Save button.
	 */
	public void clickSaveButton() {
		clickButton(SAVE_BUTTON_CSS, "Save");
	}

	/**
	 * Checks if custom time zone is selected.
	 *
	 * @return true if custom time zone is selected, else returns false
	 */
	public boolean isCustomTimeZoneSelected() {
		if (isSelected(USE_CUSTOM_TIMEZONE)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if browser time zone is selected.
	 *
	 * @return true if browser time zone is selected, else returns false
	 */
	public boolean isBrowserTimeZoneSelected() {
		if (isSelected(USE_BROWSER_TIMEZONE)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns custom time zone value.
	 */
	public String getCustomTimeZone() {
		return getDriver().findElement(By.cssSelector(COMBOBOX_OPTION_INPUT)).getAttribute("value");
	}

	/**
	 * Clicks cancel button.
	 */
	public void clickCancelButton() {
		clickButton(CANCEL_BUTTON_CSS, "Cancel");
		waitTillElementDisappears(By.cssSelector(CHANGE_TIMEZONE_PARENT));
	}
}