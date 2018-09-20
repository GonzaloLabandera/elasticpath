package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * System Configuration Result Pane.
 */
public class SystemConfigurationResultPane extends AbstractPageObject {
	private static final String SETTING_NAME_FILTER_INPUT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.configuration"
			+ ".AdminConfigurationMessages.filterLabel'] + div > input";
	private static final String EXPECTED_SETTING_RESULT_CSS = "div[row-id='%s']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public SystemConfigurationResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Enters setting name in filter input box.
	 *
	 * @param settingName String
	 */
	public void enterSettingNameFilterInput(final String settingName) {
		clearAndType(SETTING_NAME_FILTER_INPUT_CSS, settingName);
	}

	/**
	 * Verifies if setting name shows in filter.
	 *
	 * @param settingName String
	 */
	public void verifySettingName(final String settingName) {
		assertThat(getWaitDriver().waitForElementToBeInteractable(String.format(EXPECTED_SETTING_RESULT_CSS, settingName)))
				.as("Expected setting name not exist - " + settingName)
				.isTrue();
	}
}
