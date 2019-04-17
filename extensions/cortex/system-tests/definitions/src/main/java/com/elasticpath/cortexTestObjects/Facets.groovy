package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Facets.
 */
class Facets extends CommonMethods {

	static String getDisplayName() {
		return client["display-name"]
	}

	static void selectFacetChoice(String choice, String status) {
		boolean choiceExists = false;

		def facetSelector = client.save()

		client.body.links.find {
			if (it.rel == status) {
				client.GET(it.href)
				def choiceSelector = client.save()
				client.description()
				if (client["value"] == choice) {
					client.resume(choiceSelector)
					client.selectaction()
							.follow()
					 choiceExists = true
				}
			}
		}

		assertThat(choiceExists)
				.as("Unable to find choice - $choice")
				.isTrue()
	}

	static def getFacetResultDisplayNameList() {
		def actualList = []
		def links = client.body.links
		if (client.body._next != null) {
			links.addAll(client.body._next[0].links)
		}
		links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
						.definition()
				actualList.add(client["display-name"])
			}
		}
		return actualList
	}
}