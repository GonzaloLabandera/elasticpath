/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;

/**
 * Add Items To Cart Validation Service.
 */
public interface AddItemsToCartAdvisorService {

	/**
	 * Validate if the cart is empty. Return warning message is cart is not empty.
	 *
	 * @return warning message is cart is not empty
	 */
	Observable<Message> validateEmptyCart();
}
