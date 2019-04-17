package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Wish list.
 */
class WishList extends CommonMethods {


	static void getDefaultWishList() {
		client.GET("/")
				.defaultwishlist()
				.stopIfFailure()
		CortexResponse.wishListResponse = client.save()
	}

	static void resume() {
		client.resume(CortexResponse.wishListResponse)
	}

	static void lineitems() {
		client.lineitems()
				.stopIfFailure()
	}

	static void getWishListLineItems() {
		getDefaultWishList()
		client.lineitems()
				.stopIfFailure()
	}

	static void movetocartform() {
		client.movetocartform()
				.stopIfFailure()
	}

	static void movetocartaction(def itemQty, def configurationFields) {
		client.movetocartaction(
				["quantity"   : itemQty,
				 configuration: configurationFields
				])
				.stopIfFailure()
	}

	static void moveItemToCartWithoutFollow(def itemQty, def configurationFields) {
		movetocartform()
		movetocartaction(itemQty, configurationFields)
	}

	static void moveItemToCart(def itemQty, def configurationFields) {
		movetocartform()
		movetocartaction(itemQty, configurationFields)
		client.follow()
				.stopIfFailure()
	}

	static void verifyWishListItemsBySkuCode(List<String> skuCodeList) {
		CortexResponse.wishListResponse = null
		for(String skuCode : skuCodeList) {
			getDefaultWishList()
			LineItems.verifyLineItemsBySkuCode(skuCode)
		}
	}

	static void verifyWishListItemsBySkuCode(def skuCode) {
		getDefaultWishList()
		LineItems.verifyLineItemsBySkuCode(skuCode)
	}

	static void findWishListElementBySkuCode(def skuCode) {
		verifyWishListItemsBySkuCode(skuCode)
	}


}