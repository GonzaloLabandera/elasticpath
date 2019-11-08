package com.elasticpath.cucumber.definitions;

import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.dialogs.AddEditConfigurationValueDialog;
import com.elasticpath.selenium.resultspane.SystemConfigurationResultPane;
import com.elasticpath.selenium.setup.SetUp;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;

/**
 * System Configuration step definitions.
 */
public class SystemConfigurationDefinition {
	private SystemConfigurationResultPane systemConfigurationResultPane;
	private AddEditConfigurationValueDialog addEditConfigurationValueDialog;
	private ConfigurationActionToolbar configurationActionToolbar;

	/**
	 * Constructor.
	 */
	public SystemConfigurationDefinition() {
		configurationActionToolbar = new ConfigurationActionToolbar(SetUp.getDriver());
		systemConfigurationResultPane = new SystemConfigurationResultPane(SetUp.getDriver());
		addEditConfigurationValueDialog = new AddEditConfigurationValueDialog(SetUp.getDriver());
	}

	/**
	 * Open System Configuration.
	 */
	@When("^I go to System Configuration$")
	public void openSystemConfiguration() {
		systemConfigurationResultPane = configurationActionToolbar.clickSystemConfiguration();
	}

	/**
	 * Enter setting name.
	 *
	 * @param settingName String
	 */
	@When("^I enter setting name (.+) in filter$")
	public void enterSettingName(final String settingName) {
		systemConfigurationResultPane.maximizeSystemConfigurationWindow();
		systemConfigurationResultPane.enterSettingNameFilterInput(settingName);
	}

	/**
	 * Verify setting name exists.
	 *
	 * @param settingName String
	 */
	@Then("^I should see setting (.+) in the filter result$")
	public void selectSettingName(final String settingName) {
		systemConfigurationResultPane.selectSettingName(settingName);
		systemConfigurationResultPane.restoreSystemConfigurationWindow();
	}

	/**
	 * Verify setting Defined values.
	 *
	 * @param storeCode    the store code
	 * @param settingValue String
	 */
	@Then("^Defined Value for Store (.+) is (.+)$")
	public void verifyDefinedSettingValue(final String storeCode, final String settingValue) {
		systemConfigurationResultPane.maximizeSystemConfigurationWindow();
		systemConfigurationResultPane.verifyDefinedSettingValue(storeCode, settingValue);
		systemConfigurationResultPane.restoreSystemConfigurationWindow();
	}

	/**
	 * Creates new Defined Values table record for specified system setting
	 * Context can be NULL because some settings do not require it
	 *
	 * @param recordValues Map of record values
	 */
	@When("^I add new defined values record for system setting with following data$")
	public void addNewDefinedValueForSetting(final Map<String, String> recordValues) {
		systemConfigurationResultPane.selectSettingName(recordValues.get("setting"));
		addEditConfigurationValueDialog = systemConfigurationResultPane.clickNewDefinedValueButton();
		if (!"null".equalsIgnoreCase(recordValues.get("context"))) {
			addEditConfigurationValueDialog.enterContextValue(recordValues.get("context"));
		}
		addEditConfigurationValueDialog.enterTextareaValue(recordValues.get("value"));
		configurationActionToolbar = addEditConfigurationValueDialog.clickSaveButton();
	}

	/**
	 * Verifies number of Defined Values table records
	 *
	 * @param count expected number of records
	 */
	@And("^I should see (\\d+) Defined Values records$")
	public void verifyNumberOdDefinedValueRecords(final Integer count) {
		systemConfigurationResultPane.verifyNumberOfDefinedValuesRecords(count);
	}

	/**
	 * Verifies default value
	 *
	 * @param defaultValue expected number of records
	 */
	@And("^I should see (.+) as default value$")
	public void verifyNumberOdDefinedValueRecords(final String defaultValue) {
		systemConfigurationResultPane.verifyDefaultValue(defaultValue);
	}

	/**
	 * Deletes record specified by Context/Value pair
	 *
	 * @param recordValues Map of record values
	 */
	@When("^I remove defined values record for system setting with following data$")
	public void removeDefinedValueRecord(final Map<String, String> recordValues) {
		systemConfigurationResultPane.selectSettingName(recordValues.get("setting"));
		systemConfigurationResultPane.removeDefinedValueRecord(recordValues.get("context"), recordValues.get("value"));
	}
}
