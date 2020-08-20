/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.definitions.catalog.editor.tabs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddAttributeDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.EditAttributeDialog;
import com.elasticpath.selenium.domainobjects.Attribute;
import com.elasticpath.selenium.editor.catalog.CatalogEditor;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.util.Utility;

/**
 * Attribute Tab Definitions.
 */
public class AttributeTabDefinition {
	private final Attribute attribute;
	private final CatalogEditor catalogEditor;
	private AddAttributeDialog addAttributeDialog;
	private EditAttributeDialog editAttributeDialog;
	private final WebDriver driver;

	public AttributeTabDefinition(final Attribute attribute) {
		driver = SeleniumDriverSetup.getDriver();
		catalogEditor = new CatalogEditor(driver);
		this.attribute = attribute;
	}

	/**
	 * Create Attribute.
	 *
	 * @param parameters parameters for creating attribute.
	 */
	@When("^I add a new attribute with the following parameters$")
	public void createNewAttribute(final Map<String, String> parameters) {
		int languageIndex = 0;
		addAttributeDialog = catalogEditor.clickAddAttributeButton();
		final String attributeKey = parameters.get("key") + Utility.getRandomUUID();
		attribute.setKey(attributeKey);
		List<String> attributeDisplayNames = parseByComma(parameters.get("names"));
		List<String> displayNameLanguages = parseByComma(parameters.get("languages"));
		addAttributeDialog.enterAttributeKey(attributeKey);
		for (String localizedName : attributeDisplayNames) {
			fillAttributeNames(localizedName, displayNameLanguages.get(languageIndex));
			languageIndex++;
		}
		addAttributeDialog.selectAttributeUsage(parameters.get("usage"));
		addAttributeDialog.selectAttributeType(parameters.get("type"));
		if (Boolean.valueOf(parameters.get("required"))) {
			addAttributeDialog.clickRequiredAttributeCheckBox();
		}
		if (Boolean.valueOf(parameters.get("required"))) {
			addAttributeDialog.clickMultiValueCheckBox();
		}
		addAttributeDialog.clickAddButton();
	}

	/**
	 * Update localized names for Attribute with generated previously attribute key.
	 *
	 * @param names values for the new localized names.
	 */
	@When("^I update created before attribute with the following new names")
	public void updateCreatedAttribute(final Map<String, String> names) {
		openCreatedAttributeForEditing();
		for (Map.Entry<String, String> name : names.entrySet()) {
			editAttributeDialog.selectLanguage(name.getKey());
			editAttributeDialog.enterAttributeName(name.getValue());
			attribute.setName(name.getKey(), name.getValue());
		}
		editAttributeDialog.clickOKButton();
	}

	/**
	 * Edit last 5 characters with random values of attribute names without saving.
	 *
	 * @param languages list of languages for localized names
	 */
	@When("^I edit last 5 characters of attribute names with random characters for the following languages without saving")
	public void editRandomlyBrandNames(final List<String> languages) {
		String oldName;
		String editedName;
		openCreatedAttributeForEditing();
		for (String language : languages) {
			editAttributeDialog.selectLanguage(language);
			oldName = editAttributeDialog.getAttributeName();
			editedName = oldName.substring(0, oldName.length() - 5) + Utility.getRandomUUID();
			editAttributeDialog.enterAttributeName(editedName);
			attribute.setName(language, editedName);
		}
	}

	/**
	 * Closes Edit attribute dialog keeping changes.
	 */
	@When("^I close Edit attribute dialog keeping changes$")
	public void closeEditAttributeDialogKeepingChanges() {
		editAttributeDialog.clickOKButton();
	}

	/**
	 * Fills names of attribute for different languages.
	 *
	 * @param language      language which should be selected.
	 * @param attributeName new brand name.
	 */
	private void fillAttributeNames(final String attributeName, final String language) {
		addAttributeDialog.selectLanguage(language);
		addAttributeDialog.enterAttributeName(attributeName);
		attribute.setName(language, attributeName);
	}

	/**
	 * Verify newly created attribute exists.
	 */
	@Then("^Newly created attribute is in the list$")
	public void verifyNewlyCreatedAttributeExists() {
		catalogEditor.verifyCatalogAttributeValue(attribute.getKey());
	}

	/**
	 * Delete new attribute by key.
	 */
	@When("^I delete the newly created attribute by key$")
	public void deleteNewAttribute() {
		catalogEditor.verifyCatalogAttributeValue(attribute.getKey());
		catalogEditor.clickRemoveAttributeButton();
		new ConfirmDialog(driver).clickOKButton("CatalogMessages.CatalogAttributesSection_RemoveDialog_title");
	}

	/**
	 * Parses given string by comma separator and converts it to the list of strings.
	 *
	 * @param stringToParse string to parse
	 * @return list of strings which consists of elements created from given string parsed by comma separator
	 */
	private List<String> parseByComma(final String stringToParse) {
		return Arrays.asList(stringToParse.split(","));
	}

	/**
	 * Opens editing dialog for a attribute created previously.
	 */
	private void openCreatedAttributeForEditing() {
		catalogEditor.verifyCatalogAttributeValue(attribute.getKey());
		editAttributeDialog = catalogEditor.clickEditAttributeButton();
	}

}
