package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN_DISPLAY_NAME

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Payment.
 */
class Payment extends CommonMethods {

	static void paymentmethodinfo() {
		client.paymentmethodinfo()
				.stopIfFailure()
	}

	static void addToken(String displayName, String token) {
		paymentmethodinfo()
		client.paymenttokenform()
				.createpaymenttokenfororderaction(
						['display-name': displayName,
						 'token'       : token]
				)
				.follow()
				.stopIfFailure()
	}

	static def createInstrumentUsingSelectedCartOrder(){
		addToken(TEST_TOKEN_DISPLAY_NAME, TEST_TOKEN)
	}

}