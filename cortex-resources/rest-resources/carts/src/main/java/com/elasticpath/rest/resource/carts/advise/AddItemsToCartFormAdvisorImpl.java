/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.advise;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.AddItemsToCartAdvisor;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.AddItemsToCartAdvisorService;

/**
 * Add items to cart form advisor.
 */
public class AddItemsToCartFormAdvisorImpl implements AddItemsToCartAdvisor.ReadAdvisor {

	private final AddItemsToCartFormIdentifier identifier;
	private final AddItemsToCartAdvisorService addItemsToCartAdvisorService;

	/**
	 * Constructor.
	 *
	 * @param addItemsToCartAdvisorService addItemsToCartAdvisorService
	 * @param identifier  the identifier.
	 */
	@Inject
	public AddItemsToCartFormAdvisorImpl(
			@RequestIdentifier final AddItemsToCartFormIdentifier identifier,
			@ResourceService final AddItemsToCartAdvisorService addItemsToCartAdvisorService) {
		this.identifier = identifier;
		this.addItemsToCartAdvisorService = addItemsToCartAdvisorService;
	}

	@Override
	public Observable<Message> onAdvise() {
		return addItemsToCartAdvisorService.validateEmptyCart(identifier.getCart().getCartId().getValue());
	}
}
