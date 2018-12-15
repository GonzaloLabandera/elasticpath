package com.elasticpath.cortexTestObjects

/**
 * Cortex Response.
 */

class CortexResponse {

	static def elementResponse
	static def cartResponse
	static def orderResponse
	static def purchaseResponse
	static def profileResponse
	static def wishListResponse
	static def lineItemResponse


	static void reset() {
		elementResponse = null
		cartResponse = null
		orderResponse = null
		purchaseResponse = null
		profileResponse = null
		wishListResponse = null
		lineItemResponse = null
	}

}