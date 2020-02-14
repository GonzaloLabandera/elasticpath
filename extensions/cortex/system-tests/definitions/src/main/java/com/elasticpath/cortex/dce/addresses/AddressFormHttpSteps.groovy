package com.elasticpath.cortex.dce.addresses

import static org.assertj.core.api.Assertions.assertThat

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.DataTable
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Profile

class AddressFormHttpSteps {

	@When('^I get address form$')
	static void getAddress() {
		Profile.addressform()
	}

	@Then('^form should have following values$')
	static void verifyFormContainsData(DataTable dataTable) {
		def mapList = dataTable.asMaps(String, String)
		mapList.each {
			assertThat(client[it.key][it.value])
					.as(it.key + ", " + it.value + " not found")
					.isEqualTo("")
		}
	}

	@Then('^I see the following fields on the top-level address form:$')
	static void verifyFormContainsTopLevelFields(DataTable dataTable) {
		def fieldsList = dataTable.asList(String)
		fieldsList.each {
			assertThat(client[it])
					.as(it + " not found")
					.isEqualTo("")
		}
	}
}
