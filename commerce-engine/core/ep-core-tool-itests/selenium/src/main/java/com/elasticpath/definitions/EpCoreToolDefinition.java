package com.elasticpath.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.coretool.CoreTool;
import com.elasticpath.cucumber.definitions.SystemConfigurationDefinition;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.resultspane.SearchIndexesResultPane;
import com.elasticpath.selenium.resultspane.SystemConfigurationResultPane;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.DBConnector;

/**
 * EpCoreToolDefinition.
 */
public class EpCoreToolDefinition {

	private static final Logger LOGGER = LogManager.getLogger(EpCoreToolDefinition.class);
	private static final Set<String> SEARCH_INDEX_TYPES = ImmutableSet.of("category", "product", "promotion", "cmuser", "sku");
	private final CoreTool coreTool;
	private final SearchIndexesResultPane searchIndexesResultPane;
	private final SystemConfigurationDefinition systemConfigurationDefinition;
	private final SystemConfigurationResultPane systemConfigurationResultPane;
	private static final int COUNTER_125 = 125;
	private final DBConnector dbConnector;
	private final WebDriver driver;

	/**
	 * Constructor.
	 *
	 * @param systemConfigurationDefinition SystemConfigurationDefinition
	 */
	public EpCoreToolDefinition(final SystemConfigurationDefinition systemConfigurationDefinition) {
		coreTool = new CoreTool();
		driver = SeleniumDriverSetup.getDriver();
		searchIndexesResultPane = new SearchIndexesResultPane(driver);
		this.systemConfigurationDefinition = systemConfigurationDefinition;
		this.systemConfigurationResultPane = new SystemConfigurationResultPane(driver);
		dbConnector = new DBConnector();
	}

	/**
	 * Updates the cm user password.
	 *
	 * @param userName the user name
	 * @param password the password
	 */
	@When("^I run the ep core tool to change the (.+) user password to (.+)$")
	public void updatePassword(final String userName, final String password) {
		coreTool.updatePassword(userName, password);
	}

	/**
	 * Verifies status of all indexes.
	 *
	 * @param status the index status
	 */
	@Then("^the DB status of all indexes should be (.+)$")
	public void verifyIndexesStatus(final String status) {
		Map<String, String> unexpectedIndexStatuses = getUnexpectedIndexStatusesFromDatabase(SEARCH_INDEX_TYPES, status);
		for (int i = 0; i < COUNTER_125; i++) {
			if (unexpectedIndexStatuses.isEmpty()) {
				break;
			}
			searchIndexesResultPane.sleep(Constants.SLEEP_HUNDRED_MILLI_SECONDS);
			unexpectedIndexStatuses = getUnexpectedIndexStatusesFromDatabase(unexpectedIndexStatuses.keySet(), status);
		}
		assertThat(unexpectedIndexStatuses.isEmpty())
				.as("Index status is not as expected. Unexpected statuses: " + unexpectedIndexStatuses)
				.isTrue();
	}

	private Map<String, String> getUnexpectedIndexStatusesFromDatabase(final Set<String> indexTypes, String status) {
		final String indexTypesString = "'" + StringUtils.join(indexTypes, "','") + "'";
		final Map<String, String> unexpectedIndexStatuses = new HashMap<>();
		try (final ResultSet resultSet = dbConnector.executeQuery(
				String.format("SELECT INDEX_TYPE, INDEX_STATUS FROM TINDEXBUILDSTATUS WHERE INDEX_TYPE IN (%s) AND INDEX_STATUS <> '%s'",
						indexTypesString, status))) {
			while (resultSet.next()) {
				unexpectedIndexStatuses.put(resultSet.getString(1), resultSet.getString(2));
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return unexpectedIndexStatuses;
	}

	/**
	 * Rebuilds the indexes.
	 */
	@When("^I run the ep core tool to rebuild the indexes$")
	public void rebuildIndexesWithCoreTool() {
		coreTool.rebuildIndexesWithCoreTool();
	}

	/**
	 * Rebuilds the index.
	 *
	 * @param indexName the index name
	 */
	@When("^I run the ep core tool to rebuild the index of (.+)$")
	public void rebuildIndexWithCoreTool(final String indexName) {
		coreTool.rebuildIndexWithCoreTool(indexName);
	}

	/**
	 * Searches and selects the system setting.
	 *
	 * @param settingName the setting name
	 */
	@When("^I search and select the system configuration (.+)$")
	public void searchAndSelectConfig(final String settingName) {
		systemConfigurationDefinition.enterSettingName(settingName);
		selectConfig(settingName);
	}

	/**
	 * Selects the system setting.
	 *
	 * @param settingName the setting name
	 */
	@When("^I select the system configuration (.+)$")
	public void selectConfig(final String settingName) {
		systemConfigurationResultPane.selectSettingName(settingName);
	}

	/**
	 * Updates the setting defined value.
	 *
	 * @param settingName the setting name
	 * @param value       the setting value
	 */
	@When("^I run the ep core tool to set setting of (.+) to (.+)$")
	public void updateSettingDefinedValue(final String settingName, final String value) {
		coreTool.updateSettingDefinedValue(settingName, value);
		systemConfigurationResultPane.sleep(Constants.SLEEP_FIVE_SECONDS_IN_MILLIS);
	}

	/**
	 * Updates the setting metadata value.
	 *
	 * @param metadataName the metadata name
	 * @param value       the metadata value
	 */
	@When("^I run the ep core tool to set metadata of (.+) setting from (.+) to (.+)$")
	public void updateSettingMetadataValue(final String settingName, final String metadataName, final String value) {
		coreTool.updateSettingMetadataValue(settingName, metadataName, value);
		systemConfigurationResultPane.sleep(Constants.SLEEP_FIVE_SECONDS_IN_MILLIS);
	}

	/**
	 * Closes the pane
	 * @param paneName the pane name
	 */
	@When("^I close the (.+) pane")
	public void reloadPane(final String paneName) {
		systemConfigurationResultPane.closePane("System Configuration");
	}

	/**
	 * Verifies defined value count.
	 *
	 * @param definedValueCount the defined value count
	 */
	@When("^there should be (\\d+) Defined value record$")
	public void verifyDefinedValueCount(final int definedValueCount) {
		systemConfigurationResultPane.setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_FIVE_SECONDS);
		if (searchIndexesResultPane.isElementPresent(By.cssSelector("div[widget-id='true'][widget-type='row']"))) {
			assertThat(driver.findElements(By.cssSelector("div[widget-id='true'][widget-type='row']")).size())
					.as("Defined value record count is not as expected")
					.isEqualTo(definedValueCount);
		}
		systemConfigurationResultPane.setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Verifies setting definition metadata value count.
	 *
	 * @param settingDefinitionMetadataCount the defined value count
	 */
	@When("^there should be (\\d+) Setting Definition Metadata record$")
	public void verifySettingDefinitionMetadataCount(final int settingDefinitionMetadataCount) {
		systemConfigurationResultPane.setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_FIVE_SECONDS);
		if (searchIndexesResultPane.isElementPresent(By.cssSelector("div[widget-id='true'][widget-type='row']"))) {
			assertThat(driver.findElements(By.cssSelector("div[widget-id='true'][widget-type='row']")).size())
					.as("Defined value record count is not as expected")
					.isEqualTo(settingDefinitionMetadataCount);
		}
		systemConfigurationResultPane.setWebDriverImplicitWaitToDefault();
	}

	/**
	 * Updates product name.
	 *
	 * @param currentProdName current product name
	 * @param newProdName     new product name
	 */
	@When("^I update the product name from (.*) to (.*)$")
	public void updateProductName(final String currentProdName, final String newProdName) {
		dbConnector.executeUpdateQuery("UPDATE TPRODUCTLDF SET DISPLAY_NAME='" + newProdName + "' WHERE DISPLAY_NAME='" + currentProdName + "'");
	}

	/**
	 * Resets password.
	 */
	@After("@resetPassword")
	public void restPassword() {
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET PASSWORD='$2a$10$Lc.0V6Pnf63eXIqwW/6Lp.PXwa0rsbvTtLimxZPmVe4fQ.qKMDnfW' WHERE USER_NAME='cs_brand'");
	}

	/**
	 * Resets settings value.
	 */
	@After("@resetSettingValue")
	public void removeSettingsValue() {
		systemConfigurationResultPane.selectSettingName("COMMERCE/SYSTEM/EMAIL/emailTextTemplateEnabled");
		systemConfigurationResultPane.removeDefinedValueRecord("null", "true");
	}

	/**
	 * Resets setting metadata value.
	 */
	@After("@resetSettingMetadata")
	public void removeSettingsMetadata() {
		systemConfigurationResultPane.selectSettingName("COMMERCE/SYSTEM/PROMOTIONS/catalogPromotionsEnabled");
		systemConfigurationResultPane.removeSettingMetadataRecord("some_metadata");
	}

	/**
	 * Resets product name.
	 */
	@After("@resetProdName")
	public void resetProdName() {
		updateProductName("1234", "Gravity");
		rebuildIndexWithCoreTool("product");
	}

}
