/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cortex.dce.httpCaching

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.groovy.EN
import cucumber.api.groovy.Hooks

this.metaClass.mixin(Hooks)
this.metaClass.mixin(EN)

When(~/^I should see the Cache-Control header in the response with a valid max age and private directive$/) { ->
	def headerIterator = client.response.headerIterator('Cache-Control')

	assertThat(headerIterator.hasNext())
			.as("Cache-Control header not found")
			.isTrue()

	def maxAge
	def shareable

	while (headerIterator.hasNext() && (maxAge == null || shareable == null)) {
		def headerElements = headerIterator.nextHeader().getElements()
		if (maxAge == null) {
			maxAge = headerElements.find {
				it.getName() == 'max-age'
			}
		}
		if (shareable == null) {
			shareable = headerElements.find {
				it.getName() == 'private'
			}
		}
	}

	assertThat(shareable)
			.as("private directive not found")
			.isNotNull()

	assertThat(maxAge)
			.as("max-age directive not found")
			.isNotNull()

	assertThat(maxAge.getValue().isInteger())
			.as("max-age value was not an integer")
			.isTrue()

	assertThat(maxAge.getValue() as Integer)
			.as("max-age value was not greater than 0")
			.isPositive()
}

When(~/^I should see an ETag header in the response$/) { ->
	def eTag = client.response.getFirstHeader("ETag")

	assertThat(eTag)
			.as("ETag not found")
			.isNotNull()

	assertThat(eTag.getValue())
			.as("ETag not weak: $eTag")
			.startsWith("W/")
}