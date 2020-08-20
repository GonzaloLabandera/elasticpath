package com.elasticpath.selenium.framework.util;

import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.framework.webdriverfactories.ChromeWebDriverFactory;

/**
 * set up driver according to browsers specified in selenium.property file.
 */
public final class SeleniumDriverSetup {
	/**
	 * browser driver.
	 */
	private static WebDriver driver;

	private static final Object LOCKOBJ = new Object();

	/**
	 * retrieves values from selenium.proerties.
	 */
	private final PropertyManager propertyManager = PropertyManager.getInstance();

	/**
	 * selenium session time out.
	 */
	private final String browser = propertyManager.getProperty("selenium.session.browser");

	/**
	 * constructor.
	 */
	private SeleniumDriverSetup() {
		String remoteWebDriverURL = propertyManager.getProperty("remote.web.driver.url");
		if ("chrome".equals(browser)) {
			setDriver(new ChromeWebDriverFactory().createWebDriver());
		} else if ("remote_chrome".equals(browser)) {
			setDriver(new ChromeWebDriverFactory().createRemoteWebDriver(remoteWebDriverURL));
		}
	}

	/**
	 * @return webdriver.
	 */
	public static WebDriver getDriver() {
		if (driver == null) {
			new SeleniumDriverSetup();
		}
		return driver;
	}

	/**
	 * Quits the driver which will also close browser.
	 */
	public static void quitDriver() {
		synchronized (LOCKOBJ) {
			if (driver != null) {
				driver.quit();
				driver = null;
			}
		}
	}

	/**
	 * Sets WebDriver instance.
	 *
	 * @param driver the webDriver
	 */
	public static void setDriver(final WebDriver driver) {
		SeleniumDriverSetup.driver = driver;
	}

	/**
	 * Returns WebDriver instance.
	 *
	 * @return driver WebDriver instance
	 */
	public static WebDriver getDriverInstance() {
		return driver;
	}

}
