package com.elasticpath.selenium.util;

/**
 * Extended WaitDriver class to use FluentWait.
 */

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import com.elasticpath.selenium.framework.pages.WaitDriver;
import com.elasticpath.selenium.framework.util.PropertyManager;
import com.elasticpath.selenium.setup.SetUp;

/**
 * WebDriverWait helper method class.
 */
public class FluentWaitDriver extends WaitDriver {

	private static final int POLLING_INTERVAL = 500;
	private static final String ATTRIBUTE_VALUE = "value";
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(FluentWaitDriver.class);

	private final FluentWait<WebDriver> wait;

	private final JavascriptExecutor jsDriver;

	private final WebDriver webDriver;

	private static final long WEBDRIVER_DEFAULT_TIMEOUT = Long.parseLong(PropertyManager.getInstance().getProperty("selenium.waitdriver.timeout"));


	/**
	 * Constructor.
	 *
	 * @param driver The WebDriver
	 */
	public FluentWaitDriver(final WebDriver driver) {
		super(driver);
		webDriver = driver;
		wait = new FluentWait<WebDriver>(driver)
				.withTimeout(WEBDRIVER_DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.pollingEvery(POLLING_INTERVAL, TimeUnit.MILLISECONDS)
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

		//set up the js driver.
		driver.manage().timeouts().setScriptTimeout(WEBDRIVER_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
		jsDriver = (JavascriptExecutor) driver;
	}

	/**
	 * Waits for an element that is expected to be visible multiple times and returns it, such as a row or list of buttons.
	 *
	 * @param findBy Search method to search for the element
	 * @return The list of elements that are displayed
	 */
	public List<WebElement> waitForElementsListVisible(final By findBy) {
		return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(findBy));
	}


	/**
	 * Sets the wait duration of FluentWait.
	 *
	 * @param timeOutInSeconds the duration
	 */
	public void setFluentWaitTimer(final long timeOutInSeconds) {
		wait.withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Sets the wait duration of FluentWait back to default.
	 */
	public void setFluentWaitTimerBackToDefault() {
		wait.withTimeout(WEBDRIVER_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
	}

	/**
	 * Waits for element list that is expected to be visible multiple times and returns it, such as a row or list of buttons.
	 *
	 * @param elements Search method to search for the element
	 * @return The list of elements that are displayed
	 */
	public List<WebElement> waitForElementsListVisible(final List<WebElement> elements) {
		return wait.until(ExpectedConditions.visibilityOfAllElements(elements));
	}

	/**
	 * Waits for one or more elements that is expected to be present once or multiple times and returns them, such as a row or list of buttons.
	 *
	 * @param findBy Search method to search for the element
	 * @return The list of elements that are present
	 */
	public List<WebElement> waitForElementsListPresent(final By findBy) {
		return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(findBy));
	}

	/**
	 * Waits until an element is interactable and clickable.
	 *
	 * @param findBy the FindBy condition
	 * @return the webelement the web element
	 */
	public WebElement waitForElementToBeClickable(final By findBy) {
		webDriver.findElement(findBy);
		if (!findBy.toString().contains("xpath")) {
			String[] selectorStringArray = findBy.toString().split(":", 2);
			String selectorString = selectorStringArray[1].trim();
			waitForElementToBeInteractable(selectorString);
		}
		return wait.until(ExpectedConditions.elementToBeClickable(findBy));
	}

	/**
	 * Wait until an element is clickable.
	 *
	 * @param element Webelement
	 * @return WebElement that is clickable
	 */
	public WebElement waitForElementToBeClickable(final WebElement element) {
		return wait.until(ExpectedConditions.elementToBeClickable(element));
	}

	/**
	 * Waits until an element is present.
	 *
	 * @param findBy the FindBy condition
	 * @return the webelement that is visible
	 */
	public WebElement waitForElementToBePresent(final By findBy) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(findBy));
	}

	/**
	 * Waits until an element is visible.
	 *
	 * @param findBy the FindBy condition
	 * @return the webelement that is visible
	 */
	public WebElement waitForElementToBeVisible(final By findBy) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(findBy));
	}

	/**
	 * Waits until an element is invisible.
	 *
	 * @param findBy the FindBy condition
	 * @return true if the element is invisible, false otherwise
	 */
	public Boolean waitForElementToBeInvisible(final By findBy) {
		//As the element is not in DOM, it takes implicit timeout to complete. We are setting it to 1 second to reduce the wait time when element
		// not in DOM.
		SetUp.getDriver().manage().timeouts().implicitlyWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS, TimeUnit.SECONDS);
		Boolean until = wait.until(ExpectedConditions.refreshed(ExpectedConditions.invisibilityOfElementLocated(findBy)));
		SetUp.getDriver().manage().timeouts().implicitlyWait(WEBDRIVER_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
		return until;
	}

	/**
	 * Waits until an element is interactable (IE, it is the top element in the DOM for its given position).
	 * This is useful to determine whether an element is covered by a busy indicator, or dialog box,
	 * where it is still visible, but will not receive a click event.
	 * <p>
	 * Example Usage: waitForElementToBeInteractable("[widget-id='User Menu']");
	 *
	 * @param elementSelector The element's CSS selector.
	 * @return boolean true if it is interactable, false if it's not.
	 */
	public boolean waitForElementToBeInteractable(final String elementSelector) {
		waitForElementToBePresent(By.cssSelector(elementSelector));
		waitForElementToBeVisible(By.cssSelector(elementSelector));
		try {
			return wait.until(driver -> (Boolean) jsDriver.executeScript(" return EPTest.isElementInteractable(\"" + elementSelector + "\");"));
		} catch (Exception e) {
			LOGGER.debug(elementSelector + " - " + e.getMessage());
		}
		return false;
	}

	/**
	 * Waits for text in input.
	 *
	 * @param findBy       the FindBy condition
	 * @param expectedText the expected text
	 */
	public void waitForTextInInput(final By findBy, final String expectedText) {
		wait.until(ExpectedConditions.attributeContains(findBy, ATTRIBUTE_VALUE, expectedText));
	}

	/**
	 * Waits for button to be enabled.
	 *
	 * @param cssSelector the css selector
	 */
	public void waitForButtonToBeEnabled(final String cssSelector) {
		ExpectedCondition<Boolean> expectation = driver -> (Boolean) ((JavascriptExecutor) driver).executeScript(String.format(
				"return EPTest.isButtonEnabled(\"%s\");", cssSelector));
		try {
			wait.until(expectation);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage() + " for element - " + cssSelector);
		}
	}


	/**
	 * Waits for button to be disabled.
	 *
	 * @param cssSelector the css selector
	 */
	public void waitForButtonToBeDisabled(final String cssSelector) {
		ExpectedCondition<Boolean> expectation = driver -> !(Boolean) ((JavascriptExecutor) driver).executeScript(String.format(
				"return EPTest.isButtonEnabled(\"%s\");", cssSelector));
		try {
			wait.until(expectation);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage() + " for element - " + cssSelector);
		}
	}

	/**
	 * Waits for text in the element to appear.
	 *
	 * @param cssString the css selector
	 * @param text      Text to wait for
	 */
	public void waitForTextInElement(final String cssString, final String text) {
		try {
			wait.until(driver -> {
				try {
					return driver.findElement(By.cssSelector(cssString)).getText().contains(text);
				} catch (StaleElementReferenceException e) {
					return false;
				}
			});
		} catch (Exception e) {
			LOGGER.debug(e.getMessage() + " for element - " + cssString);
		}
	}

	/**
	 * Waits for value in the element to appear.
	 *
	 * @param cssString the css selector
	 * @param text      Text to wait for
	 * @return boolean true if element attribute contains expected text, else returns false
	 */
	public boolean waitForValueInElement(final String cssString, final String text) {
		return wait.until(driver -> {
			try {
				LOGGER.debug("expected text - " + text);
				LOGGER.debug("actual text - " + driver.findElement(By.cssSelector(cssString)).getAttribute(ATTRIBUTE_VALUE));
				return driver.findElement(By.cssSelector(cssString)).getAttribute(ATTRIBUTE_VALUE).contains(text);
			} catch (StaleElementReferenceException e) {
				return false;
			} catch (Exception e) {
				LOGGER.debug("waitForValueInElement() exception: " + e.getMessage());
				return false;
			}
		});
	}


	/**
	 * Waits for an element to no longer be stale.
	 *
	 * @param cssString the css selector
	 * @return boolean
	 */
	public boolean waitForElementToBeNotStale(final String cssString) {

		return wait.until(driver -> {
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

			try {
				driver.findElement(By.cssSelector(cssString)).getText();

			} catch (StaleElementReferenceException e) {
				LOGGER.debug("Element " + cssString + " is stale at this point ");
				return false;
			} finally {
				driver.manage().timeouts().implicitlyWait(WEBDRIVER_DEFAULT_TIMEOUT, TimeUnit.SECONDS);
			}

			return true;
		});
	}

	/**
	 * Waits for browser window to be maximized.
	 *
	 * @return true if it's maximized, else returns false
	 */
	public boolean waitForWindowToMaximize() {
		try {
			return wait.until(driver -> (Boolean) jsDriver.executeScript(" return (window.screen.height === window.outerHeight)"));
		} catch (Exception e) {
			return false;
		}
	}

}
