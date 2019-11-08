/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * A selenium representation of the AddEditTagGroupDialog for interacting with the dialog during selenium test execution.
 */
public class CreateEditTagGroupDialog extends AbstractDialog {

	/**
	 * The xpath id of the add/edit dialog.  Use String.format(CREATE_EDIT_TAG_GROUP_DIALOG_CSS_TEMPLATE, {add|edit}) to specify which dialog.
	 */
	public static final String ADD_EDIT_TAG_GROUP_DIALOG_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages.%sGroupDialogTitle'][widget-type='Shell'] ";
	private static final String CREATE_EDIT_TAG_GROUP_PARENT_INPUT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages";

	private static final String TAG_GROUP_CODE_INPUT_CSS = CREATE_EDIT_TAG_GROUP_PARENT_INPUT_CSS + ".guidLabel'] input";
	private static final String TAG_GROUP_DISPLAY_NAME_INPUT_CSS = "//div[@automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages.nameLabel']/..//following::div[@widget-type='Text']/input";
	private static final String SAVE_BUTTON_CSS = "div[widget-id='Save']";

	private final String parentCss;

	/**
	 * Create a new instance.
	 *
	 * @param driver     the web driver for this test execution
	 * @param dialogName the type of dialog - must be one of "edit" or "add"
	 */
	public CreateEditTagGroupDialog(final WebDriver driver, final String dialogName) {
		super(driver);
		parentCss = String.format(ADD_EDIT_TAG_GROUP_DIALOG_PARENT_CSS, dialogName);
	}

	/**
	 * Fill in the value for tag group code
	 *
	 * @param tagGroupCode the value to place in the code input field
	 */
	public void enterTagGroupCode(final String tagGroupCode) {
		clearAndType(TAG_GROUP_CODE_INPUT_CSS, tagGroupCode);
	}

	/**
	 * Fill in the value for tag group display name
	 *
	 * @param tagGroupDisplayName the value to place in the display name input field
	 */
	public void enterTagGroupDisplayName(final String tagGroupDisplayName) {
		WebElement displayNameTextField = getDriver().findElement(By.xpath(TAG_GROUP_DISPLAY_NAME_INPUT_CSS));
		clearAndType(displayNameTextField, tagGroupDisplayName);
	}

	/**
	 * Click the save button to save this tag group.
	 */
	public void clickSaveButton() {
		clickButton(parentCss + SAVE_BUTTON_CSS, "Save");
		waitTillElementDisappears(By.cssSelector(parentCss));
	}

}
