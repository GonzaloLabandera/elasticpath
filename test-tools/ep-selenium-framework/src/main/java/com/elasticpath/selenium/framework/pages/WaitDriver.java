package com.elasticpath.selenium.framework.pages;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * WebDriverWait helper method class.
 */
public class WaitDriver {

	private static final Logger LOGGER = Logger.getLogger(WaitDriver.class);

	private static final int ONE_SECOND_IN_MILLISECONDS = 1000;

	/**
	 * The default wait interval for the wait driver.
	 */
	private static final int WEBDRIVER_WAIT_INTERVAL = Integer.parseInt(PropertyManager.getInstance().getProperty("selenium.waitdriver.timeout"));

	private final WebDriver driver;

	private WebDriverWait wait;

	/**
	 * Constructor.
	 *
	 * @param driver The WebDriver
	 */
	public WaitDriver(final WebDriver driver) {
		this.driver = driver;
		wait = new WebDriverWait(driver, WEBDRIVER_WAIT_INTERVAL);
	}

	/**
	 * Adjusts the web driver to wait the default number of seconds.
	 */
	public void adjustWaitBackToDefault() {
		adjustWaitInterval(WEBDRIVER_WAIT_INTERVAL);
	}

	/**
	 * Adjusts the web driver to wait the specified number of seconds.
	 *
	 * @param interval The interval in seconds
	 */
	public void adjustWaitInterval(final int interval) {
		wait = new WebDriverWait(driver, interval);
	}

	/**
	 * Waits for specified amount of seconds.
	 *
	 * @param timeInSeconds Time to wait in seconds
	 */
	public void waitFor(final int timeInSeconds) {
		try {
			Thread.sleep(timeInSeconds * ONE_SECOND_IN_MILLISECONDS);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Checks if there is an alert present and returns true/false.
	 *
	 * @param timeOutInSeconds Time to wait in seconds for alert to be present before it throws an error
	 * @return true or false depending if the alert is displayed
	 */
	public boolean waitForAlertPresent(final long timeOutInSeconds) {
		wait.withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
		if (wait.until((webDriver) -> {return ExpectedConditions.alertIsPresent();}) == null) {

			return false;
		}

		return true;
	}

	/**
	 * wait for element presence before the specified time out.
	 *
	 * @param byMethod how webdriver is found by.
	 * @param timeOutInSeconds time out.
	 */
	public void waitForElement(final By byMethod, final long timeOutInSeconds) {
		wait.withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
		wait.until((webDriver) -> {return ExpectedConditions.presenceOfElementLocated(byMethod);});
	}

	/**
	 * Wait for element to be displayed.
	 *
	 * @param byMethod Method to search for the element
	 * @param timeOutInSeconds Time to wait in seconds before it throws an error
	 */
	public void waitForElementDisplayed(final By byMethod, final long timeOutInSeconds) {
		wait.withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
		wait.until((webDriver) -> {return ExpectedConditions.visibilityOfElementLocated(byMethod);});
	}

	/**
	 * Same as above except it takes in a {@link WebElement} instead of a {@link By).
	 *
	 * @param element Expected element to be displayed
	 * @param timeOutInSeconds Time to wait in seconds before it throws an error
	 */
	public void waitForElementDisplayed(final WebElement element, final long timeOutInSeconds) {
		wait.withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
		wait.until((webDriver) -> {return ExpectedConditions.visibilityOf(element);});
	}

	/**
	 * Waits for element to be enabled.
	 *
	 * @param element Element to wait to be enabled
	 * @param timeOutInSeconds Time to wait in seconds before it throws an error
	 * @return True if element is enabled and false otherwise
	 */
	public boolean waitForElementEnabled(final WebElement element, final int timeOutInSeconds) {
		wait.withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
		return wait.until((driver) -> {
				return element.isEnabled();
		});
	}

	/**
	 * Waits for an element that is expected to be appearing multiple times and returns it.
	 *
	 * @param byMethod Search method to search for the element
	 * @param timeOutInSeconds Time to wait in seconds before it throws an error
	 * @return The list of elements that are displayed
	 */
	public List<WebElement> waitForElementsListDisplayed(final By byMethod, final long timeOutInSeconds) {
		wait.withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
		//todo, possibly debug this?
		return wait.until((webDriver) -> {
			return ExpectedConditions.presenceOfAllElementsLocatedBy(byMethod).apply(webDriver);
		});
	}

	/**
	 * Waits for an element to no longer be stale.
	 *
	 * @param elements List of elements to not be stale
	 */
	public void waitForElementsToBeNotStale(final List<WebElement> elements) {
		wait.withTimeout(WaitDriver.WEBDRIVER_WAIT_INTERVAL, TimeUnit.SECONDS);
		wait.until((driver)-> {
				driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
				try {
					for (WebElement element : elements) {
						element.getText();
					}
				} catch (StaleElementReferenceException e) {
					return false;
				} finally {
					driver.manage().timeouts().implicitlyWait(WaitDriver.WEBDRIVER_WAIT_INTERVAL, TimeUnit.SECONDS);
				}
				return true;
		});
	}

	/**
	 * Waits for a select element to change values from its original value.
	 *
	 * @param selectElement the select element to watch
	 * @param originalValue the original value of the select element to change from
	 */
	public void waitForSelectChangeByValue(final Select selectElement, final String originalValue) {
		// final Select selectElement = new Select(element);
		wait.until((driver ) -> {
				return !originalValue.equals(selectElement.getFirstSelectedOption().getAttribute("value"));
		});
	}

	/**
	 * Waits for a select element to change text from its original text.
	 *
	 * @param selectElement the select element to watch
	 * @param originalVisibleText the original text of the select element to change from
	 */
	public void waitForSelectChangeByVisibleText(final Select selectElement, final String originalVisibleText) {
		// final Select selectElement = new Select(element);
		wait.until((WebDriver driver) -> {
				return !originalVisibleText.equals(selectElement.getFirstSelectedOption().getText());
		});
	}

	/**
	 * Waits for text in the element to appear.
	 *
	 * @param element Element that contains the text to appear
	 * @param text Text to wait for
	 */
	public void waitForTextInElement(final WebElement element, final String text) {
		wait.until((driver) -> {
				try {
					return element.getText().contains(text);
				} catch (StaleElementReferenceException e) {
					return false;
				}
		});
	}

}
