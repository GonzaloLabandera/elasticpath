package com.elasticpath.cucumber.definitions;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.util.Constants;

/**
 * Test Base class contains some methods for Before and After tests to be run for each test.
 */
public class TestBase {
	private static boolean screenShotTaken;

	/**
	 * For After hooks, higher order number run first.
	 * Default order value is 10000
	 */

	/**
	 * Takes screenshot on scenario failure, it will run after scenario's steps and its @After hook.
	 *
	 * @param scenario Scenario
	 */
	@After(order = 1)
	public void tearDown(final Scenario scenario) {

		if (scenario.isFailed() && !screenShotTaken) {
			final byte[] screenshot = ((TakesScreenshot) SeleniumDriverSetup.getDriver()).getScreenshotAs(OutputType.BYTES);
			scenario.embed(screenshot, "image/png");
		}
		SeleniumDriverSetup.quitDriver();
	}

	/**
	 * Takes screenshot on scenario step failure, it will run after scenario's steps but before its @After hook.
	 *
	 * @param scenario Scenario
	 */
	@After(order = Constants.SCREENSHOT_ORDER_NUMBER)
	public void tearDown1(final Scenario scenario) {

		if (scenario.isFailed()) {
			final byte[] screenshot = ((TakesScreenshot) SeleniumDriverSetup.getDriver()).getScreenshotAs(OutputType.BYTES);
			scenario.embed(screenshot, "image/png");
			screenShotTaken = true;
		}
	}
}
