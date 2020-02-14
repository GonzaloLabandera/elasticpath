/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil.createShipmentDetailsId;

import io.reactivex.Maybe;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionInfoIdentifier;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorResolutionStrategy;
import com.elasticpath.service.shoppingcart.validation.impl.ShippingOptionShoppingCartValidatorImpl;

/**
 * Translates a structured error message into a identifier to shipping option.
 */
public class ShippingOptionResolutionStrategy implements StructuredErrorResolutionStrategy {

	private ShoppingCartRepository shoppingCartRepository;
	private CartOrderRepository cartOrderRepository;

	/**
	 * Transforms structured error resolution into a resource identifier.
	 *
	 * @param message a structured error message
	 * @param cortexResourceID the cart Id
	 * @return a resource identifier, or Optional.empty() if none exists.
	 */
	@Override
	public Maybe<ResourceIdentifier> getResourceIdentifier(final StructuredErrorMessage message, final String cortexResourceID) {
		if (ShippingOptionShoppingCartValidatorImpl.MESSAGE_IDS.contains(message.getMessageId())) {
			return shoppingCartRepository.findStoreForCartGuid(cortexResourceID)
					.flatMapMaybe(storeCode -> cartOrderRepository.findByCartGuid(cortexResourceID)
							.flatMapMaybe(cartOrder -> buildShippingOptionInfoIdentifier(storeCode, cartOrder)));
		}
		return Maybe.empty();
	}

	private Maybe<ShippingOptionInfoIdentifier> buildShippingOptionInfoIdentifier(final String storeCode, final CartOrder cartOrder) {
		final ShippingOptionInfoIdentifier shippingOptionInfoIdentifier =
				ShippingOptionInfoIdentifier.builder()
						.withShipmentDetailsId(CompositeIdentifier.of(createShipmentDetailsId(cartOrder.getGuid(), ShipmentDetailsConstants
								.SHIPMENT_TYPE))).
						withScope(StringIdentifier.of(storeCode)).
						build();
		return Maybe.just(shippingOptionInfoIdentifier);
	}

	protected ShoppingCartRepository getShoppingCartRepository() {
		return shoppingCartRepository;
	}

	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	protected  CartOrderRepository getCartOrderRepository() {
		return cartOrderRepository;
	}

	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}
}
