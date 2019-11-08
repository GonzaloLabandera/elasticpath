package com.elasticpath.selenium.toolbars;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.wizards.PaymentAuthorizationWizard;

/**
 * Customer Service Toolbar.
 */
public class CustomerServiceActionToolbar extends AbstractToolbar {

	private static final String VIEW_CUSTOMER_IMPORT_JOBS_BUTTON_CSS = "div[widget-id='IMPORTS']";
	private static final String SAVE_ALL = "Save All";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CustomerServiceActionToolbar(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks Save All button.
	 *
	 * @return PaymentAuthorizationWizard
	 */
	public PaymentAuthorizationWizard clickSaveAllButton() {
		sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);
		clickButton(SAVE_ALL_BUTTON_CSS, SAVE_ALL);
		return new PaymentAuthorizationWizard(getDriver());
	}

	/**
	 * Verifies View Customer Import Jobs button is present.
	 */
	public void verifyCustomerImportJobsButtonIsPresent() {
		assertThat(isElementPresent(By.cssSelector(VIEW_CUSTOMER_IMPORT_JOBS_BUTTON_CSS)))
				.as("Unable to find View Customer Import Jobs button")
				.isTrue();
	}

}
