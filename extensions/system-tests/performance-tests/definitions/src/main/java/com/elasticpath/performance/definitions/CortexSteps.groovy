/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.definitions

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_PAYMENT_CONFIGURATION_NAME

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.CortexResponse
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Payment
import com.elasticpath.cortexTestObjects.Profile

class CortexSteps {

	@When('^I view default cart$')
	static void viewDefaultCart() {
		client.GET("/carts/mobee/default")
		assert client.response.status == 200
	}

	@And('^I logout$')
	static void logout() {
		client.DELETE("/oauth2/tokens")
				.stopIfFailure()
	}

	@And('^I retrieve default user profile$')
	static void profile() {
		client.GET("/")
				.defaultprofile()
				.stopIfFailure()
		assert client.response.status == 200
	}

	@And('^I retrieve all user addresses$')
	static void addresses() {
		profile()
		client
				.addresses()
				.stopIfFailure()
		assert client.response.status == 200
	}

	@And('^I create default billing Canadian address$')
	static void addCanadianBillingAddress() {
		Profile.createAddress("CA", "", "Vancouver", "", "", "H0H 0H0", "BC",
				"123 Somestreet", "User", "Test")
		assert client.response.status == 201
	}

	@And('^I view all payment methods$')
	static void paymentmethods() {
		profile()
		client.paymentmethods()
				.stopIfFailure()
		assert client.response.status == 200
	}

	@And('^I view default payment instrument$')
	static void defaultPaymentInstrument() {
		Profile.defaultinstrumentselector()
		client
				.chosen()
				.description()
				.stopIfFailure()
	}

	@And('^I view order receipt$')
	static void viewOrderReceipt() {
		profile()
		client.purchases()
				.element()
				.stopIfFailure()
	}

	@And('^I zoom order receipt$')
	static void zoomOrderReceipt() {
		client.GET("/purchases/mobee?zoom=element,element:paymentinstruments:element,element:lineitems:element")
		assert client.response.status == 200
	}

	@And('^I create default saved payment instrument$')
	static def createSavedPaymentInstrumentWithDefaultName() {
		Payment.createSavedPaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
											 Arrays.asList("display-name", DEFAULT_PAYMENT_CONFIGURATION_NAME) as List<String>
		)))
	}

	@And('^I list all shipping options$')
	static void shippingOptionSelector() {
		Order.deliveries()
		client.element()
				.shippingoptioninfo()
				.selector()
				.stopIfFailure()
	}

	@And('^I view cart order$')
	static void getOrder() {
		client.GET("/")
				.defaultcart()
				.order()
		CortexResponse.orderResponse = client.save()
		assert client.response.status == 200
	}

	@And('^I checkout$')
	static void submitPurchaseWithoutFollow() {
		Order.purchaseform()
		client.submitorderaction()
				.stopIfFailure()
	}
}
