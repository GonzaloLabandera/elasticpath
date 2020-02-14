package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.util.Constants;

/**
 * Create Payment Configuration dialog.
 */
public class CreatePaymentConfigurationDialog extends AbstractDialog {

	public static final String CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations"
			+ ".AdminPaymentConfigurationMessages.CreatePaymentConfigurationDialog'] ";
	public static final String PAYMENT_PROVIDER_SELECTOR_CSS = CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS + "div[widget-type='CCombo'][tabindex='12']";
	public static final String PAYMENT_METHOD_SELECTOR_CSS = CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS + "div[widget-type='CCombo'][tabindex='13']";
	public static final String CONFIGURATION_NAME_CSS = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations"
			+ ".AdminPaymentConfigurationMessages.PaymentConfigurationNameLabel'] input";
	public static final String DISPLAY_NAME_CSS = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations"
			+ ".AdminPaymentConfigurationMessages.PaymentConfigurationDisplayNameLabel'] input";
	public static final String FIRST_LOCALIZED_DISPLAY_NAME_CSS = CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS + "div[widget-type='Text'][tabindex='22'] input";
	public static final String FIRST_LANGUAGE_COMBO_BOX_CSS = CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS + "div[widget-type='CCombo'][tabindex='23']";
	public static final String SECOND_LOCALIZED_DISPLAY_NAME_CSS = CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS + "div[widget-type='Text'][tabindex='26'] input";
	public static final String SECOND_LANGUAGE_COMBO_BOX_CSS = CREATE_PAYMENT_CONFIGURATION_DIALOG_CSS + "div[widget-type='CCombo'][tabindex='27']";
	public static final String ADD_LOCALIZED_PROPERTIES_LINK_CSS = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations."
			+ "AdminPaymentConfigurationMessages.PaymentConfigurationAddLocalizationStringLink']";
	public static final String PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS = "div[widget-id='Payment Configuration Properties Table']";
	public static final String PROPERTY_FIELD_CSS = " div[row-id='%s'] div[column-id='<Enter a value>']";
	public static final String SAVE_BUTTON_CSS =
			"div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonSave']";
	public static final String NAME_VALIDATION_MESSAGE = "div[widget-id='%s']";
	public static final String CONFIGURATION_PROPERTY_KEY_COLUMN_NAME = "Property Key";
	public static final String CONFIGURATION_PROPERTY_KEY_COLUMN_CSS = "div[column-id='%s']";
	public static final String CONFIGURATION_PROPERTY_VALUE_COLUMN_CSS = CONFIGURATION_PROPERTY_KEY_COLUMN_CSS + "~div[column-num='1']";

	/**
	 * constructor.
	 *
	 * @param driver the driver.
	 */
	public CreatePaymentConfigurationDialog(final WebDriver driver) {
		super(driver);
	}


	/**
	 * Selects payment provider.
	 *
	 * @param paymentProvider the payment provider
	 */
	public void selectPaymentProvider(final String paymentProvider) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(PAYMENT_PROVIDER_SELECTOR_CSS));
		assertThat(selectComboBoxItem(PAYMENT_PROVIDER_SELECTOR_CSS, paymentProvider))
				.as("Unable to find payment provider - " + paymentProvider)
				.isTrue();
	}

	/**
	 * Selects payment method.
	 * @param paymentMethod the payment method
	 */
	public void selectPaymentMethod(final String paymentMethod) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(PAYMENT_METHOD_SELECTOR_CSS));
		assertThat(selectComboBoxItem(PAYMENT_METHOD_SELECTOR_CSS, paymentMethod))
				.as("Unable to find payment method - " + paymentMethod)
				.isTrue();
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
	 * @param displayName the configuration name
	 */
	public void enterDisplayName(final String displayName) {
		clearAndType(DISPLAY_NAME_CSS, displayName);
	}

	/**
	 * Enters first display name.
	 *
	 * @param displayName the configuration name
	 */
	public void enterFirstLocalizedDisplayName(final String displayName) {
		clearAndType(FIRST_LOCALIZED_DISPLAY_NAME_CSS, displayName);
	}

	/**
	 * Select first language.
	 *
	 * @param language language which should be chosen
	 */
	public void selectFirstLocalizedDisplayNameLocale(final String language) {
		selectComboBoxItem(FIRST_LANGUAGE_COMBO_BOX_CSS, language);
	}

	/**
	 * Enters second display name.
	 *
	 * @param displayName the configuration name
	 */
	public void enterSecondLocalizedDisplayName(final String displayName) {
		clearAndType(SECOND_LOCALIZED_DISPLAY_NAME_CSS, displayName);
	}

	/**
	 * Select second language.
	 *
	 * @param language language which should be chosen
	 */
	public void selectSecondLocalizedDisplayNameLocale(final String language) {
		selectComboBoxItem(SECOND_LANGUAGE_COMBO_BOX_CSS, language);
	}

	/**
	 * Click add localized properties link.
	 */
	public void clickAddLocalizedPropertiesLink() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(ADD_LOCALIZED_PROPERTIES_LINK_CSS)));
	}

	/**
	 * Enter configuration property value.
	 * @param key the property field
	 * @param value the property value
	 */
	public void enterConfigurationValue(final String key, final String value) {
		selectItemInDialog(PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS,
				CONFIGURATION_PROPERTY_KEY_COLUMN_CSS, key, CONFIGURATION_PROPERTY_KEY_COLUMN_NAME);
		String propertyField = String.format(CONFIGURATION_PROPERTY_VALUE_COLUMN_CSS, key);
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(propertyField)).click();
		sleep(Constants.SLEEP_ONE_SECOND_IN_MILLIS);
		clearAndType(PAYMENT_CONFIGURATION_PROPERTIES_TABLE_CSS + " input", value);
	}

	/**
	 * Verifies is save payment configuration button disabled.
	 */
	public void verifyIsSavePaymentConfigurationButtonDisabled() {
		setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
		verifyButtonIsDisabled(SAVE_BUTTON_CSS, "Save");
		setWebDriverImplicitWaitToDefault();
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
	 * Verifies configuration name validation message.
	 * @param validationMessage String
	 */
	public void verifyNameValidationMessage(final String validationMessage) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format(NAME_VALIDATION_MESSAGE, validationMessage)));
	}
}
