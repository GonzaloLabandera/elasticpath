/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.AddItemsToCartAdvisorService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Add Items To Cart Validation Service.
 */
@Singleton
@Named("addItemsToCartAdvisorService")
public class AddItemsToCartAdvisorServiceImpl implements AddItemsToCartAdvisorService {

	private final ShoppingCartRepository shoppingCartRepository;

	/**
	 * Constructor.
	 *
	 * @param shoppingCartRepository shoppingCartRepository
	 */
	@Inject
	public AddItemsToCartAdvisorServiceImpl(@Named("shoppingCartRepository") final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	@Override
	public Observable<Message> validateEmptyCart(final String cartGuid) {
		return shoppingCartRepository.getShoppingCart(cartGuid)
				.flatMapObservable(this::isCartEmpty);
	}

	/**
	 * Check if cart is empty. Returns warning message if cart is not empty.
	 *
	 * @param shoppingCart shoppingCart
	 * @return warning message if cart is not empty.
	 */
	protected Observable<Message> isCartEmpty(final ShoppingCart shoppingCart) {
		if (shoppingCart.isEmpty()) {
			return Observable.empty();
		}

		return Observable.just(buildNonEmptyCartMessage(shoppingCart));
	}

	/**
	 * Build the 'cart not empty' warning message.
	 *
	 * @param shoppingCart shoppingCart
	 * @return the warning message.
	 */
	protected Message buildNonEmptyCartMessage(final ShoppingCart shoppingCart) {
		Map<String, String> data = ImmutableMap.of("numberOfItems", String.valueOf(shoppingCart.getNumItems()));
		String message = "Shopping cart already contains '"
				+ shoppingCart.getNumItems()
				+ "' item(s). Please consider clearing your shopping cart before continuing "
				+ "or you may continue and additional item(s) will be added or updated with the existing cart item(s).";
		return Message.builder()
				.withType(StructuredMessageTypes.WARNING)
				.withId("cart.is.not.empty")
				.withDebugMessage(message)
				.withData(data)
				.build();
	}
}
