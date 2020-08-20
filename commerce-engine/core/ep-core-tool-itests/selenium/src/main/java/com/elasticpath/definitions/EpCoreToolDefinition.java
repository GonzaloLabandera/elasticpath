package com.elasticpath.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.configuration.Configuration;
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

	private static final Logger LOGGER = Logger.getLogger(EpCoreToolDefinition.class);
	private final CoreTool coreTool;
	private final SearchIndexesResultPane searchIndexesResultPane;
	private final SystemConfigurationDefinition systemConfigurationDefinition;
	private final SystemConfigurationResultPane systemConfigurationResultPane;
	private static final int COUNTER_125 = 125;
	private final DBConnector dbConnector;
	private final Configuration configuration;
	private static final int INDEX_COUNT_6 = 6;
	private static final int INDEX_COUNT_1 = 1;
	private ResultSet resultSet;
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
		configuration = new Configuration(driver);
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
	 * Verifies rebuilding status of all indexes.
	 *
	 * @param status the index status
	 */
	@Then("^the status of all indexes should be (.+)$")
	public void verifyIndexesStatus(final String status) {
		Map<String, String> dbMap = getDbIndexNotifyMap();
		LOGGER.info("db value map: " + dbMap);
		int counter = 0;
		int statusCount = configuration.getStatusCount(status.toUpperCase(Locale.ENGLISH));
		LOGGER.info("search indexes count for status " + status + "..... " + statusCount);
		while (statusCount != INDEX_COUNT_6 && counter < COUNTER_125) {
			searchIndexesResultPane.sleep(Constants.SLEEP_HUNDRED_MILLI_SECONDS);
			statusCount = configuration.getStatusCount(status.toUpperCase(Locale.ENGLISH));
			LOGGER.info("search indexes count for status " + status + "..... " + statusCount);
			counter++;
		}
		assertThat(statusCount)
				.as("Index status is not as expected - db value map: " + dbMap)
				.isEqualTo(INDEX_COUNT_6);
	}

	/**
	 * Verifies rebuilding status an index.
	 *
	 * @param indexName the index name
	 * @param status    the index status
	 */
	@Then("^the status of (.+) index should be (.+)$")
	public void verifyIndexStatus(final String indexName, final String status) {
		Map<String, String> dbMap = getDbIndexNotifyMap();
		LOGGER.info("db value map: " + dbMap);
		int counter = 0;
		int statusCount = configuration.getStatusCount(indexName, status.toUpperCase(Locale.ENGLISH));
		LOGGER.info(indexName + " search index count for status " + status + " ..... " + statusCount);
		while (statusCount != INDEX_COUNT_1 && counter < COUNTER_125) {
			searchIndexesResultPane.sleep(Constants.SLEEP_HUNDRED_MILLI_SECONDS);
			statusCount = configuration.getStatusCount(indexName, status.toUpperCase(Locale.ENGLISH));
			LOGGER.info(indexName + " search index count for status " + status + " ..... " + statusCount);
			counter++;
		}
		assertThat(statusCount)
				.as("Index status of " + indexName + " is not as expected - db value map: " + dbMap)
				.isEqualTo(INDEX_COUNT_1);
	}

	private Map<String, String> getDbIndexNotifyMap() {
		Map<String, String> indexNotifyMap = new HashMap<>();
		resultSet = dbConnector.executeQuery("SELECT INDEX_TYPE, UPDATE_TYPE FROM TINDEXNOTIFY");
		try {
			while (resultSet.next()) {
				indexNotifyMap.put(resultSet.getString(1), resultSet.getString(2));
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (SQLException ex) {
				LOGGER.error(ex);
			}
		}
		return indexNotifyMap;
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
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET PASSWORD='3d4f2bf07dc1be38b20cd6e46949a1071f9d0e3d' WHERE USER_NAME='cs_brand'");
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
	 * Resets product name.
	 */
	@After("@resetProdName")
	public void resetProdName() {
		updateProductName("1234", "Gravity");
		rebuildIndexWithCoreTool("product");
	}

}
