package com.elasticpath.cucumber.definitions.catalog.editor.tabs;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.AddEditSkuOptionDialog;
import com.elasticpath.selenium.dialogs.AddEditSkuOptionValueDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.domainobjects.Product;
import com.elasticpath.selenium.domainobjects.SkuOption;
import com.elasticpath.selenium.domainobjects.containers.SkuOptionContainer;
import com.elasticpath.selenium.editor.catalog.tabs.SkuOptionsTab;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.AbstractToolbar;
import com.elasticpath.selenium.toolbars.CatalogManagementActionToolbar;
import com.elasticpath.selenium.util.Utility;

/**
 * Sku Options Tab Definitions.
 */
public class SkuOptionsTabDefinition {

	private final SkuOption skuOption;
	private final AbstractToolbar catalogManagementActionToolbar;
	private final SkuOptionsTab skuOptionsTab;
	private final Product product;
	private AddEditSkuOptionValueDialog addEditSkuOptionValueDialog;
	private AddEditSkuOptionDialog addEditSkuOptionDialog;
	private final SkuOptionContainer skuOptionContainer;

	/**
	 * Constructor.
	 *
	 * @param product            variable for the product.
	 * @param skuOption          variable for the skuOption.
	 * @param skuOptionContainer variable for the skuOptionContainer.
	 */
	public SkuOptionsTabDefinition(final Product product, final SkuOption skuOption, final SkuOptionContainer skuOptionContainer) {
		final WebDriver driver = SetUp.getDriver();
		this.catalogManagementActionToolbar = new CatalogManagementActionToolbar(driver);
		this.skuOptionsTab = new SkuOptionsTab(driver);
		this.product = product;
		this.skuOption = skuOption;
		this.skuOptionContainer = skuOptionContainer;
	}

	/**
	 * Create SkuOption.
	 *
	 * @param skuOptionCode the sku option code
	 * @param displayName   the display name
	 */
	@When("^I create a new sku option with sku code (.*) and display name (.*)$")
	public void createSkuOption(final String skuOptionCode, final String displayName) {
		addEditSkuOptionDialog = skuOptionsTab.clickAddSkuOptionButton();
		addSkuOption(skuOptionCode, displayName, addEditSkuOptionDialog.getLanguage());
		skuOptionContainer.addSkuOption(skuOption);
		saveSkuOptionChanges();
	}

	/**
	 * Create SkuOption.
	 *
	 * @param skuOptionCode the sku option code
	 * @param names         values for the new localized names
	 */
	@When("^I add a new sku option with sku option code (.*) without saving with the following names$")
	public void addSkuOptionWithoutSaving(final String skuOptionCode, final Map<String, String> names) {
		final SkuOption sku = new SkuOption();
		addEditSkuOptionDialog = skuOptionsTab.clickAddSkuOptionButton();
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			fillSkuOptionParameters(skuOptionCode, localizedName.getValue(), localizedName.getKey());
			sku.setName(localizedName.getKey(), localizedName.getValue());
		}
		addEditSkuOptionDialog.clickAddButton();
		sku.setCode(skuOption.getCode());
		skuOptionContainer.addSkuOption(sku);
	}

	/**
	 * Create SkuOption with sku option value.
	 *
	 * @param skuOptionDetails sku option details: sku code, sku name, sku option value code, sku option value name
	 */
	@When("^I create a new sku option with sku option value$")
	public void createSkuOptionWithValue(final Map<String, String> skuOptionDetails) {
		addEditSkuOptionDialog = skuOptionsTab.clickAddSkuOptionButton();
		addSkuOption(skuOptionDetails.get("code"), skuOptionDetails.get("name"), addEditSkuOptionDialog.getLanguage());
		skuOptionsTab.selectSkuOption(skuOption.getCode());
		addSkuOptionValueForSelectedLanguage(skuOptionDetails.get("valueCode"), skuOptionDetails.get("valueName"));
		saveSkuOptionChanges();
	}

	/**
	 * Adds new sku option to opened catalog.
	 *
	 * @param code new sku option code
	 * @param name new sku option name
	 */
	private void addSkuOption(final String code, final String name, final String language) {
		fillSkuOptionParameters(code, name, language);
		addEditSkuOptionDialog.clickAddButton();
	}

	/**
	 * Fill sku option parameters in opened dialog.
	 *
	 * @param code new sku option code
	 * @param name new sku option name
	 */
	private void fillSkuOptionParameters(final String code, final String name, final String language) {
		String skuOptionCodeRandom = code + Utility.getRandomUUID();
		this.product.setSKUOption(skuOptionCodeRandom);
		addEditSkuOptionDialog.enterSkuOptionCode(skuOptionCodeRandom);
		addEditSkuOptionDialog.selectLanguage(language);
		addEditSkuOptionDialog.enterSkuOptionDisplayName(name);
		skuOption.setCode(skuOptionCodeRandom);
		skuOption.setName(language, name);
	}

	/**
	 * Adds new sku option value to selected sku option and language in opened catalog.
	 *
	 * @param valueCode new sku option value code
	 * @param valueName new sku option value name
	 */
	private void addSkuOptionValueForSelectedLanguage(final String valueCode, final String valueName) {
		addEditSkuOptionValueDialog = skuOptionsTab.clickAddSkuOptionValueButton();
		fillFormAndAddSkuOptionValue(addEditSkuOptionValueDialog.getLanguage(), valueCode, valueName);
	}

	/**
	 * Adds new sku option value to selected sku option in opened catalog.
	 *
	 * @param language  localization for new sku option value
	 * @param valueCode new sku option value code
	 * @param valueName new sku option value name
	 */
	private void addSkuOptionValue(final String language, final String valueCode, final String valueName) {
		addEditSkuOptionValueDialog = skuOptionsTab.clickAddSkuOptionValueButton();
		addEditSkuOptionValueDialog.selectLanguage(language);
		fillFormAndAddSkuOptionValue(language, valueCode, valueName);
	}

	/**
	 * Fills code and name of sku option value and adds it.
	 *
	 * @param language  language which should be selected
	 * @param valueCode new sku option value code
	 * @param valueName new sku option value name
	 */
	private void fillFormAndAddSkuOptionValue(final String language, final String valueCode, final String valueName) {
		String skuOptionValueCodeRandom = valueCode + Utility.getRandomUUID();
		fillFormSkuOptionValue(language, skuOptionValueCodeRandom, valueName);
		addEditSkuOptionValueDialog.clickAddButton();
	}

	/**
	 * Fills code and name of sku option value.
	 *
	 * @param language  language which should be selected
	 * @param valueCode new sku option value code
	 * @param valueName new sku option value name
	 */
	private void fillFormSkuOptionValue(final String language, final String valueCode, final String valueName) {
		addEditSkuOptionValueDialog.enterSkuOptionCode(valueCode);
		addEditSkuOptionValueDialog.enterSkuOptionValueDisplayName(valueName);
		skuOption.addSkuOptionValue(language, valueCode, valueName);
		skuOptionContainer.getSkuOptionByPartialCode(skuOption.getCode()).addSkuOptionValue(language, valueCode, valueName);
	}

	/**
	 * verify newly created sku option exists.
	 */
	@Then("^newly created sku option is in the list$")
	public void verifyNewlyCreatedSkuOptionExists() {
		skuOptionsTab.verifySkuOption(addEditSkuOptionDialog.getCode());
	}

	/**
	 * Verify updated sku option name exists.
	 */
	@Then("^updated sku option name is in the list$")
	public void verifyUpdatedSkuOptionExists() {
		skuOptionsTab.verifySkuOption(addEditSkuOptionDialog.getDisplayName());
	}

	/**
	 * Edit sku option name.
	 */
	@When("^I edit the sku option name$")
	public void editSkuOptionNameWithoutChanges() {
		skuOptionsTab.selectSkuOption(addEditSkuOptionDialog.getCode());
		String skuOptionCodeRandom = "EditSkuDisplayName" + "_" + Utility.getRandomUUID();
		skuOptionsTab.clickEditSkuOptionButton();
		addEditSkuOptionDialog.enterSkuOptionDisplayName(skuOptionCodeRandom);
		addEditSkuOptionDialog.clickAddButton();
		saveSkuOptionChanges();
	}

	/**
	 * Edit sku option value name. Accepts partial matched sku value code
	 *
	 * @param skuValueCode sku value code which names should be changed.
	 * @param names        values for the new localized names
	 */
	@When("^I edit sku option value (.+) names without saving$")
	public void editSkuOptionValueNameWithoutChanges(final String skuValueCode, final Map<String, String> names) {
		skuOptionsTab.verifySkuOptionValue(skuOption.getCode(), skuOption.getSkuOptionValueCodeByPartialCode(skuValueCode));
		skuOptionsTab.clickEditSkuOptionButton();
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			addEditSkuOptionValueDialog.selectLanguage(localizedName.getKey());
			addEditSkuOptionValueDialog.enterSkuOptionValueDisplayName(localizedName.getValue());
			skuOption.addSkuOptionValue(
					localizedName.getKey(),
					skuOption.getSkuOptionValueCodeByPartialCode(skuValueCode),
					localizedName.getValue());
		}
		addEditSkuOptionDialog.clickAddButton();
	}

	/**
	 * Edit last 5 characters with random values of sku option value name. Accepts partial matched sku value code
	 *
	 * @param skuValueCode sku value code which names should be changed.
	 * @param languages    sku option languages
	 */
	@When("^I edit last 5 characters of sku option value (.+) names without saving$")
	public void editRandomlySkuOptionValueNamesWithoutSaving(final String skuValueCode, final List<String> languages) {
		String oldName;
		String editedName;
		String skuOptionValueCode = skuOption.getSkuOptionValueCodeByPartialCode(skuValueCode);
		skuOptionsTab.verifySkuOptionValue(skuOption.getCode(), skuOptionValueCode);
		addEditSkuOptionValueDialog = skuOptionsTab.clickEditSelectedForValueButton();
		for (String language : languages) {
			addEditSkuOptionValueDialog.selectLanguage(language);
			oldName = addEditSkuOptionDialog.getDisplayName();
			skuOption.addSkuOptionValueOldName(language, skuOptionValueCode, oldName);
			editedName = oldName.substring(0, oldName.length() - 5) + Utility.getRandomUUID();
			addEditSkuOptionValueDialog.enterSkuOptionValueDisplayName(editedName);
			skuOption.addSkuOptionValue(language, skuOptionValueCode, editedName);
		}
		addEditSkuOptionDialog.clickAddButton();
	}

	/**
	 * Edit sku option name without saving.
	 *
	 * @param names values for the new localized names
	 */
	@When("^I edit the sku option name with the following new names without saving$")
	public void editSkuOptionName(final Map<String, String> names) {
		skuOptionsTab.selectSkuOption(skuOption.getCode());
		skuOptionsTab.clickEditSkuOptionButton();
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			addEditSkuOptionDialog.selectLanguage(localizedName.getKey());
			addEditSkuOptionDialog.enterSkuOptionDisplayName(localizedName.getValue());
			skuOption.setName(localizedName.getKey(), localizedName.getValue());
		}
		addEditSkuOptionDialog.clickAddButton();
	}

	/**
	 * Edit last 5 characters with random values of sku option names without saving.
	 *
	 * @param languages values for the new localized names
	 */
	@When("^I edit last 5 characters of sku option names with random characters for the following languages without saving$")
	public void editRandomlySkuOptionNames(final List<String> languages) {
		String oldName;
		String editedName;
		skuOptionsTab.selectSkuOption(skuOption.getCode());
		addEditSkuOptionDialog = skuOptionsTab.clickEditSkuOptionButton();
		for (String language : languages) {
			addEditSkuOptionDialog.selectLanguage(language);
			oldName = addEditSkuOptionDialog.getSkuOptionDisplayName();
			skuOption.setOldName(language, oldName);
			editedName = oldName.substring(0, oldName.length() - 5) + Utility.getRandomUUID();
			addEditSkuOptionDialog.enterSkuOptionDisplayName(editedName);
			skuOption.setName(language, editedName);
		}
		addEditSkuOptionDialog.clickAddButton();
	}

	/**
	 * Delete new sku option.
	 */
	@When("^I delete the newly created sku option$")
	public void deleteNewSkuOption() {
		skuOptionsTab.selectTab("SkuOptions");
		skuOptionsTab.selectSkuOption(addEditSkuOptionDialog.getCode());
		skuOptionsTab.clickRemoveSkuOptionButton();
		new ConfirmDialog(SetUp.getDriver()).clickOKButton("CatalogMessages.CatalogSkuOptionsSection_RemoveSelectionDialog");
		saveSkuOptionChanges();
	}

	/**
	 * Verify newly created sku option is deleted.
	 */
	@Then("^the newly created sku option is deleted$")
	public void verifyNewSkuOptionDelete() {
		skuOptionsTab.verifySkuOptionDelete(addEditSkuOptionDialog.getCode());
	}

	/**
	 * Create SkuOption value for selected language.
	 *
	 * @param skuOptionValueCode the sku option value code
	 * @param displayName        sku option value display name
	 */
	@And("^I create a new sku option value with sku value code (.*) and display name (.*)$")
	public void createSkuOptionValueForSelectedLanguage(final String skuOptionValueCode, final String displayName) {
		addSkuOptionValueForSelectedLanguage(skuOptionValueCode, displayName);
		saveSkuOptionChanges();
	}

	/**
	 * Create SkuOption value.
	 *
	 * @param language           localization for new sku option value
	 * @param skuOptionValueCode the sku option value code
	 * @param displayName        sku option value display name
	 */
	@And("^I create a new sku option value for language (.*) with sku value code (.*) and display name (.*)$")
	public void createSkuOptionValue(final String language, final String skuOptionValueCode, final String displayName) {
		addSkuOptionValue(language, skuOptionValueCode, displayName);
		saveSkuOptionChanges();
	}

	/**
	 * Create SkuOption value with different languages.
	 *
	 * @param skuOptionValueCode the sku option value code
	 * @param names              map containing a pair of language and display name as an entry
	 */
	@And("^I create a new sku option value with sku value code (.*) and the following names$")
	public void createSkuOptionValueDifferentLocales(final String skuOptionValueCode, final Map<String, String> names) {
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			addSkuOptionValue(localizedName.getKey(), skuOptionValueCode, localizedName.getValue());
		}
		saveSkuOptionChanges();
	}

	/**
	 * Add SkuOption value with different languages without saving.
	 *
	 * @param skuOptionValueCode the sku option value code
	 * @param names              map containing a pair of language and display name as an entry
	 */
	@And("^I add a new sku option value with sku option value code (.*) and the following names without saving$")
	public void addSkuOptionValueDifferentLocales(final String skuOptionValueCode, final Map<String, String> names) {
		addEditSkuOptionValueDialog = skuOptionsTab.clickAddSkuOptionValueButton();
		String skuOptionValueCodeRandom = skuOptionValueCode + Utility.getRandomUUID();
		for (Map.Entry<String, String> localizedName : names.entrySet()) {
			addEditSkuOptionValueDialog.selectLanguage(localizedName.getKey());
			fillFormSkuOptionValue(localizedName.getKey(), skuOptionValueCodeRandom, localizedName.getValue());
		}
		addEditSkuOptionValueDialog.clickAddButton();
	}


	/**
	 * verify newly created sku option value exists.
	 */
	@Then("^newly created sku option value is in the list$")
	public void verifyNewlyCreatedSkuOptionValueExists() {
		skuOptionsTab.verifySkuOptionValue(addEditSkuOptionDialog.getCode(), addEditSkuOptionValueDialog.getCode());
	}

	/**
	 * Save changes made for opened sku option and refresh editor
	 */
	@Then("^I save changes made for (?:opened|new) sku option$")
	public void saveSkuOptionChanges() {
		catalogManagementActionToolbar.saveAll();
		catalogManagementActionToolbar.clickReloadActiveEditor();
	}
}
