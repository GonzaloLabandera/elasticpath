package com.elasticpath.selenium.framework.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * Page super class. All page objects in this test framework extends the page super class.
 */
public abstract class AbstractPage {

	/** the driver which drives a particular page object. */
	private WebDriver driver;

	/** gets value from the properties file. */
	protected static final PropertyManager PROPERTY_MANAGER;

	private WaitDriver waitDriver;

	static {
		PROPERTY_MANAGER = PropertyManager.getInstance();
	}

	/**
	 * constructor.
	 * 
	 * @param driver webdriver which drives this webpage.
	 */
	public AbstractPage(final WebDriver driver) {
		super();
		this.driver = driver;
		PageFactory.initElements(driver, this);
		waitDriver = new WaitDriver(driver);
		afterInit();
	}

	/**
	 * This method will be executed after the page is created. Objects that inherit this class can override this class to verify pages for example.
	 */
	public abstract void afterInit();

	/**
	 * checks if javascript alert popup is present.
	 */
	public Boolean isAlertPresent() {
		try{
			driver.switchTo().alert();
			return true;
		}
		catch(Exception e){
			return false;
		}
	}

	/**
	 * Accepts a javascript alert popup.
	 */
	public void alertAccept() {
		Alert javaScriptAlert = driver.switchTo().alert();
		javaScriptAlert.accept();
	}

	/**
	 * Dismisses a javascript alert popup.
	 */
	public void alertDismiss() {
		Alert javaScriptAlert = driver.switchTo().alert();
		javaScriptAlert.dismiss();
	}

	/**
	 * short cut for both clear the field and type in new text.
	 * 
	 * @param element webElement for field.
	 * @param text what gets typed in the field.
	 */
	public void clearAndType(final WebElement element, final String text) {
		element.clear();
		if (text != null && !text.isEmpty()) {
			element.sendKeys(text);
		}
	}

	/**
	 * delete cookies for current browser.
	 */
	public void deleteCookies() {
		driver.manage().deleteAllCookies();
	}

	/**
	 * Gets the text content of an element using javascript (for special cases like the html5 cufon tag).
	 * 
	 * @param element The element that contains the text
	 * @return The text of the element
	 */
	public String getTextContent(final WebElement element) {
		return (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent", element);
	}

	/**
	 * Move mouse over the specified element.
	 * 
	 * @param element Element to hover the mouse over
	 */
	public void hoverMouseOverElement(final WebElement element) {
		Actions builder = new Actions(driver);
		builder.moveToElement(element).build().perform();
	}

	/**
	 * Checks if the element is present on the page.
	 * 
	 * @param element Element to check if present on the page
	 * @return true if element is present on the page
	 */
	public boolean isElementPresent(final WebElement element) {
		try {
			element.getTagName();
			return true;
		} catch (NoSuchElementException e) {
			return false;
		} catch (StaleElementReferenceException e) {
			return false;
		}
	}

	/**
	 * Same as isElementPresent(final WebElement element) except that it takes in a parent and a by method.
	 * 
	 * @param parent Parent element that contains the element
	 * @param byMethod Method to search for the element
	 * @return true if element is present on the page
	 */
	public boolean isElementPresent(final WebElement parent, final By byMethod) {
		try {
			parent.findElement(byMethod);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	/**
	 * Same as isElementPresent(final WebElement element) except that it takes in a by method.
	 *
	 * @param by Method to search for the element
	 * @return true if element is present on the page
	 */
	@SuppressWarnings({"PMD.ShortVariable"})
	public boolean isElementPresent(final By by){
		try{
			driver.findElement(by);
			return true;
		}catch(NoSuchElementException e) {
			return false;
		} catch (StaleElementReferenceException e) {
			return false;
		}
	}

	/**
	 * Refreshes the page.
	 */
	public void refreshPage() {
		driver.navigate().refresh();
	}

	/**
	 * Helper method to select an option in a drop down list. Checks if visibleText is not null before performing.
	 * 
	 * @param dropDownElement The select element to set
	 * @param visibleText The visible text to set to
	 */
	public void setDropDownValueByVisibleText(final WebElement dropDownElement, final String visibleText) {
		if (visibleText != null) {
			Select dropDown = new Select(dropDownElement);
			dropDown.selectByVisibleText(visibleText);
		}
	}

	/**
	 * Switch back to the main frame.
	 */
	public void switchBackToOriginalFrame() {
		driver = driver.switchTo().defaultContent();
	}

	/**
	 * Switches to a frame element.
	 * 
	 * @param frameElement The frame element to switch into
	 */
	public void switchToFrame(final WebElement frameElement) {
		driver = driver.switchTo().frame(frameElement);
	}

	/**
	 * @param waitDriver the waitDriver to set
	 */
	public void setWaitDriver(final WaitDriver waitDriver) {
		this.waitDriver = waitDriver;
	}

	/**
	 * @return the waitDriver
	 */
	public WaitDriver getWaitDriver() {
		return waitDriver;
	}

	protected WebDriver getDriver() {
		return driver;
	}

	protected void setDriver(final WebDriver driver) {
		this.driver = driver;
	}

	protected PropertyManager getPropertyManager() {
		return PROPERTY_MANAGER;
	}
}