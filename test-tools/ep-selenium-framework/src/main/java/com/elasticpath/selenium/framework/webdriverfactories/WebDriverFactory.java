package com.elasticpath.selenium.framework.webdriverfactories;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * webdriver factory interface.
 */
public interface WebDriverFactory {

	/**
	 *
	 * @return webdriver.
	 */
	/** retrieves values from selenium.properties file. */
	PropertyManager PROPERTY_MANAGER = PropertyManager.getInstance();

	/**
	 *
	 *
	 * @return new webdriver.
	 */
	WebDriver createWebDriver();

	/**
	 * create remote webdriver.
	 *
	 * @param remoteWebDriverURL url for the remote server.
	 * @return remote webdriver.
	 */
	RemoteWebDriver createRemoteWebDriver(String remoteWebDriverURL);

}
