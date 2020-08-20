package com.elasticpath.cucumber.definitions;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cortex.dce.payment.PaymentMethodsSteps;
import com.elasticpath.selenium.dialogs.CreatePaymentConfigurationDialog;
import com.elasticpath.selenium.dialogs.EditPaymentConfigurationDialog;
import com.elasticpath.selenium.domainobjects.PaymentConfiguration;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.resultspane.PaymentConfigurationsResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.util.DBConnector;
import com.elasticpath.selenium.util.Utility;

/**
 * Payment step definitions.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class PaymentConfigurationDefinition {
	private static final Logger LOGGER = Logger.getLogger(PaymentConfigurationDefinition.class);
	private static final String PROPERTY_NAME = "Property name";
	private static final String OLD_VALUE = "Old value";
	private static final String NEW_VALUE = "New value";

	private final ConfigurationActionToolbar configurationActionToolbar;
	private final ActivityToolbar activityToolbar;
	private final PaymentConfiguration paymentConfiguration;
	private PaymentConfigurationsResultPane paymentConfigurationsResultPane;
	private CreatePaymentConfigurationDialog createPaymentConfigurationDialog;
	private EditPaymentConfigurationDialog editPaymentConfigurationDialog;
	private final WebDriver driver;

	/**
	 * Constructor.
	 */
	public PaymentConfigurationDefinition(final PaymentConfiguration paymentConfiguration) {
		driver = SeleniumDriverSetup.getDriver();
		configurationActionToolbar = new ConfigurationActionToolbar(driver);
		activityToolbar = new ActivityToolbar(driver);
		this.paymentConfiguration = paymentConfiguration;
	}

	/**
	 * Clicks on Payment Configurations.
	 */
	@When("^I go to Payment Configurations$")
	public void clickPaymentConfigurations() {
		activityToolbar.clickConfigurationButton();
		paymentConfigurationsResultPane = configurationActionToolbar.clickPaymentConfigurations();
	}

	/**
	 * Create payment configuration attached to store
	 */
	@And("^I create and activate payment configuration with following details$")
	public void createPaymentConfigurationAttachedToStore(final Map<String, String> paymentConfigurationMap) {
		clickPaymentConfigurations();
		createNewPaymentConfiguration(paymentConfigurationMap);
		savePaymentConfiguration();
		activatePaymentConfiguration();
	}

	/**
	 * Fill in payment configuration form.
	 * @param paymentConfigurationMap the configuration map
	 */
	@When("^I create a new Payment Configuration with following details$")
	public void createNewPaymentConfiguration(final Map<String, String> paymentConfigurationMap) {
		createPaymentConfigurationDialog = paymentConfigurationsResultPane.clickCreatePaymentConfigurationButton();
		createPaymentConfigurationDialog.selectPaymentProvider(paymentConfigurationMap.get("PROVIDER"));
		paymentConfiguration.setPaymentProvider(paymentConfigurationMap.get("PROVIDER"));
		createPaymentConfigurationDialog.selectPaymentMethod(paymentConfigurationMap.get("METHOD"));
		paymentConfiguration.setPaymentMethod(paymentConfigurationMap.get("METHOD"));
		String paymentConfigurationName = paymentConfigurationMap.get("CONFIGURATION_NAME") + Utility.getRandomUUID();
		paymentConfiguration.setConfigurationName(paymentConfigurationName);
		LOGGER.debug("Created Payment Configuration - " + paymentConfigurationName);
		createPaymentConfigurationDialog.enterConfigurationName(paymentConfigurationName);
		paymentConfiguration.setConfigurationName(paymentConfigurationName);
		createPaymentConfigurationDialog.enterDisplayName(paymentConfigurationMap.get("DISPLAY_NAME"));
	}

	/**
	 * Fill in payment configuration first localized display name and locale.
	 *
	 * @param localizedDisplayName the payment configuration localized display name.
	 * @param locale               the locale of payment configuration localized display name.
	 */
	@When("^I configure the first localized display name (.+) for (.+) locale$")
	public void setPaymentConfigurationFirstLocalizedDisplayName(final String localizedDisplayName, final String locale) {
		createPaymentConfigurationDialog.enterFirstLocalizedDisplayName(localizedDisplayName);
		createPaymentConfigurationDialog.selectFirstLocalizedDisplayNameLocale(locale);
		paymentConfiguration.setName(locale, localizedDisplayName);
	}

	/**
	 * Fill in payment configuration second localized display name and locale.
	 *
	 * @param localizedDisplayName the payment configuration localized display name.
	 * @param locale               the locale of payment configuration localized display name.
	 */
	@When("^I configure the second localized display name (.+) for (.+) locale$")
	public void setPaymentConfigurationSecondLocalizedDisplayName(final String localizedDisplayName, final String locale) {
		createPaymentConfigurationDialog.enterSecondLocalizedDisplayName(localizedDisplayName);
		createPaymentConfigurationDialog.selectSecondLocalizedDisplayNameLocale(locale);
		paymentConfiguration.setName(locale, localizedDisplayName);
	}

	/**
	 * Click add localized properties link.
	 */
	@When("^I click add localized properties")
	public void clickAddLocalizedProperties() {
		createPaymentConfigurationDialog.clickAddLocalizedPropertiesLink();
	}

	/**
	 * Click delete first localized properties link.
	 */
	@When("^I click delete first localization")
	public void clickDeleteLocalizedProperties() {
		editPaymentConfigurationDialog.clickDeleteFirstLocalizedPropertiesLink();
	}

	@When("^I open newly created payment method for the (.+) language")
	public void openPaymentConfigurationInLanguage(final String locale) {
		PaymentMethodsSteps.openPaymentConfigurationInLanguage(paymentConfiguration.getConfigurationName(), locale);
	}

	/**
	 * Fill in configuration properties fields.
	 * @param propertiesMap the properties map
	 */
	@When("^I configure the payment configuration properties$")
	public void configurePaymentConfigurationProperties(final Map<String, String> propertiesMap) {
		for (Map.Entry<String, String> paymentConfiguration : propertiesMap.entrySet()) {
			createPaymentConfigurationDialog.enterConfigurationValue(paymentConfiguration.getKey(), paymentConfiguration.getValue());
		}
	}

	/**
	 * Open the edit payment configuration dialog.
	 */
	@When("^I open the edit payment configuration dialog$")
	public void openEditPaymentConfigurationDialog() {
		paymentConfigurationsResultPane.verifyPaymentConfigurationExist(paymentConfiguration.getConfigurationName());
		editPaymentConfigurationDialog = paymentConfigurationsResultPane.clickEditPaymentConfigurationButton();
	}

	/**
	 * Check the descriptions keys.
	 */

	@When("^the payment configuration properties are in the following order$")
	public void verifyDescriptionsOrder(final List<String> descriptions) {
		paymentConfigurationsResultPane.verifyDescriptionOrder(descriptions);
	}

	/**
	 * Fill in payment configuration name.
	 *
	 * @param name the payment configuration name.
	 */
	@When("^I configure the payment configuration name (.+)$")
	public void updatePaymentConfigurationName(final String name) {
		String newPaymentConfigurationName = name + Utility.getRandomUUID();
		editPaymentConfigurationDialog.enterConfigurationName(newPaymentConfigurationName);
		paymentConfiguration.setConfigurationName(newPaymentConfigurationName);
	}

	/**
	 * Update configuration name to use an existing name.
	 */
	@When("^I update the payment configuration name to use an existing name (.+)$")
	public void updateSamePaymentConfigurationName(final String existingName) {
		editPaymentConfigurationDialog.enterConfigurationName(existingName);
	}

	/**
	 * Fill in payment configuration display name.
	 *
	 * @param displayName the payment configuration display name.
	 */
	@When("^I configure the payment configuration display name (.+)$")
	public void updatePaymentConfigurationDisplayName(final String displayName) {
		editPaymentConfigurationDialog.enterDisplayName(displayName);
	}

	/**
	 * Fill in payment configuration localized display name and locale.
	 *
	 * @param localizedDisplayName the payment configuration localized display name.
	 * @param locale               the locale of payment configuration localized display name.
	 */
	@When("^I configure the localized display name (.+) for (.+) locale$")
	public void updatePaymentConfigurationLocalizedDisplayName(final String localizedDisplayName, final String locale) {
		editPaymentConfigurationDialog.enterLocalizedDisplayName(localizedDisplayName);
		editPaymentConfigurationDialog.selectLocalizedDisplayNameLocale(locale);
	}

	/**
	 * Update configuration properties fields.
	 *
	 * @param propertiesMap the properties map
	 */
	@When("^I update the payment configuration properties$")
	public void updatePaymentConfigurationProperties(final List<Map<String, String>> propertiesMap) {
		propertiesMap.forEach(properties -> editPaymentConfigurationDialog
				.enterPaymentConfigurationValue(properties.get(PROPERTY_NAME), properties.get(OLD_VALUE), properties.get(NEW_VALUE)));
	}

	/**
	 * Verifies are payment configuration values not interactive.
	 *
	 * @param propertiesMap the properties map
	 */
	@When("^Payment configuration values should not be interactive$")
	public void verifyArePaymentConfigurationValuesNotInteractive(final Map<String, String> propertiesMap) {
		propertiesMap.forEach((key, value) -> editPaymentConfigurationDialog.verifyIsPaymentConfigurationValueNotInteractive(key, value));
	}

	/**
	 * Save payment configuration.
	 */
	@When("^I save the payment configuration$")
	public void savePaymentConfiguration() {
		createPaymentConfigurationDialog.clickSave();
	}

	/**
	 * Verifies is save payment configuration button disabled with given validation message.
	 * @param validationMessage String
	 */
	@Then("^Save payment configuration button is disabled with a validation message (.+)$")
	public void verifyUniqueConfigurationName(final String validationMessage) {
		createPaymentConfigurationDialog.verifyNameValidationMessage(validationMessage);
		createPaymentConfigurationDialog.verifyIsSavePaymentConfigurationButtonDisabled();
	}

	/**
	 * Verifies new payment configuration exists in list.
	 */
	@Then("^the newly created payment configuration exists in the list$")
	public void verifyPaymentConfiguration() {
		paymentConfigurationsResultPane.verifyPaymentConfigurationExist(paymentConfiguration.getConfigurationName());
	}

	/**
	 * Verifies new payment configuration exists in list.
	 */
	@Then("^the payment configuration (.+) exists in the list$")
	public void verifyPaymentConfiguration(final String configurationName) {
		paymentConfigurationsResultPane.verifyPaymentConfigurationExist(configurationName);
	}

	/**
	 * Activates the newly created payment configuration.
	 */
	@When("^I activate the newly created payment configuration$")
	public void activatePaymentConfiguration() {
		paymentConfigurationsResultPane.verifyPaymentConfigurationExist(paymentConfiguration.getConfigurationName());
		paymentConfigurationsResultPane.activatePaymentConfiguration();
	}

	/**
	 * Disable newly created configuration.
	 */
	@When("^I disable the newly created payment configuration$")
	public void disableNewlyCreatedPaymentConfiguration() {
		paymentConfigurationsResultPane.verifyPaymentConfigurationExist(paymentConfiguration.getConfigurationName());
		paymentConfigurationsResultPane.disablePaymentConfiguration();
	}

	/**
	 * Verifies disable denied.
	 */
	@Then("^the disable payment configuration action is denied$")
	public void verifyDisableDenied() {
		paymentConfigurationsResultPane.verifyDisableDenied();
	}

	/**
	 * Disable the selected payment configuration.
	 * @param paymentConfiguration String
	 */
	@When("^I disable the payment configuration (.+)$")
	public void disableGivenPaymentConfiguration(final String paymentConfiguration) {
		paymentConfigurationsResultPane.verifyPaymentConfigurationExist(paymentConfiguration);
		paymentConfigurationsResultPane.clickDisableButton();
	}

	/**
	 * Verifies newly created payment configuration status.
	 * @param status String
	 */
	@Then("^the newly created payment configuration status is (.+)$")
	public void verifyNewlyCreatedPaymentConfigurationStatus(final String status) {
		paymentConfigurationsResultPane.verifyPaymentConfigurationStatus(paymentConfiguration.getConfigurationName(), status);
		if ("Active".equals(status)) {
			verifyActivateButtonDisabled();
		} else if ("Disabled".equals(status)) {
			verifyActivateButtonDisabled();
			verifyDisableButtonDisabled();
		}
	}

	/**
	 * Verifies given payment configuration status.
	 * @param paymentConfiguration String
	 * @param status               String
	 */
	@Then("^the payment configuration (.+) status is (.+)$")
	public void verifyPaymentConfigurationStatus(final String paymentConfiguration, final String status) {
		paymentConfigurationsResultPane.verifyPaymentConfigurationStatus(paymentConfiguration, status);
	}

	/**
	 * Verifies if Activate button is disabled.
	 */
	@Then("^the Activate button is disabled$")
	public void verifyActivateButtonDisabled() {
		paymentConfigurationsResultPane.verifyActivateButtonDisabled();
	}

	/**
	 * Verifies if Disable button is disabled.
	 */
	@Then("^the Disable button is disabled$")
	public void verifyDisableButtonDisabled() {
		paymentConfigurationsResultPane.verifyDisableButtonDisabled();
	}

	@When("^I update the saved payment configuration properties$")
	public void updateSavedPaymentConfigurationProperties(final Map<String, String> propertiesMap) {
		propertiesMap.forEach(this::updateSavedPaymentConfigurationProperty);
	}

	/**
	 * Update the Payment Provider Config Data.
	 * @param configKey String
	 * @param value String
	 */
	@When("^I update the saved payment configuration property (.+) with value (.+)$")
	public void updateSavedPaymentConfigurationProperty(final String configKey, final String value) {
		final String configurationName = paymentConfiguration.getConfigurationName();
		final DBConnector dbConnector = new DBConnector();

		dbConnector.executeUpdateQuery("UPDATE TPAYMENTPROVIDERCONFIGDATA SET TPAYMENTPROVIDERCONFIGDATA.CONFIG_DATA='" + value + "' \n"
				+ "WHERE TPAYMENTPROVIDERCONFIGDATA.CONFIG_KEY='" + configKey + "' \n"
				+ "AND TPAYMENTPROVIDERCONFIGDATA.PAYMENTPROVIDERCONFIG_UID = \n"
				+ "(SELECT TPAYMENTPROVIDERCONFIG.UIDPK FROM TPAYMENTPROVIDERCONFIG WHERE TPAYMENTPROVIDERCONFIG"
				+ ".CONFIGURATION_NAME='" + configurationName + "')");
	}

}
