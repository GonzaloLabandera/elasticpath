package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Edit Customer Profile Dialog
 */
public class EditCustomerProfileDialog extends AbstractDialog {

	/**
	 * Constant for the sku option dialog shell css.
	 */
	public static final String EDIT_CUSTOMER_PROFILE_PARENT_CSS
			= "div[automation-id*='CustomerDetailsProfilerPage'][seeable='true'] ";

	private static final String CUSTOMER_STATUS_COMBO_BOX_CSS = EDIT_CUSTOMER_PROFILE_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.fulfillment.FulfillmentMessages.ProfileBasicSection_Status'][widget-type='CCombo']";

	/**
	 * Constructor.
	 *
	 * @param driver the driver.
	 */
	public EditCustomerProfileDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verify that status can be selected.
	 *
	 * @param customerStatus customer status.
	 */
	public void selectCustomerStatus(final String customerStatus) {
		assertThat(selectComboBoxItem(CUSTOMER_STATUS_COMBO_BOX_CSS, customerStatus))
				.as("Unable to find customer status - " + customerStatus)
				.isTrue();
	}

	/**
	 * Verify that status can not be selected.
	 *
	 * @param customerStatus customer status.
	 */
	public void verifySelectValueIsNotAvailable(final String customerStatus) {
		assertThat(selectComboBoxItem(CUSTOMER_STATUS_COMBO_BOX_CSS, customerStatus))
				.as("Selected value is present - " + customerStatus)
				.isFalse();
	}

	/**
	 * Get selected customer status.
	 *
	 * @return selected status value
	 */
	public String getCurrentStatus() {
		return getDriver().findElement(By.cssSelector(CUSTOMER_STATUS_COMBO_BOX_CSS + " input")).getAttribute("value");
	}
}
