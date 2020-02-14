package com.elasticpath.configuration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Configuration.
 */
public class Configuration extends AbstractPageObject {

	private final WebDriver driver;

	/**
	 * Constructor.
	 *
	 * @param webDriver the web driver
	 */
	public Configuration(final WebDriver webDriver) {
		super(webDriver);
		this.driver = webDriver;
	}

	/**
	 * Returns count of status.
	 *
	 * @param status the status
	 * @return count of status
	 */
	public int getStatusCount(final String status) {
		setWebDriverImplicitWait(1);
		int count = driver.findElements(By.cssSelector("div[widget-id='Search Index'] "
				+ "div[automation-id*='com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages." + status + "']")).size();
		setWebDriverImplicitWaitToDefault();
		return count;
	}

	/**
	 * Returns count of status based on index name.
	 *
	 * @param indexName the index name
	 * @param status    the status
	 * @return count of status
	 */
	public int getStatusCount(final String indexName, final String status) {
		setWebDriverImplicitWait(1);
		int count = driver.findElements(By.cssSelector("div[widget-id='Search Index'] div[row-id='" + indexName + "'] "
				+ "div[automation-id*='com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages." + status + "']")).size();
		setWebDriverImplicitWaitToDefault();
		return count;
	}
}
