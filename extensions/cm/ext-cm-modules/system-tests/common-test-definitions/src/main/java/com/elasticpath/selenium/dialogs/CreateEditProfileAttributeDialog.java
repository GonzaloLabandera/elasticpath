package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Create Warehouse Dialog.
 */
public class CreateEditProfileAttributeDialog extends AbstractDialog {

	private static final String CREATE_EDIT_PROFILE_ATTRIBUTE_DIALOG_CSS = "div[automation-id*='com.elasticpath.cmclient.admin.customers"
			+ ".AdminCustomersMessages.%sAttribute'] ";
	private static final String CREATE_EDIT_PROFILE_ATTRIBUTE_PARENT_INPUT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.customers"
			+ ".AdminCustomersMessages";
	private static final String PROFILE_ATTRIBUTE_KEY_INPUT_CSS = CREATE_EDIT_PROFILE_ATTRIBUTE_PARENT_INPUT_CSS + ".AttributeKey'] input";
	private static final String PROFILE_ATTRIBUTE_NAME_INPUT_CSS = CREATE_EDIT_PROFILE_ATTRIBUTE_PARENT_INPUT_CSS + ".AttributeName'] input";
	private static final String PROFILE_ATTRIBUTE_TYPE_COMBO_CSS = CREATE_EDIT_PROFILE_ATTRIBUTE_PARENT_INPUT_CSS
			+ ".AttributeType'][widget-type='CCombo']";
	private static final String SAVE_BUTTON_CSS = "div[automation-id='com.elasticpath.cmclient.core.CoreMessages"
			+ ".AbstractEpDialog_ButtonSave'][style*='opacity: 1']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateEditProfileAttributeDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs profile attribute key.
	 *
	 * @param profileAttributeKey String
	 */
	public void enterProfileAttributeKey(final String profileAttributeKey) {
		clearAndType(PROFILE_ATTRIBUTE_KEY_INPUT_CSS, profileAttributeKey);
	}

	/**
	 * Inputs profile attribute name.
	 *
	 * @param profileAttributeName String
	 */
	public void enterProfileAttributeName(final String profileAttributeName) {
		clearAndType(PROFILE_ATTRIBUTE_NAME_INPUT_CSS, profileAttributeName);
	}

	/**
	 * Selects profile attribute type in combo box.
	 *
	 * @param profileAttributeType String
	 */
	public void selectProfileAttributeType(final String profileAttributeType) {
		assertThat(selectComboBoxItem(PROFILE_ATTRIBUTE_TYPE_COMBO_CSS, profileAttributeType))
				.as("Unable to find state - " + profileAttributeType)
				.isTrue();
	}

	/**
	 * Clicks save button.
	 */
	public void clickSaveButton() {
		clickButton(SAVE_BUTTON_CSS, "Save");
		waitTillElementDisappears(By.cssSelector(CREATE_EDIT_PROFILE_ATTRIBUTE_DIALOG_CSS));
	}
}
