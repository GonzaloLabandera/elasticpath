package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * FindItemBy.
 */
class FindItemBy extends CommonMethods {

	/**
	 * Searches by product name.
	 * @param keyword
	 */
	static void productName(final String keyword, final int ... pageSize) {
		CortexResponse.elementResponse = null

		if (pageSize.size() == 0) {
			search(keyword, 5)
		} else {
			search(keyword, pageSize[0])
		}

		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				def item = client.save()
				client.definition()
				println("product name: " + client["display-name"])
				if (client["display-name"] == keyword) {
					CortexResponse.elementResponse = item
					return true
				}
			}
		}

		assertThat(CortexResponse.elementResponse != null)
				.as("Unable to find " + keyword)
				.isTrue()

		client.resume(CortexResponse.elementResponse)
	}

	/**
	 * Searches for offer by product name and open the offer.
	 * @param keyword
	 */
	static void offerByName(final String keyword, final int ... pageSize) {
		CortexResponse.elementResponse = null
		def offer = null
		if (pageSize.size() == 0) {
			searchForOffer(keyword, 5)
		} else {
			searchForOffer(keyword, pageSize[0])
		}
		client.follow()
				.stopIfFailure()
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				offer = client.save()
				client.definition()
				println("product name: " + client["display-name"])
				if (client["display-name"] == keyword) {
					CortexResponse.elementResponse = offer
					return true
				}
			}
		}

		assertThat(CortexResponse.elementResponse != null)
				.as("Unable to find " + keyword)
				.isTrue()

		client.resume(offer)
	}

	/**
	 * Searches for offer by product code and open the offer.
	 * @param skuCode
	 */
	static void offerBySkuCode(final String skuCode, final int ... pageSize) {
		CortexResponse.elementResponse = null
		def offer = null
		if (pageSize.size() == 0) {
			searchForOffer(skuCode, 5)
		} else {
			searchForOffer(skuCode, pageSize[0])
		}
		client.follow()
				.stopIfFailure()
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				offer = client.save()
				client.code()
				println("product code: " + client["code"])
				if (client["code"] == skuCode) {
					CortexResponse.elementResponse = offer
					return true
				}
			}
		}

		assertThat(CortexResponse.elementResponse != null)
				.as("Unable to find" + skuCode)
				.isTrue()

		client.resume(offer)
	}

	/**
	 * Lookup by sku code.
	 * @param skuCode
	 */
	static skuCode(final String skuCode) {
		CortexResponse.elementResponse = null
		lookup(skuCode)

		def item = client.save()
		client.code()
		println("sku code: " + client["code"])
		if (client["code"] == skuCode) {
			CortexResponse.elementResponse = item
		}

		assertThat(CortexResponse.elementResponse != null)
				.as("Unable to find " + skuCode)
				.isTrue()

		client.resume(CortexResponse.elementResponse)
	}

	static search(keyword, pageSize) {
		client.GET("/")
				.searches()
				.keywordsearchform()
				.itemkeywordsearchaction(
				['keywords' : keyword,
				 'page-size': pageSize]
		)
				.follow()
				.stopIfFailure()
	}

	static lookup(def skuCode) {
		lookupWithoutFollow(skuCode)
		client.follow()
				.stopIfFailure()
	}

	static lookupWithoutFollow(def skuCode) {
		client.GET("/")
				.lookups()
				.itemlookupform()
				.itemlookupaction([code: skuCode])
				.stopIfFailure()
	}

	static batch(def skuCodes) {
		client.GET("/")
				.lookups()
				.batchitemslookupform()
				.batchitemslookupaction([codes: skuCodes])
				.follow()
				.stopIfFailure()
	}

	static void search() {
		client.GET("/")
				.searches()
				.stopIfFailure()
	}

}