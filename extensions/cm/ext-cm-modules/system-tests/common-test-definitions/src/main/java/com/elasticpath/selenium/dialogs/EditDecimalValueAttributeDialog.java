package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Edit Decimal Value Dialog.
 */
public class EditDecimalValueAttributeDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String PARENT_DECIMAL_VALUE_CSS = "div[widget-id='Edit Decimal Value'][widget-type='Shell'] ";
	private static final String INPUT_CSS = PARENT_DECIMAL_VALUE_CSS + "input";
	private static final String OK_BUTTON_CSS = PARENT_DECIMAL_VALUE_CSS + "div[widget-id='OK']";
	private static final String INPUT_EDIT_CSS = "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.AttributeShortTextDialog_WindowTitle'] "
			+ "[appearance-id = 'text-area'] > textarea";
	private static final String OK_CSS = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonOK']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public EditDecimalValueAttributeDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs decimal attribute value.
	 *
	 * @param decimalValue the decimal value.
	 */
	public void enterDecimalValue(final String decimalValue) {
		clearAndType(INPUT_CSS, decimalValue);
	}

	/**
	 * Inputs attribute value.
	 *
	 * @param value the attribute value.
	 */
	public void enterValue(final String value) {
		clearAndType(INPUT_EDIT_CSS, value);
	}

	/**
	 * Click ok button.
	 */
	public void clickOKButton() {
		clickButton(OK_BUTTON_CSS, "OK");
		waitTillElementDisappears(By.cssSelector(OK_BUTTON_CSS));
	}

	/**
	 * Click ok button on attribute dialog.
	 */
	public void clickOKOnAttributeDialog() {
		clickButton(OK_CSS, "OK");
		waitTillElementDisappears(By.cssSelector(OK_CSS));
	}
}
