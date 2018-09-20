package com.elasticpath.cortex.dce.addresses

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

class AddressConstants {

	def static final DEFAULT_COUNTRY_NAME = "CA"

	def static final DEFAULT_LOCALITY = "Vancouver"

	def static final DEFAULT_POSTAL_CODE = "V7V7V7"

	def static final DEFAULT_POSTAL_REGION = "BC"

	def static final DEFAULT_STREE_ADDRESS_SNIPPET = "random street"

	def static final DEFAULT_FAMILY_NAME = "itest"

	def static final DEFAULT_GIVEN_NAME = "generated"

	def static final ADDRESS_REL = "addresses"

	def static final ADDRESS_LINK_TYPE = "elasticpath.addresses.address"

	public static void goToAddressForm() {
		client.GET("/")
				.defaultprofile()
				.addresses()
				.addressform()
				.stopIfFailure()
	}

	public static void goToAddress() {
		client.GET("/")
				.defaultprofile()
				.addresses()
				.stopIfFailure()
	}
}
