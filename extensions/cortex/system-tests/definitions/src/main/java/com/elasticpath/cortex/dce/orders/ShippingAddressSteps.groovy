package com.elasticpath.cortex.dce.orders

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static OrderConstants.DESTINATION_LINK
import static OrderConstants.SHIPPING_PRODUCT
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Profile

class ShippingAddressSteps {

	def static orderShippingAddressUri
	def static defaultShippingAddressUri

	@And('^Shopper gets the default shipping address$')
	static void getDefaultShippingAddress() {
		Profile.defaultShippingAddress()
		defaultShippingAddressUri = client.body.self.uri
	}

	@And('^the shoppers order does not have a shipping address applied$')
	static void verifyOrderHasNoShippingAddress() {
		CommonMethods.searchAndAddProductToCart(SHIPPING_PRODUCT)
		Order.destinationinfo()
		assertLinkDoesNotExist(client, DESTINATION_LINK)
	}

	@When('^I create a default shipping address on the profile$')
	static void createDefaultShippingAddress() {
		Profile.createUniqueAddress()
		Profile.defaultShippingAddress()
		defaultShippingAddressUri = client.body.self.uri
	}

	@When('^I retrieve the shoppers shipping address info on the order$')
	static void getOrderShippingAddress() {
		CommonMethods.searchAndAddProductToCart(SHIPPING_PRODUCT)
		Order.destination()
		orderShippingAddressUri = client.body.self.uri
	}

	@Then('^the default shipping address is automatically applied to the order$')
	static void verifyDefaultAddressOnOrder() {
		assertThat(defaultShippingAddressUri)
				.as("Shipping address was not as expected")
				.isEqualTo(orderShippingAddressUri)
	}
}
