package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Abstract dialog class for common dialog actions. Dialog is a single prompt requesting user's confirmation.
 * Dialog classes should extend this class for common methods.
 */
public abstract class AbstractDialog extends AbstractPageObject {

	/**
	 * Cancel button css selector.
	 */
	protected static final String CANCEL_BUTTON_CSS = "div[widget-id='Cancel'][seeable='true']";
	/**
	 * OK button css selector.
	 */
	protected static final String OK_BUTTON_CSS = "div[widget-id='OK'][seeable='true']";
	private static final String SAVE_BUTTON_CSS = "div[widget-id='Save']";
	private static final String MOVERIGHT_BUTTON_CSS = "div[automation-id*='%s'] div[widget-id='Add']";
	private static final String MOVEALLLEFT_BUTTON_CSS = "div[automation-id*='%s'] div[widget-id='Remove All']";
	private static final String CLOSE_BUTTON = "div[widget-id='Close'][widget-type='Button']";

	/**
	 * constructor.
	 *
	 * @param driver the driver.
	 */
	public AbstractDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks cancel.
	 */
	public void clickCancel() {
		clickButton(CANCEL_BUTTON_CSS, "Cancel");
	}

	/**
	 * Clicks close.
	 */
	public void clickClose() {
		clickButton(CLOSE_BUTTON, "Close");
	}

	/**
	 * Closes dialog if it is opened.
	 */
	public void closeDialogIfOpened(){
		if (isCancelButtonPresent()) {
			clickCancel();
		}
	}

	/**
	 * Returns true if cancel button is present on the page, otherwise - false
	 *
	 * @return true if cancel button is present on the page, otherwise - false
	 */
	private boolean isCancelButtonPresent() {
		setWebDriverImplicitWait(1);
		if (getDriver().findElements(By.cssSelector(CANCEL_BUTTON_CSS)).size() > 0) {
			setWebDriverImplicitWaitToDefault();
			return true;
		}
		setWebDriverImplicitWaitToDefault();
		return false;
	}

	/**
	 * Clicks save.
	 */
	public void clickSave() {
		clickButton(SAVE_BUTTON_CSS, "Save");
	}

	/**
	 * Clicks OK.
	 */
	public void clickOK() {
		clickButton(OK_BUTTON_CSS, "OK");
	}

	/**
	 * Clicks '>' (move right).
	 *
	 * @param dialogAutomationId the css
	 */
	public void clickMoveRight(final String dialogAutomationId) {
		clickButton(String.format(MOVERIGHT_BUTTON_CSS, dialogAutomationId), dialogAutomationId);
	}

	/**
	 * Clicks '<<' (move all left).
	 *
	 * @param dialogAutomationId the css
	 */
	public void clickMoveAllLeft(final String dialogAutomationId) {
		clickButton(String.format(MOVEALLLEFT_BUTTON_CSS, dialogAutomationId), dialogAutomationId);
	}

	/**
	 * Verifies error message is displayed.
	 *
	 * @param errorMessage String
	 */
	public void verifyErrorMessageDisplayed(final String errorMessage) {
		setWebDriverImplicitWait(2);
		try {
			getDriver().findElement(By.cssSelector("div[widget-id*='" + errorMessage + "']"));
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message validation failed - '" + errorMessage + "'").isEqualTo(true);
		}
		setWebDriverImplicitWaitToDefault();
	}

}
