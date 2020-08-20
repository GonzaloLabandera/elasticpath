/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl;

import io.reactivex.Maybe;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorResolutionStrategy;
import com.elasticpath.service.shoppingcart.validation.impl.ModifierShoppingCartValidatorImpl;

/**
 * Resolves resource identifier to cart descriptor.
 */
public class ShoppingCartModifierResolutionStrategy implements StructuredErrorResolutionStrategy {

	private ShoppingCartRepository shoppingCartRepository;
	private CartOrderRepository cartOrderRepository;

	/**
	 * Transforms structured error resolution into a resource identifier.
	 *
	 * @param message          a structured error message
	 * @param cortexResourceID the cart Id.
	 * @return a resource identifier, or Optional.empty() if none exists.
	 */
	@Override
	public Maybe<ResourceIdentifier> getResourceIdentifier(final StructuredErrorMessage message, final String cortexResourceID) {
		if (StringUtils.equals(message.getMessageId(), ModifierShoppingCartValidatorImpl.MESSAGE_ID)) {

			return shoppingCartRepository.findStoreForCartGuid(cortexResourceID)
					.flatMapMaybe(
							storeCode -> cartOrderRepository.findByCartGuid(cortexResourceID)
									.flatMapMaybe(cartOrder -> Maybe.just(buildCartDescriptorIdentifier(storeCode, cartOrder))));
		}
		return Maybe.empty();
	}

	private CartDescriptorIdentifier buildCartDescriptorIdentifier(final String storeCode, final CartOrder cartOrder) {
		return CartDescriptorIdentifier.builder()
				.withCart(CartIdentifier.builder()
						.withCartId(StringIdentifier.of(cartOrder.getShoppingCartGuid()))
						.withCarts(CartsIdentifier.builder().withScope(StringIdentifier.of(storeCode))
								.build())
						.build())
				.build();
	}

	protected ShoppingCartRepository getShoppingCartRepository() {
		return shoppingCartRepository;
	}

	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	protected CartOrderRepository getCartOrderRepository() {
		return cartOrderRepository;
	}

	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}
}
