package com.elasticpath.cortex.dce.httpCaching

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.When
import org.assertj.core.api.SoftAssertions

class HttpCachingSteps {

	@When('^I should see the Cache-Control header in the response with a valid max age and private directive$')
	static void verifyResponseContainsCacheControlHeader() {
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

		SoftAssertions softly = new SoftAssertions()

		softly.assertThat(shareable)
				.as("Cache-Control private directive not found")
				.isNotNull()

		softly.assertThat(maxAge)
				.as("Cache-Control max-age directive not found")
				.isNotNull()

		softly.assertAll()

		assertThat(maxAge.getValue().isInteger())
				.as("Cache-Control max-age value was not an integer")
				.isTrue()

		assertThat(maxAge.getValue() as Integer)
				.as("Cache-Control max-age value was not greater than 0")
				.isPositive()
	}

	@When('^I should see an ETag header in the response$')
	static void verifyResponseContainETag() {
		def eTag = client.response.getFirstHeader("ETag")

		assertThat(eTag)
				.as("ETag not found")
				.isNotNull()

		assertThat(eTag.getValue())
				.as("ETag not weak: $eTag")
				.startsWith("W/")
	}
}
