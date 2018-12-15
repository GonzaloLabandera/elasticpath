package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Line Item.
 */
class LineItem extends CommonMethods {

	static void availability() {
		client.availability()
	}

	static void item() {
		client.item()
	}

	static int getQuantity() {
		client.resume(CortexResponse.lineItemResponse)
		return client["quantity"]
	}

	static void dependentlineitems() {
		getLineitem()
		client.dependentlineitems()
				.stopIfFailure()
	}

	static void getLineitem(){
		client.resume(CortexResponse.lineItemResponse)
	}

}