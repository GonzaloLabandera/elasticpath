package com.elasticpath.cortex.dce.orders

import static OrderConstants.BILLING_ADDRESS_LINK
import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Profile

class BillingAddressSteps {

	static def orderBillingAddressUri
	static def defaultBillingAddressUri

	@And('^Shopper gets the default billing address$')
	static void getDefaultBillingAddress() {
		Profile.defaultBillingAddress()
		defaultBillingAddressUri = client.body.self.uri
	}

	@And('^the shoppers order does not have a billing address applied$')
	static void verifyOrderHasNoBillingAddress() {
		Order.billingaddressinfo()
		assertLinkDoesNotExist(client, BILLING_ADDRESS_LINK)
	}

	@When('^I create a default billing address on the profile$')
	static void createDefaultBillingAddress() {
		Profile.createUniqueAddress()
		Profile.defaultBillingAddress()
		defaultBillingAddressUri = client.body.self.uri
	}

	@When('^I retrieve the shoppers billing address info on the order$')
	static void getOrderBillingAddress() {
		Order.billingAddress()
		orderBillingAddressUri = client.body.self.uri
	}

	@Then('^the default billing address is automatically applied to the order$')
	static void verifyDefaultBillingAddressIsOnOrder() {
		assertThat(orderBillingAddressUri)
				.as("Billing address is not as expected")
				.isEqualTo(defaultBillingAddressUri)
	}

	@When('^I retrieve the order$')
	static void getOrder() {
		Order.getOrder()
	}

	@And('^billing address is selected$')
	static void verifyBillingAddressSelected() {
		Profile.createUniqueAddress()
	}

	@Then('^I use the selectaction$')
	static void clickSelectactionLink() {
		client.selectaction()
				.stopIfFailure()
	}

}
