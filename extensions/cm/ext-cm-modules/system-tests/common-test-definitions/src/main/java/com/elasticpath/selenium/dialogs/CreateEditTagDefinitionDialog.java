/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * A selenium dialog class that provides functionality for interacting with the EditTagDefinitionDialog.
 */
public class CreateEditTagDefinitionDialog extends AbstractDialog {

	/**
	 * The css selector for the tag dictionary table.
	 */
	public static final String TAG_DICTIONARY_TABLE_CSS = "div[widget-id='Dictionaries'][widget-type='Table'] ";

	/**
	 * The display options for this dialog.
	 */
	public enum DIALOGTYPE {
		add,
		edit
	}

	private static final String ADD_EDIT_TAG_DEFINITION_DIALOG_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages.%sTagDefinitionDialogTitle'][widget-type='Shell'] ";
	private static final String CREATE_EDIT_TAG_DEFINITION_PARENT_INPUT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages";
	private static final String TAG_DEFINITION_CODE_INPUT_CSS = CREATE_EDIT_TAG_DEFINITION_PARENT_INPUT_CSS + ".TagDefinition_Label_TagCode'] "
			+ "input";
	private static final String TAG_DEFINITION_NAME_INPUT_CSS = CREATE_EDIT_TAG_DEFINITION_PARENT_INPUT_CSS + ".TagDefinition_Label_TagName'] "
			+ "input";
	private static final String TAG_DEFINITION_DESCRIPTION_INPUT_CSS = CREATE_EDIT_TAG_DEFINITION_PARENT_INPUT_CSS
			+ ".TagDefinition_Label_Description'] textarea";
	private static final String TAG_DEFINITION_DISPLAY_NAME_INPUT_CSS = "//div[@automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages.TagDefinition_Label_Name']/..//div[@widget-type='Text']/input";
	private static final String TAG_DEFINITION_LANGUAGE_COMBO_CSS = CREATE_EDIT_TAG_DEFINITION_PARENT_INPUT_CSS
			+ ".TagDefinition_Label_Name'][widget-type='CCombo']";
	private static final String TAG_DEFINITION_FIELD_TYPE_COMBO_CSS = CREATE_EDIT_TAG_DEFINITION_PARENT_INPUT_CSS
			+ ".TagDefinition_Label_FieldType'][widget-type='CCombo']";
	private static final String TAG_DEFINITION_DICTIONARY_CHECKBOX_ROW = "//div[@automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages.%sTagDefinitionDialogTitle']//div[@widget-id='%s']//div[1]";
	private static final String SAVE_BUTTON_CSS = "div[widget-id='Save']";
	private static final String TAG_DICTIONARY_CODE_COLUMN = "div[column-id='%s']";

	private final DIALOGTYPE dialogType;

	/**
	 * Create a new instance.
	 *
	 * @param driver the instance of the web driver for this test
	 */
	public CreateEditTagDefinitionDialog(final WebDriver driver, final DIALOGTYPE dialogType) {
		super(driver);
		this.dialogType = dialogType;
	}

	/**
	 * Set the value in the tag definition code input field.
	 *
	 * @param newTagDefCode the tag definition code to inject
	 */
	public void enterTagDefinitionCode(final String newTagDefCode) {
		clearAndType(TAG_DEFINITION_CODE_INPUT_CSS, newTagDefCode);
	}

	/**
	 * Set the value in the tag definition name input field.
	 *
	 * @param tagDefName the tag definition name to inject
	 */
	public void enterTagDefinitionName(final String tagDefName) {
		clearAndType(TAG_DEFINITION_NAME_INPUT_CSS, tagDefName);
	}

	/**
	 * Set the value in the tag definition name input field.
	 *
	 * @param tagDefDesc the tag definition description to inject
	 */
	public void enterTagDefinitionDescription(final String tagDefDesc) {
		clearAndType(TAG_DEFINITION_DESCRIPTION_INPUT_CSS, tagDefDesc);
	}

	/**
	 * Set the value in the tag definition display name input field.
	 *
	 * @param tagDefDisplayName the tag definition display name to inject
	 */
	public void enterTagDefinitionDisplayName(final String tagDefDisplayName) {
		WebElement displayNameTextField = getDriver().findElement(By.xpath(TAG_DEFINITION_DISPLAY_NAME_INPUT_CSS));
		clearAndType(displayNameTextField, tagDefDisplayName);
	}

	/**
	 * Select the value in the tag definition display name language selection field
	 *
	 * @param language the language to select - must be a valid locale identifier
	 */
	public void selectTagDefinitionLanguage(final String language) {
		Locale locale = LocaleUtils.toLocale(language);
		assertThat(selectComboBoxItem(TAG_DEFINITION_LANGUAGE_COMBO_CSS, locale.getDisplayName()))
				.as("Unable to find language selection - " + language)
				.isTrue();
	}

	/**
	 * Select the value in the tag definition field type selection field
	 *
	 * @param fieldType the fieldType to select - must be a valid fieldType
	 */
	public void selectFieldType(final String fieldType) {
		assertThat(selectComboBoxItem(TAG_DEFINITION_FIELD_TYPE_COMBO_CSS, fieldType))
				.as("Unable to find field type - " + fieldType)
				.isTrue();
	}

	/**
	 * Select the dictionaries that the tag will belong to from the table of all dictionaries.
	 *
	 * @param dictionaryNames a String array of dictionary guids - must be valid dictionary guids
	 */
	public void selectTagDefinitionDictionaries(final String[] dictionaryNames) {
		for (String dictionary : dictionaryNames) {
			String xpathForDictionary = String.format(TAG_DEFINITION_DICTIONARY_CHECKBOX_ROW, dialogType, dictionary);
			selectItemInDialog(TAG_DICTIONARY_TABLE_CSS, TAG_DICTIONARY_TABLE_CSS + TAG_DICTIONARY_CODE_COLUMN, dictionary, "Code");
			WebElement dictionaryCheckBox = getDriver().findElement(By.xpath(xpathForDictionary));
			clickWithoutScrollWidgetIntoView(dictionaryCheckBox);
		}
	}

	/**
	 * Clicks the save button to save the tag definition.
	 */
	public void clickSaveButton() {
		String dialogParentCss = String.format(ADD_EDIT_TAG_DEFINITION_DIALOG_PARENT_CSS, dialogType);
		clickButton(dialogParentCss + SAVE_BUTTON_CSS, "Save");
		waitTillElementDisappears(By.cssSelector(dialogParentCss));
	}
}
