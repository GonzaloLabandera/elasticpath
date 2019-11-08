/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cucumber.definitions;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.dialogs.CreateEditTagDefinitionDialog;
import com.elasticpath.selenium.dialogs.CreateEditTagGroupDialog;
import com.elasticpath.selenium.domainobjects.TagDefinition;
import com.elasticpath.selenium.resultspane.TagsResultPane;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.util.Utility;

/**
 * Tags (groups and definitions) cucumber step definitions.
 */
public class TagsDefinition {

	private final ConfigurationActionToolbar configurationActionToolbar;
	private TagsResultPane tagsResultPane;
	private CreateEditTagGroupDialog createEditTagGroupDialog;
	private List<String> newTagDefinitionCodes;
	private String uniqueTagName = "";

	/**
	 * Create a new instance of TagsDefinition.
	 */
	public TagsDefinition() {
		configurationActionToolbar = new ConfigurationActionToolbar(SetUp.getDriver());
	}

	/**
	 * Click Tags menu item.
	 */
	@And("^I go to Tags$")
	public void clickTags() {
		tagsResultPane = configurationActionToolbar.clickTags();
	}

	/**
	 * Open the edit tag group dialog.
	 *
	 * @param tagGroupName the code/guid of the tag group to open the edit dialog for
	 */
	@And("^I open the tag group (.+)$")
	public void editTagGroup(final String tagGroupName) {
		createEditTagGroupDialog = tagsResultPane.clickEditTagGroupButton(tagGroupName);
	}

	@And("^I open the newly created tag group$")
	public void editTagGroup() {
		createEditTagGroupDialog = tagsResultPane.clickEditTagGroupButton(uniqueTagName);
	}

	/**
	 * Select a tag group from the Tag Group list.
	 *
	 * @param tagGroupName the code/guid of the tag group to select
	 */
	@And("^I select the tag group (.+)$")
	public void selectTagGroup(final String tagGroupName) {
		tagsResultPane.selectTagGroup(tagGroupName);
	}

	@And("^I select the newly created tag group$")
	public void selectTagGroup() {
		tagsResultPane.selectTagGroup(uniqueTagName);
	}

	/**
	 * Use the tag group dialog to create a new tag group.  The default language is used and is not modifiable.
	 *
	 * @param tagGroupCode the value to use as the code/guid and display name for the tag group
	 */
	@When("^I add a tag group (.+)$")
	public void createTagGroup(final String tagGroupCode) {
		uniqueTagName = tagGroupCode + "_" + Utility.getRandomUUID();

		createEditTagGroupDialog = tagsResultPane.clickAddNewTagGroupButton();
		createEditTagGroupDialog.enterTagGroupCode(uniqueTagName);
		createEditTagGroupDialog.enterTagGroupDisplayName(uniqueTagName);
		createEditTagGroupDialog.clickSaveButton();
	}

	/**
	 * Verify new tag group exists.*
	 *
	 * @param tagGroupCode the code/guid of the tag group that should now be in the list
	 */
	@Then("^the tag groups list contains (.+)$")
	public void verifyNewTagGroupExists(final String tagGroupCode) {
		tagsResultPane.maximizeTagsWindow();
		tagsResultPane.verifyTagGroupExists(tagGroupCode);
	}

	@Then("^verify the tag groups list contains newly added tag group$")
	public void verifyNewTagGroupExists() {
		tagsResultPane.maximizeTagsWindow();
		tagsResultPane.verifyTagGroupExists(uniqueTagName);
	}

	/**
	 * Verify new tag group exists, by name.
	 * @param tagGroupName the name of the tag group that should now be in the list
	 */
	@Then("^the tag group list contains the name (.+)$")
	public void verifyNewTagGroupExistsByName(final String tagGroupName) {
		tagsResultPane.maximizeTagsWindow();
		tagsResultPane.verifyTagGroupExistsByName(tagGroupName);
	}

	@Then("^verify the edited tag group list contains the new name$")
	public void verifyNewTagGroupExistsByName() {
		tagsResultPane.maximizeTagsWindow();
		tagsResultPane.verifyTagGroupExistsByName(uniqueTagName);
	}

	/**
	 * Add tag definitions to the tag group.
	 */
	@When("^I add tag definitions with the following data$")
	public void createNewTagDefinition(final List<TagDefinition> tagDefinitions) {
		newTagDefinitionCodes = new ArrayList<>();
		for (TagDefinition tagDef : tagDefinitions) {
			CreateEditTagDefinitionDialog newTagDefDialog = tagsResultPane.clickAddTagDefinitionButton();
			fillTagDefinitionDialog(tagDef, newTagDefDialog, true);
		}
	}

	/**
	 * Verify new tag definitions exists
	 */
	@Then("^verify the tag definition list contains the tag definitions$")
	public void verifyNewTagDefinitionsExist() {
		tagsResultPane.maximizeTagsWindow();
		for (String tagDefCode : newTagDefinitionCodes) {
			tagsResultPane.verifyTagDefinitionExists(tagDefCode);
		}
	}

	/**
	 * Editing the tag group name.
	 *
	 * @param newTagGroupName the tag group group name to change to
	 */
	@When("^I edit the tag group name to (.+)$")
	public void editTagGroupCode(final String newTagGroupName) {
		uniqueTagName = newTagGroupName + "_" + Utility.getRandomUUID();

		createEditTagGroupDialog.enterTagGroupDisplayName(uniqueTagName);
		createEditTagGroupDialog.clickSaveButton();
	}

	/**
	 * Edit a tag definition.
	 */
	@When("^I edit the tag definition (.+) with the following data$")
	public void editTagDefinitions(final String tagDefinitionCode, final List<TagDefinition> definitions) {
		newTagDefinitionCodes = new ArrayList<>();
		for (TagDefinition tagDef : definitions) {
			CreateEditTagDefinitionDialog newTagDefDialog = tagsResultPane.clickEditTagDefinitionButton(tagDefinitionCode);
			fillTagDefinitionDialog(tagDef, newTagDefDialog, false);
		}
	}

	/**
	 * Remove the tag definition from the tag group.
	 *
	 * @param tagDefinitionCodeToRemove the code of the tag definition to remove from the list
	 */
	@When("^I remove the tag definition (.+)$")
	public void removeTagDefinition(final String tagDefinitionCodeToRemove) {
		tagsResultPane.clickRemoveTagDefinitionButton(tagDefinitionCodeToRemove);
	}

	/**
	 * Remove the tag group.
	 *
	 * @param tagGroupToRemove the code/guid of tag group to remove from the list
	 */
	@When("^I remove the tag group (.+)$")
	public void removeTagGroup(final String tagGroupToRemove) {
		tagsResultPane.clickRemoveTagGroupButton(tagGroupToRemove);
	}

	@When("^I remove the recently created tag group$")
	public void removeTagGroup() {
		tagsResultPane.clickRemoveTagGroupButton(uniqueTagName);
	}

	/**
	 * Verify that a tag definition does NOT exist in the current tag group.
	 *
	 * @param tagDefinitionCode the code/guid of the tag definition to verify
	 */
	@Then("^the tag definition (.+) should not exist in the list$")
	public void verifyTagDefinitionDoesNotExist(final String tagDefinitionCode) {
		tagsResultPane.verifyTagDefinitionDoesNotExist(tagDefinitionCode);
	}

	/**
	 * Verify that a tag group does NOT exist in the list.
	 *
	 * @param tagGroupCode the code/guid of the tag group to verify
	 */
	@Then("^the tag group (.+) should not exist in the list$")
	public void verifyTagGroupDoesNotExist(final String tagGroupCode) {
		tagsResultPane.verifyTagGroupDoesNotExist(tagGroupCode);
	}

	@Then("^the tag group should not exist in the list$")
	public void verifyTagGroupDoesNotExist() {
		tagsResultPane.verifyTagGroupDoesNotExist(uniqueTagName);
	}

	/**
	 * A method for the user to fill in a tag definition dialog.
	 *
	 * @param tagDefinition
	 * @param newTagDefDialog
	 * @param createMode
	 */
	private void fillTagDefinitionDialog(final TagDefinition tagDefinition, final CreateEditTagDefinitionDialog newTagDefDialog,
			final boolean createMode) {
		String newTagDefCode = tagDefinition.getCode();
		if (createMode) {
			newTagDefDialog.enterTagDefinitionCode(newTagDefCode);
			newTagDefDialog.enterTagDefinitionName(tagDefinition.getName());
		}
		newTagDefDialog.enterTagDefinitionDescription(tagDefinition.getDescription());
		newTagDefDialog.selectTagDefinitionLanguage(tagDefinition.getLanguage());
		newTagDefDialog.enterTagDefinitionDisplayName(tagDefinition.getDisplayName());
		newTagDefDialog.selectFieldType(tagDefinition.getFieldType());
		String[] dictionaryNames = tagDefinition.getDictionaries().split(",");
		newTagDefDialog.selectTagDefinitionDictionaries(dictionaryNames);
		newTagDefDialog.clickSaveButton();
		newTagDefinitionCodes.add(newTagDefCode);
	}

	/**
	 * Clean-up method for tags at the end of the test suit cases.
	 */
	@After("@cleanupTags")
	public void cleanupTags() {
		tagsResultPane.maximizeTagsWindow();
		selectTagGroup();
		tagsResultPane.clickRemoveTagGroupButton(uniqueTagName);
	}
}
