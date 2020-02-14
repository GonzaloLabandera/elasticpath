/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.util.Constants;

/**
 * Edit Payment Configuration dialog.
 */
public class EditPaymentConfigurationDialog extends AbstractDialog {

	public static final String CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations"
			+ ".AdminPaymentConfigurationMessages.CreatePaymentConfigurationDialog'] ";
	public static final String CONFIGURATION_NAME_CSS = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations"
			+ ".AdminPaymentConfigurationMessages.PaymentConfigurationNameLabel'] input";
	public static final String DISPLAY_NAME_CSS = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations"
			+ ".AdminPaymentConfigurationMessages.PaymentConfigurationDisplayNameLabel'] input";
	public static final String PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS = "div[widget-id='Payment Configuration Properties Table']";
	public static final String PROPERTY_FIELD_CSS = " div[row-id='%s'] div[column-id='%s']";
	public static final String SAVE_BUTTON_CSS =
			"div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonSave']";
	public static final String LOCALIZED_DISPLAY_NAME_CSS = "div[widget-type='Text'][tabindex='22'] input";
	public static final String LOCALIZED_DISPLAY_NAME_LOCALE_CSS = "div[widget-type='CCombo'][tabindex='23']";
	public static final String DELETE_FIRST_LANGUAGE_LINK_CSS = "div[widget-type='ImageHyperlink'][tabindex='24']";

	/**
	 * Constructor.
	 *
	 * @param driver the driver.
	 */
	public EditPaymentConfigurationDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks save button.
	 */
	@Override
	public void clickSave() {
		clickButton(SAVE_BUTTON_CSS, "Save");
		waitTillElementDisappears(By.cssSelector(CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS));
	}

	/**
	 * Enters configuration name.
	 * @param configName the configuration name
	 */
	public void enterConfigurationName(final String configName) {
		clearAndType(CONFIGURATION_NAME_CSS, configName);
	}

	/**
	 * Enters display name.
	 *
	 * @param displayName the configuration name
	 */
	public void enterDisplayName(final String displayName) {
		clearAndType(DISPLAY_NAME_CSS, displayName);
	}

	/**
	 * Enters display name.
	 *
	 * @param displayName the configuration name
	 */
	public void enterLocalizedDisplayName(final String displayName) {
		clearAndType(LOCALIZED_DISPLAY_NAME_CSS, displayName);
	}

	/**
	 * Selects payment method.
	 *
	 * @param paymentMethod the payment method
	 */
	public void selectLocalizedDisplayNameLocale(final String paymentMethod) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(LOCALIZED_DISPLAY_NAME_LOCALE_CSS));
		assertThat(selectComboBoxItem(LOCALIZED_DISPLAY_NAME_LOCALE_CSS, paymentMethod))
				.as("Unable to find payment method - " + paymentMethod)
				.isTrue();
	}

	/**
	 * Enter configuration property value.
	 *
	 * @param field    the property field
	 * @param oldValue the old value of property
	 * @param newValue the new value of property
	 */
	public void enterPaymentConfigurationValue(final String field, final String oldValue, final String newValue) {
		String propertyField = PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS + String.format(PROPERTY_FIELD_CSS, field, oldValue);
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(propertyField)).click();
		sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);
		clearAndType(PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS + " input", newValue);
	}

	/**
	 * Verifies is payment configuration value not interactive.
	 *
	 * @param field the property field
	 * @param value the value of property
	 */
	public void verifyIsPaymentConfigurationValueNotInteractive(final String field, final String value) {
		final String propertyField = PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS + String.format(PROPERTY_FIELD_CSS, field, value);
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(propertyField)).click();
		sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);

		assertThat(isElementInteractive(PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS + " input"))
				.as("Payment configuration value should not be interactive")
				.isFalse();
	}

	/**
	 * Delete first localized propeties
	 */
	public void clickDeleteFirstLocalizedPropertiesLink() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(DELETE_FIRST_LANGUAGE_LINK_CSS)));
	}

}
