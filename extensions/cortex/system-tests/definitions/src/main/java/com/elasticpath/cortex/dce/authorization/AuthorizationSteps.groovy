package com.elasticpath.cortex.dce.authorization

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then

class AuthorizationSteps {

	String CART_LINE_ITEM_PRICE_URI

	@Then('^capture the uri of the registered shopper\'s cart line item price$')
	void captureCartLineItemPriceURI() {
		client.GET("/")
				.defaultcart()
				.lineitems()
				.element()
				.price()
				.stopIfFailure()
		CART_LINE_ITEM_PRICE_URI = client.body.self.uri
	}

	@Then('^I attempt to view another shopper\'s cart line item price$')
	void viewCartLineItemPrice() {
		client.GET(CART_LINE_ITEM_PRICE_URI)
				.stopIfFailure()
	}

	@Then('^I am not able to view another shopper\'s cart line item price$')
	static void attemptToViewCartLineItemPrice() {
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(403)
		client.follow()
	}
}
