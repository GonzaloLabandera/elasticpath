/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.CreateEditTagDefinitionDialog;
import com.elasticpath.selenium.dialogs.CreateEditTagGroupDialog;
import com.elasticpath.selenium.util.Constants;

/**
 * A selenium representation of the TagGroupView.  Allows for interacting with this view during selenium test execution.
 */
public class TagsResultPane extends AbstractPageObject {

	private static final String TAG_GROUP_LIST_PARENT_CSS = "div[widget-id='Tag Groups'][widget-type='Table'] ";
	private static final String TAG_GROUP_LIST_CSS = TAG_GROUP_LIST_PARENT_CSS + "div[column-id='%s']";
	private static final String CREATE_TAG_GROUP_BUTTON_CSS = "div[widget-id='Add Group'][seeable='true']";
	private static final String REMOVE_TAG_GROUP_BUTTON_CSS = "div[widget-id='Remove Group'][seeable='true']";
	private static final String EDIT_TAG_GROUP_BUTTON_CSS = "div[widget-id='Edit Group'][seeable='true']";
	private static final String CREATE_TAG_DEFINITION_BUTTON_CSS = "div[widget-id='Add Tag'][seeable='true']";
	private static final String REMOVE_TAG_DEFINITION_BUTTON_CSS = "div[widget-id='Remove Tag'][seeable='true']";
	private static final String EDIT_TAG_DEFINITION_BUTTON_CSS = "div[widget-id='Edit Tag'][seeable='true']";
	private static final String TAG_DEFINITION_LIST_CODE_COLUMN_CSS = "div[column-id='%s']";
	private static final String TAG_DEFINITION_LIST_PARENT_CSS = "div[widget-id='Tag Definitions'][widget-type='Table'] ";
	private static final String CODE_COLUMN_NAME = "Code";
	private static final String MAXIMIZE_WINDOW_CSS = "div[pane-location='center-pane-outer'] div[appearance-id='ctabfolder-button']"
			+ "[widget-id='Maximize'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public TagsResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Maximize Tags window.
	 */
	public void maximizeTagsWindow() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_THREE_SECONDS);
		if (getDriver().findElements(By.cssSelector(MAXIMIZE_WINDOW_CSS)).size() != 0) {
			click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(MAXIMIZE_WINDOW_CSS)));
		}
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks the button to edit a tag group and returns the edit dialog.
	 *
	 * @param tagGroupCode the guid/code of the tag group that must exist in the list
	 * @return a new instance of the CreateEditTagGroupDialog
	 */
	public CreateEditTagGroupDialog clickEditTagGroupButton(final String tagGroupCode) {
		verifyTagGroupExists(tagGroupCode);
		final String dialogName = "edit";
		clickButton(EDIT_TAG_GROUP_BUTTON_CSS, "Edit Group", String.format(CreateEditTagGroupDialog
				.ADD_EDIT_TAG_GROUP_DIALOG_PARENT_CSS, dialogName));
		return new CreateEditTagGroupDialog(getDriver(), dialogName);
	}

	/**
	 * Clicks the button to add a new tag group and returns the create dialog.
	 *
	 * @return a new instance of the CreateEditTagGroupDialog
	 */
	public CreateEditTagGroupDialog clickAddNewTagGroupButton() {
		final String dialogName = "add";
		clickButton(CREATE_TAG_GROUP_BUTTON_CSS, "Add Group", String.format(CreateEditTagGroupDialog
				.ADD_EDIT_TAG_GROUP_DIALOG_PARENT_CSS, dialogName));
		return new CreateEditTagGroupDialog(getDriver(), dialogName);
	}

	/**
	 * Verify that a tag group with a matching code exists in the tag group list.
	 *
	 * @param tagGroupCode the code to verify
	 */
	public void verifyTagGroupExists(final String tagGroupCode) {
		assertThat(selectItemInCenterPaneWithoutPagination(TAG_GROUP_LIST_PARENT_CSS, TAG_GROUP_LIST_CSS, tagGroupCode,
				CODE_COLUMN_NAME))
				.as("Tag Group does not exist in the list - " + tagGroupCode)
				.isTrue();
	}

	/**
	 * Verify that a tag group with a matching name exists in the tag group list.  The default locale is assumed.
	 *
	 * @param tagGroupName the name to verify
	 */
	public void verifyTagGroupExistsByName(final String tagGroupName) {
		assertThat(selectItemInCenterPaneWithoutPagination(TAG_GROUP_LIST_PARENT_CSS, TAG_GROUP_LIST_CSS, tagGroupName,
				"Display Name"))
				.as("Tag Group name does not exist in the list - " + tagGroupName)
				.isTrue();
	}

	/**
	 * Select (and verify) a tag group with a matching code in the tag group list.
	 *
	 * @param tagGroupCode the code to select
	 */
	public void selectTagGroup(final String tagGroupCode) {
		verifyTagGroupExists(tagGroupCode);
	}

	/**
	 * Verify that a tag group exits and remove it from the list by clicking the button.
	 *
	 * @param tagGroupToRemove the code/guid of the tag group to s
	 */
	public void clickRemoveTagGroupButton(final String tagGroupToRemove) {
		verifyTagGroupExists(tagGroupToRemove);
		clickButton(REMOVE_TAG_GROUP_BUTTON_CSS, "Remove Group");
	}

	/**
	 * Verify that a tag group does NOT exist in the list.
	 *
	 * @param tagGroupCode the code/guid of the tag group to verify
	 */
	public void verifyTagGroupDoesNotExist(final String tagGroupCode) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(verifyItemIsNotInCenterPaneWithoutPagination(TAG_GROUP_LIST_PARENT_CSS, TAG_GROUP_LIST_CSS, tagGroupCode,
				CODE_COLUMN_NAME))
				.as("Tag Group is still in the list - " + tagGroupCode)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Clicks on the button to add a new tag definition.
	 *
	 * @return the selenium representation for the AddEditTagDefinitionDialog
	 */
	public CreateEditTagDefinitionDialog clickAddTagDefinitionButton() {
		clickButton(CREATE_TAG_DEFINITION_BUTTON_CSS, "Add Tag");
		return new CreateEditTagDefinitionDialog(getDriver(), CreateEditTagDefinitionDialog.DIALOGTYPE.add);
	}

	/**
	 * Verifies that the specified tag definition exists in the list of tag definitions.
	 *
	 * @param tagDefCode the tag definition code to verify
	 */
	public void verifyTagDefinitionExists(final String tagDefCode) {
		assertThat(
				selectItemInDialog(TAG_DEFINITION_LIST_PARENT_CSS, TAG_DEFINITION_LIST_PARENT_CSS + TAG_DEFINITION_LIST_CODE_COLUMN_CSS, tagDefCode,
						CODE_COLUMN_NAME))
				.as("Tag Definition does not exist in the list - " + tagDefCode)
				.isTrue();
	}

	/**
	 * Selects the specified tag definition in the table and clicks on the button to edit it.
	 *
	 * @param tagDefinitionCode the tag definition to select and edit
	 * @return the selenium representation for the AddEditTagDefinitionDialog
	 */
	public CreateEditTagDefinitionDialog clickEditTagDefinitionButton(final String tagDefinitionCode) {
		verifyTagDefinitionExists(tagDefinitionCode);
		clickButton(EDIT_TAG_DEFINITION_BUTTON_CSS, "Edit Tag");
		return new CreateEditTagDefinitionDialog(getDriver(), CreateEditTagDefinitionDialog.DIALOGTYPE.edit);
	}

	/**
	 * Selects the specified tag definition in the table and clicks on the button to remove it.
	 *
	 * @param tagDefinitionCodeToRemove the code of the tag definition to remove
	 */
	public void clickRemoveTagDefinitionButton(final String tagDefinitionCodeToRemove) {
		verifyTagDefinitionExists(tagDefinitionCodeToRemove);
		clickButton(REMOVE_TAG_DEFINITION_BUTTON_CSS, "Remove Tag");
	}

	/**
	 * Verifies that the specified tag definition code does not exist in the tag definition table.
	 *
	 * @param tagDefinitionCode the code of the tag definition to verify
	 */
	public void verifyTagDefinitionDoesNotExist(final String tagDefinitionCode) {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		assertThat(verifyItemIsNotInCenterPaneWithoutPagination(TAG_DEFINITION_LIST_PARENT_CSS, TAG_DEFINITION_LIST_PARENT_CSS + TAG_DEFINITION_LIST_CODE_COLUMN_CSS, tagDefinitionCode,
				CODE_COLUMN_NAME))
				.as("Tag Definition is still in the list - " + tagDefinitionCode)
				.isFalse();
		setWebDriverImplicitWaitToDefault();
	}

}
