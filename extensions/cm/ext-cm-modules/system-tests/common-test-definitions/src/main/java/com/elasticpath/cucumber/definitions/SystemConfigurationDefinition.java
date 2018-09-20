package com.elasticpath.cucumber.definitions;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.resultspane.SystemConfigurationResultPane;

/**
 * System Configuration step definitions.
 */
public class SystemConfigurationDefinition {
	private final SystemConfigurationResultPane systemConfigurationResultPane;

	/**
	 * Constructor.
	 */
	public SystemConfigurationDefinition() {
		systemConfigurationResultPane = new SystemConfigurationResultPane(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Enter setting name.
	 *
	 * @param settingName String
	 */
	@When("^I enter setting name (.+) in filter$")
	public void enterSettingName(final String settingName) {
		systemConfigurationResultPane.enterSettingNameFilterInput(settingName);
	}

	/**
	 * Verify setting name exists.
	 *
	 * @param settingName String
	 */
	@Then("^I should see setting (.+) in the filter result$")
	public void verfiySettingNameExists(final String settingName) {
		systemConfigurationResultPane.verifySettingName(settingName);
	}

}
