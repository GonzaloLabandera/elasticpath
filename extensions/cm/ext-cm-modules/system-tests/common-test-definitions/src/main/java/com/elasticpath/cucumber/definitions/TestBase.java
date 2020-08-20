package com.elasticpath.cucumber.definitions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.elasticpath.selenium.dialogs.ProblemOccurredDialog;
import com.elasticpath.selenium.framework.util.PropertyManager;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.Utility;

/**
 * Test Base class contains some methods for Before and After tests to be run for each test.
 */
public class TestBase {
	private boolean screenShotTaken;
	private static final Logger LOGGER = Logger.getLogger(TestBase.class);
	private ProblemOccurredDialog problemOccurredDialog;
	private static final String MIME_TYPE_IMAGE = "image/png";
	private LogEntries driverLogEntries;
	private String cortexFileName;
	private static String cortexLogFolderPath;
	private static boolean propBuildDirExists;

	/**
	 * For Before hooks, lower order number runs first.
	 *
	 * For After hooks, higher order number runs first.
	 * Default order value is 10000
	 */

	/**
	 * Runs before each scenario.
	 */
	@Before(order = 1)
	public void beforeScenario(final Scenario scenario) {
		String buildDirectory = PropertyManager.getInstance().getProperty("project.build.directory");
		if (buildDirectory != null && buildDirectory.length() > 0) {
			propBuildDirExists = true;
			cortexLogFolderPath = buildDirectory + "/cortexLogs/";
			cortexFileName = scenario.getName() + "_" + Utility.getRandomUUID() + ".log";
			File file = new File(cortexLogFolderPath);

			if (!file.exists()) {
				file.mkdir();
			}

			try {
				PrintStream fileOut = new PrintStream(cortexLogFolderPath + cortexFileName);
				System.setOut(fileOut);
			} catch (Exception ex) {
				LOGGER.info(ex.getMessage());
			}
		}
	}

	/**
	 * @return cortex log file.
	 */
	private String getCortexLog() {
		try {
			if (propBuildDirExists) {
				return new String(Files.readAllBytes(Paths.get(cortexLogFolderPath + cortexFileName)));
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Takes screenshot on scenario failure, it will run after scenario's steps and its @After hook.
	 *
	 * @param scenario Scenario
	 */
	@After(order = 1)
	public void finalTearDown(final Scenario scenario) {
		try {
			if (scenario.isFailed() && !screenShotTaken && SeleniumDriverSetup.getDriverInstance() != null) {
				scenario.embed(getScreenShot(), MIME_TYPE_IMAGE);
			}
		} catch (UnhandledAlertException uae) {
			scenario.embed(getScreenShot(), MIME_TYPE_IMAGE);
			SeleniumDriverSetup.getDriver().switchTo().alert().accept();
		} finally {
			SeleniumDriverSetup.quitDriver();
		}
	}

	/**
	 * Sets driver log entries.
	 */
	private void setDriverLogEntries() {
		driverLogEntries = SeleniumDriverSetup.getDriver().manage().logs().get(LogType.DRIVER);
	}

	/**
	 * Captures the logs from the chromeDriver.
	 */
	private void analyzeLog() {
		for (LogEntry entry : driverLogEntries) {
			LOGGER.debug(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
		}
	}

	/**
	 * @return driver logs.
	 */
	private String getDriverLogs() {
		String logs = "";
		for (LogEntry entry : driverLogEntries) {
			logs = logs + new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage() + System.lineSeparator();
		}
		return logs;
	}

	/**
	 * Takes screenshot on scenario step failure, it will run after scenario's steps but before its @After hook.
	 *
	 * @param scenario Scenario
	 */
	@After(order = Constants.SCREENSHOT_ORDER_NUMBER)
	public void tearDown(final Scenario scenario) {
		if (scenario.isFailed()) {
			if (getCortexLog() != null && getCortexLog().length() > 0) {
				String cortexHtml = "<b>Cortex Logs:</b><br>"
						+ "<textarea rows='50' cols='175'>" + getCortexLog() + "</textarea> ";
				scenario.embed(cortexHtml.getBytes(), "text/html");
			}
			if (SeleniumDriverSetup.getDriverInstance() != null) {
				handleProblemOccuredDialogPresent();
				setDriverLogEntries();
				LOGGER.debug("START of failure logs for scenario " + scenario.getName());
				analyzeLog();
				LOGGER.debug("END of failure logs for scenario " + scenario.getName());
				try {
					scenario.embed(getScreenShot(), MIME_TYPE_IMAGE);

					String logHtml = "<b>Driver Logs:</b><br>"
							+ "<textarea rows='50' cols='175'>" + getDriverLogs() + "</textarea> ";
					scenario.embed(logHtml.getBytes(), "text/html");

					String html = "<b>Page Source</b><br>"
							+ "<textarea rows='50' cols='175'>" + SeleniumDriverSetup.getDriver().getPageSource() + "</textarea> ";
					scenario.embed(html.getBytes(), "text/plain");

					screenShotTaken = true;
				} catch (UnhandledAlertException uae) {
					scenario.embed(getScreenShot(), MIME_TYPE_IMAGE);
					screenShotTaken = true;
					SeleniumDriverSetup.getDriver().switchTo().alert().accept();
				}
			}
		}
	}

	/**
	 * Checks if `Problem Occured` Dialog is present, and if present, clicks the Details
	 * button on the dialog.
	 */
	private void handleProblemOccuredDialogPresent() {
		problemOccurredDialog = new ProblemOccurredDialog(SeleniumDriverSetup.getDriver());
		if (problemOccurredDialog.isDialogPresent()) {
			problemOccurredDialog.clickDetailsButton();
		}
	}

	/**
	 * @return screenshot.
	 */
	private byte[] getScreenShot() {
		return ((TakesScreenshot) SeleniumDriverSetup.getDriver()).getScreenshotAs(OutputType.BYTES);
	}
}
