package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

public class PaymentProcessingErrorDialog extends AbstractPageObject {

	public static final String OK_BUTTON_CSS = "div[widget-id='OK'][seeable='true']";
	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this webpage.
	 */
	public PaymentProcessingErrorDialog(final WebDriver driver) {
		super(driver);
	}

	public void verifyErrorMessageDisplayedInPaymentProcessingErrorDialog(final String expErrorMessage) {
		try {
			getDriver().findElement(By.cssSelector(String.format("div[widget-id*='" + expErrorMessage + "'] ", expErrorMessage)));
		} catch (Exception e) {
			assertThat(false)
					.as("Expected error message not present - " + expErrorMessage)
					.isEqualTo(true);
		}
	}

}
