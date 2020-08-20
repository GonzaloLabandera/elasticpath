package com.elasticpath.selenium.framework.webdriverfactories;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * abstract webdriver factory.
 */
public abstract class AbstractWebDriverFactory implements WebDriverFactory {

	/**
	 * selenium session timeout.
	 */
	private final transient String timeout = PROPERTY_MANAGER.getProperty("selenium.session.timeout");

	@Override
	public abstract WebDriver createWebDriver();

	@Override
	public abstract RemoteWebDriver createRemoteWebDriver(final String remoteWebDriverURL);

	/**
	 * @param driver driver for this webpage.
	 * @param script the java script executed.
	 * @return executing java scripts "script".
	 */
	public static Object executeJavascript(final WebDriver driver, final String script) {
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		return jsExecutor.executeScript(script);
	}

	/**
	 * Gets the timeout.
	 *
	 * @return timeout the timeout
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * set time out session for this driver.
	 *
	 * @param driver target webdriver.
	 */
	public void setSessionTimeOut(final WebDriver driver) {
		driver.manage().timeouts().implicitlyWait(Long.parseLong(this.getTimeout()), TimeUnit.SECONDS);
	}
}
