package com.elasticpath.cucumber.definitions;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import com.elasticpath.cortex.dce.LoginSteps;
import com.elasticpath.cortex.dce.payment.PaymentMethodsSteps;
import com.elasticpath.cortexTestObjects.Order;
import com.elasticpath.cortexTestObjects.Payment;
import com.elasticpath.cortexTestObjects.Profile;
import com.elasticpath.selenium.domainobjects.PaymentConfiguration;

/**
 * System Configuration step definitions.
 */
public class StorePaymentDefinition {
	private final PaymentConfiguration paymentConfiguration;

	/**
	 * Constructor.
	 */
	public StorePaymentDefinition(final PaymentConfiguration paymentConfiguration) {
		this.paymentConfiguration = paymentConfiguration;
	}

	@Then("^I can see the newly created payment configuration in Cortex order payment methods for store (.+)$")
	public void verifyPaymentInCortex(final String store) {
		LoginSteps.loginAsPublocUserWithScope(store);
		Order.getPaymentConfigurationWithName(paymentConfiguration.getConfigurationName());
	}

	@Then("^the (?:inactive||unassociated with store) payment configuration is not visible in store (.+)$")
	public void verifyOrderPaymentMethodNotExists(final String store) {
		LoginSteps.loginAsPublocUserWithScope(store);
		Order.paymentmethodsresource();
		PaymentMethodsSteps.verifyPaymentMethodsNotExist(paymentConfiguration.getConfigurationName());
	}

	@Then("^the newly created payment method is not visible in my profile")
	public void verifyProfilePaymentMethodNotExists() {
		Profile.paymentmethods();
		PaymentMethodsSteps.verifyPaymentMethodsNotExist(paymentConfiguration.getConfigurationName());
	}


	@And("^I have created payment instrument with the newly created payment configuration on my profile:$")
	public void createSavedPaymentInstrumentInProfile(final DataTable dataTable) {
		Payment.createSavedPaymentInstrument(paymentConfiguration.getConfigurationName(), dataTable);
	}

	@And("^I have created order payment instrument with the newly created payment configuration$")
	public void createUnsavedOrderPaymentInstrument(final DataTable dataTable) {
		Payment.createUnsavedPaymentInstrument(paymentConfiguration.getConfigurationName(), dataTable);
	}

}
