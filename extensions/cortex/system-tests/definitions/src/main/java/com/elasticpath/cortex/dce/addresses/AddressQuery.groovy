/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortex.dce.addresses

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.ELEMENT_LINK
import static com.elasticpath.cortex.dce.SharedConstants.TEST_EMAIL_VALUE
import static com.elasticpath.cortex.dce.addresses.AddressConstants.*
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Payment
import com.elasticpath.cortexTestObjects.Profile

class AddressQuery {

	@When('^I fill in billing address needinfo$')
	static void setBillingAddress() {
		Profile.createUniqueAddress()
	}

	@And('^I fill in payment methods needinfo$')
	static void setPaymentMethod() {
		Payment.createUnsavedPaymentInstrumentWithDefaultName()
	}

	@And('^I fill in email needinfo$')
	static void setEmail() {
		Profile.addEmailWithoutFollow(TEST_EMAIL_VALUE)
	}

	@And('^I create (?:a|another) unique address')
	static void setUniqueAddress() {
		Profile.createUniqueAddress()
	}

	@And('^I should see (.+) element on addresses$')
	static void verifyAddressContainsElement(int numberOfAddresses) {

		Profile.addresses()

		def elements = client.body.links.findAll { link ->
			link.rel == ELEMENT_LINK
		}

		assertThat(elements)
				.size()
				.as("Number of elements is not as expected")
				.isEqualTo(numberOfAddresses)
	}

	@And('^address element (.+) is identical to the public shopper\'s address$')
	static void verifyAddressElementIsIdenticalTo(int index) {

		Profile.addresses()

		def elements = client.body.links.findAll { link ->
			link.rel == ELEMENT_LINK
		}

		def element = elements[index - 1]
		client.GET(element.href)

		assertThat(client.body.address.'country-name')
				.as("Country name is not as expected")
				.isEqualTo(DEFAULT_COUNTRY_NAME)
		assertThat(client.body.address.'locality')
				.as("Locality is not as expected")
				.isEqualTo(DEFAULT_LOCALITY)
		assertThat(client.body.address.'postal-code')
				.as("Postal code is not as expected")
				.isEqualTo(DEFAULT_POSTAL_CODE)
		assertThat(client.body.address.'region')
				.as("Region is not as expected")
				.isEqualTo(DEFAULT_POSTAL_REGION)
		assertThat(client.body.address.'street-address')
				.as("Street address is not as expected")
				.contains(DEFAULT_STREE_ADDRESS_SNIPPET)
		assertThat(client.body.name.'family-name')
				.as("Family name is not as expected")
				.isEqualTo(DEFAULT_FAMILY_NAME)
		assertThat(client.body.name.'given-name')
				.as("Given name is not as expected")
				.isEqualTo(DEFAULT_GIVEN_NAME)
	}
}
