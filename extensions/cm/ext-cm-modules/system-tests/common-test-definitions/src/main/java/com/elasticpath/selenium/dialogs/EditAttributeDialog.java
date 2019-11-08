package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Edit Attribute Dialog.
 */
public class EditAttributeDialog extends AbstractDialog {

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String EDIT_ATTRUBUTE_PARENT_CSS
			= "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.AttributeAddDialog_WinTitle_Edit'] ";
	private static final String ATTRIBUTE_LANGUAGE_CSS = EDIT_ATTRUBUTE_PARENT_CSS + "div[widget-id='Display Name'] ";
	private static final String ATTRUBUTE_NAME_INPUT_CSS = ATTRIBUTE_LANGUAGE_CSS + "+ div[widget-type='Text'] > input";
	private static final String OK_BUTTON_CSS = EDIT_ATTRUBUTE_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonOK'][seeable='true']";
	private static final String ATTRIBUTE_TYPE_SELECTOR_CSS = EDIT_ATTRUBUTE_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.AttributeAddDialog_AttributeType'][widget-type='CCombo']";


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public EditAttributeDialog(final WebDriver driver) {
		super(driver);
	}


	/**
	 * Inputs attribute name.
	 *
	 * @param attributeName the attribute name.
	 */
	public void enterAttributeName(final String attributeName) {
		clearAndType(ATTRUBUTE_NAME_INPUT_CSS, attributeName);

	}

	/**
	 * Returns attribute name.
	 *
	 * @return attribute name
	 */
	public String getAttributeName() {
		return getDriver().findElement(By.cssSelector(ATTRUBUTE_NAME_INPUT_CSS)).getAttribute("value");
	}

	/**
	 * Click ok button.
	 */
	public void clickOKButton() {
		clickButton(OK_BUTTON_CSS, "OK");
		waitTillElementDisappears(By.cssSelector(EDIT_ATTRUBUTE_PARENT_CSS));
	}

	/**
	 * Select language.
	 *
	 * @param language language which should be chosen.
	 */
	public void selectLanguage(final String language) {
		selectComboBoxItem(ATTRIBUTE_LANGUAGE_CSS, language);
	}

	/**
	 * Select attribute type.
	 *
	 * @param type attribute type which should be chosen.
	 */
	public void selectType(final String type) {
		selectComboBoxItem(ATTRIBUTE_TYPE_SELECTOR_CSS, type);
	}

	/**
	 * Returns attribute type.
	 *
	 * @return attribute field.
	 */
	public String getType() {
		return getDriver().findElement(By.cssSelector(ATTRIBUTE_TYPE_SELECTOR_CSS + " input")).getAttribute("value");
	}

}
