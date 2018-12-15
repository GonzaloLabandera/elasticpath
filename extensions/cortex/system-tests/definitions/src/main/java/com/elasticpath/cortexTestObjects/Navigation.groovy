package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Navigation.
 */
class Navigation extends CommonMethods {

	static void getNavigation() {
		client.GET("/")
				.navigations()
				.stopIfFailure()
	}

	static void navigateToCategory(String[] categoryArray) {
		getNavigation()
		openLinkRelWithFieldWithValue("element", "name", categoryArray[0].trim())

		for (int i = 1; i < categoryArray.size(); i++) {
			openLinkRelWithFieldWithValue("child", "name", categoryArray[i].trim())
		}
	}

	static void navigateToCategory(String category) {
		navigateToCategory(category.split(","))
	}

	static boolean isItemsContainElementWithDisplayName(String displayName) {
		def itemExists = false
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				CortexResponse.elementResponse = client.save()
				if (displayName == Item.getItemName()) {
					return itemExists = true
				}
			}
		}

		return itemExists
	}

}