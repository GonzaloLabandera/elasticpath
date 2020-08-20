package com.elasticpath.cucumber.definitions;

import java.util.List;

import cucumber.api.java.en.Then;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.ErrorDialog;
import com.elasticpath.selenium.dialogs.ValidationErrorsDialog;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;

/**
 * Common definitions.
 */
public class CommonDefinition {

	private final WebDriver driver;

	/**
	 * Constructor.
	 */
	public CommonDefinition() {
		driver = SeleniumDriverSetup.getDriver();
	}

	/**
	 * Verify Error message.
	 *
	 * @param errMsg the error message.
	 */
	@Then("^I should see error (.+)$")
	public void verifyErrorMessage(final String errMsg) {
		new ValidationErrorsDialog(driver).verifyErrorMessage(errMsg);
	}

	/**
	 * Verify error message.
	 *
	 * @param errMsgList the list of messages.
	 */
	@Then("^I should see following error messages?$")
	public void verifyErrorMessages(final List<String> errMsgList) {
		new ValidationErrorsDialog(driver).verifyAllErrorMessages(errMsgList);
	}

	/**
	 * Verify Error message.
	 *
	 * @param errMsg the error message.
	 */
	@Then("^I should see the following error: (.+)$")
	public void verifyGenericErrorMessage(final String errMsg) {
		new ErrorDialog(driver).verifyErrorMessage(errMsg);
	}

}
