/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cortex.dce.datapolicies

import static org.assertj.core.api.Assertions.assertThat

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client

import cucumber.api.DataTable
import cucumber.api.groovy.EN
import cucumber.api.groovy.Hooks

import com.elasticpath.cortex.dce.SharedConstants

this.metaClass.mixin(Hooks)
this.metaClass.mixin(EN)

When(~'^I access the data policies resource from root') { ->
	client.GET("/")
			.'data-policies'()
			.stopIfFailure()
}

When(~'^I access the data policies resource from a scope with no data polices enabled') { ->
	def scope = SharedConstants.TOKEN_SCOPE
	client.GET("datapolicies/$scope")
}

Then(~'^I can follow a link to data policies') { ->
	client.'data-policies'()
			.stopIfFailure()
}

Then(~'I should see a list of data policies with( at least | )(.*) data (?:policies|policy)$') { String atLeast, int numberOfPolicies ->
	def count = 0

	client.body.'links'.each { link ->
		if (link.rel == "element") {
			count++
			assertThat(link.type)
					.as("Data policy type is incorrect.")
					.isEqualTo("datapolicies.data-policy")
		}
	}

	if (atLeast.length() > 1) {
		assertThat(count >= numberOfPolicies)
				.as("Number of data policies is not as expected.")
				.isTrue()
	} else {
		assertThat(count)
				.as("Number of data policies is not as expected.")
				.isEqualTo(numberOfPolicies)
	}
}

Then(~'I should not see any data policies$') { ->
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

Then(~'^I can (?:access|see) (?:a|the) (?:data policy|data policies) with the following (?:field|fields):') { DataTable dataPolicyTable ->
	def policyFlag = "data-policy-consent"
	def policyKey = "policy-reference-key"
	def policyName = "policy-name"

	def dataPoliciesMap = dataPolicyTable.asMap(String, String)
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

When(~/^I attempt to access the data policy after removing the (.*) header/) { String headerName ->
	Map<String, String> headers = (HashMap) client.getHeaders()
	headers.remove(headerName)
	client.GET(client.body.self.href)
}

When(~/^I remove the (.*) header$/) { String headerName ->
	Map<String, String> headers = (HashMap) client.getHeaders()
	headers.remove(headerName)
}

When(~/^I access the data policy form$/) { ->
	client.datapolicyconsentform()
			.stopIfFailure()
}

When(~/^I post the following fields to the data policy form:$/) { DataTable dataPolicyTable ->
	def dataPoliciesMap = dataPolicyTable.asMap(String, String)
	client.POST(client.body.self.href, dataPoliciesMap)
			.stopIfFailure()
}

When(~/^I select the data policy named (.*)$/) { String policyName ->
	def self = client.body.self.href
	def target = ''
	client.body.links.findAll { link ->
		client.GET(link.href)
				.stopIfFailure()
		if (client.body.'policy-name' == policyName) {
			target = link.href
		}
		client.GET(self)
				.stopIfFailure()
	}
	client.GET(target)
			.stopIfFailure()
}