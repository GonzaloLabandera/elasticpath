package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * Validation Errors dialog.
 */
public class ErrorDialog extends AbstractDialog {

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ErrorDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verify Error message.
	 *
	 * @param errMsg the error message.
	 */
	public void verifyErrorMessage(final String errMsg) {
		try {
			getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format("div[widget-id*='" + errMsg + "'] ", errMsg)));
			clickOK();
		} catch (TimeoutException e) {
			assertThat(false)
					.as("Error message validation failed").isEqualTo(true);
		}
	}

}
