package com.elasticpath.selenium.framework.webdriverfactories;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.elasticpath.selenium.framework.util.ScreenShotRemoteWebDriver;

/**
 * generates chrome browser.
 */
public class ChromeWebDriverFactory extends AbstractWebDriverFactory {

	private static ChromeOptions options;
	private static DesiredCapabilities capabilities;

	private static final Logger LOGGER = Logger.getLogger(ChromeWebDriverFactory.class);

	@Override
	public WebDriver createWebDriver() {
		System.setProperty("webdriver.chrome.driver", PROPERTY_MANAGER.getProperty("selenium.chrome.driver.path"));
		WebDriver driver = new ChromeDriver(getOptions());
		setSessionTimeOut(driver);
		return driver;
	}

	/**
	 * @param remoteWebDriverURL webdriver url.
	 * @return remote webdriver if gets desired capabilities for chrome webdriver.
	 */
	@Override
	public RemoteWebDriver createRemoteWebDriver(final String remoteWebDriverURL) {
		ScreenShotRemoteWebDriver driver = null;
		try {
			setDesiredCapabilities();
			for (int i = 0; i < 5; i++) {
				try {
					driver = new ScreenShotRemoteWebDriver(new URL(remoteWebDriverURL), getDesiredCapabilities());
					setSessionTimeOut(driver);
					LOGGER.debug(i + ": --------------- new driver initialized ---------------");
					break;
				} catch (WebDriverException ex) {
					if (i < 4) {
						LOGGER.debug(i + ": --------------- retrying driver initialization ---------------");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							LOGGER.error(e.getLocalizedMessage(), e);
						}
					} else {
						LOGGER.debug(i + ": --------------- maximum number of retry exceeded ---------------");
						LOGGER.error(ex.getLocalizedMessage(), ex);
					}
				}
			}
		} catch (MalformedURLException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
		}
		return driver;
	}

	/**
	 * Sets the Desired Capabilities.
	 */
	public static void setDesiredCapabilities() {
		capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(ChromeOptions.CAPABILITY, getOptions());
	}

	/**
	 * @return Desired Capabilities.
	 */
	public static DesiredCapabilities getDesiredCapabilities() {
		setDesiredCapabilities();
		return capabilities;
	}

	/**
	 * @return Chrome Options.
	 */
	public static ChromeOptions getOptions() {
		setOptions();
		return options;
	}

	/**
	 * Sets the Chrome Options.
	 */
	public static void setOptions() {
		options = new ChromeOptions();
		options.addArguments("disable-infobars");
		options.addArguments("--start-fullscreen");

		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable(LogType.DRIVER, Level.INFO);
		options.setCapability("goog:loggingPrefs", logPrefs);

		options.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
		options.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);

		Map<String, Object> prefs = new HashMap<>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		prefs.put("intl.accept_languages", PROPERTY_MANAGER.getProperty("ep.locale"));
		options.setExperimentalOption("prefs", prefs);

		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);

		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
	}

}
