package com.elasticpath.cortex.dce.datapolicies

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.SharedConstants
import com.elasticpath.cortexTestObjects.Profile

class DataPolicySteps {

	/**
	 * Retrieve list of data policies.
	 * @return list
	 */
	static retrieveDataPolicyList() {
		def actualDataPolicyList = []
		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.uri)
				actualDataPolicyList.add(client.body.'policy-name')
			}
		}
		return actualDataPolicyList
	}

	@Given('^the following data policies are assigned to data policy segment (.+)$')
	static void assignDataPoliciesStatement(String value, DataTable dataPolicyListTable) {
		//	Non implementation Given statement.
	}

	@Given('^the following data policies with segment (.+) are in (?:Disabled|Draft) state$')
	static void verifyDataPoliciesDraftState(String segment, DataTable dataPolicyListTable) {
		//	Non implementation Given statement.
	}

	@When('^I access the data policies$')
	@Then('^I can follow a link to data policies')
	static void getDataPolicies() {
		Profile.datapolicies()
	}

	@When('^I access the data policies resource from a scope with no data polices enabled')
	static void getDataPoliciesWithScope() {
		def scope = SharedConstants.TOKEN_SCOPE
		client.GET("datapolicies/$scope")
	}

	@Then('^the following expected data policies are visible in my profile$')
	static void verifyProfileDataPolicies(DataTable dataPolicyListTable) {
		def dataPolicyList = dataPolicyListTable.asList(String)
		assertThat(retrieveDataPolicyList())
				.as("Data policy list order or size is not as expected")
				.containsExactlyElementsOf(dataPolicyList)
	}

	@Then('^the following list of data policies are not visible$')
	static void verifyDataPoliciesNotVisible(DataTable dataPolicyListTable) {
		def dataPolicyList = dataPolicyListTable.asList(String)
		assertThat(retrieveDataPolicyList())
				.doesNotContainAnyElementsOf(dataPolicyList)
	}

	@Then('^I should not see any data policies$')
	static void verifyDataPoliciesNotDisplayed() {
		boolean containsDataPolicies = false
		client.body.'links'.each { link ->
			if (link.rel == "element") {
				containsDataPolicies = true
			}
		}
		assertThat(client.body.'links'.size())
				.as("Number of links is not as expected")
				.isEqualTo(1)

		assertThat(containsDataPolicies)
				.as("Resource should not contain data policies")
				.isFalse()
	}

	@Then('^I can (?:access|see) (?:a|the) (?:data policy|data policies) with the following (?:field|fields):$')
	static void verifyDataPoliciesAccessible(DataTable dataPolicyTable) {
		def dataPoliciesMap = dataPolicyTable.asMap(String, String)
		verifyDataPolicies(dataPoliciesMap)
	}

	static void verifyDataPolicies(Map<String, String> dataPoliciesMap){
		def policyFlag = "data-policy-consent"
		def policyKey = "policy-reference-key"
		def policyName = "policy-name"

		def dataPolicy = client.body

		assertThat(dataPolicy[policyFlag])
				.as("Policy flag is not as expected.")
				.isEqualTo(dataPoliciesMap.get(policyFlag))

		assertThat(dataPolicy[policyKey].toString())
				.as("Policy key is not as expected.")
				.isEqualTo(dataPoliciesMap.get(policyKey))

		assertThat(dataPolicy[policyName].toString())
				.as("Policy name is not as expected.")
				.isEqualTo(dataPoliciesMap.get(policyName))
	}

	@When('^I attempt to access the data policy after removing the (.*) header$')
	static void getDataPoliciesNoHeader(String headerName) {
		Map<String, String> headers = (HashMap) client.getHeaders()
		headers.remove(headerName)
		client.GET(client.body.self.href)
	}

	@When('^I remove the (.*) header$')
	static void deleteHeader(String headerName) {
		Map<String, String> headers = (HashMap) client.getHeaders()
		headers.remove(headerName)
	}

	@When('^I access the data policy form$')
	static void getDataPolicyForm() {
		Profile.datapolicyconsentform()
	}

	@When('^I post the following fields to the data policy form:$')
	static void postToDataPolicyForm(DataTable dataPolicyTable) {
		def dataPoliciesMap = dataPolicyTable.asMap(String, String)
		client.POST(client.body.self.href, dataPoliciesMap)
				.stopIfFailure()
	}

	@When('^I select the data policy (.*)$')
	static void selectDataPolicy(String policyName) {
		Profile.selectDataPolicy(policyName)
	}
}
