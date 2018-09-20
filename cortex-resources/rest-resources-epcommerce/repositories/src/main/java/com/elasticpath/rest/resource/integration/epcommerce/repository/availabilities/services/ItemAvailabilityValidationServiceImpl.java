/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.services;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.availabilities.AvailabilityForCartLineItemIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.carts.LineItemsIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.ItemAvailabilityValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Validation service for order item availability.
 */
@Component
public class ItemAvailabilityValidationServiceImpl implements ItemAvailabilityValidationService {

	private StoreProductRepository storeProductRepository;
	private ShoppingCartRepository shoppingCartRepository;

	@Override
	public Observable<LinkedMessage<AvailabilityForCartLineItemIdentifier>> validateItemUnavailable(final OrderIdentifier orderIdentifier) {
		final String scope = orderIdentifier.getScope().getValue();

		return shoppingCartRepository.getDefaultShoppingCart()
				.flatMapObservable(shoppingCart -> getUnavailableItemMessages(scope, shoppingCart.getGuid(), shoppingCart.getCartItems()));
	}

	/**
	 * Get the unavailable items messages from the shopping items.
	 *
	 * @param storeCode     storeCode
	 * @param cartId        cartId
	 * @param shoppingItems shoppingItems
	 * @return the unavailable items messages
	 */
	protected Observable<LinkedMessage<AvailabilityForCartLineItemIdentifier>> getUnavailableItemMessages(
			final String storeCode, final String cartId, final List<ShoppingItem> shoppingItems) {
		return Observable.fromIterable(shoppingItems)
				.flatMapMaybe(shoppingItem -> getStoreProduct(storeCode, shoppingItem.getSkuGuid())
						.flatMapMaybe(storeProduct -> getLinkedMessage(storeCode, cartId, shoppingItem, storeProduct)));
	}

	/**
	 * Get the StoreProduct given the skuGuid.
	 *
	 * @param storeCode storeCode
	 * @param skuGuid   skuGuid
	 * @return the store product.
	 */
	protected Single<StoreProduct> getStoreProduct(final String storeCode, final String skuGuid) {
		return storeProductRepository.findDisplayableStoreProductWithAttributesBySkuGuid(storeCode, skuGuid);
	}

	/**
	 * Get the LinkedMessage for the unavailable shopping item.
	 *
	 * @param scope        scope
	 * @param cartId       cartId
	 * @param shoppingItem shoppingItem
	 * @param storeProduct storeProduct
	 * @return the LinkedMessage
	 */
	protected Maybe<LinkedMessage<AvailabilityForCartLineItemIdentifier>> getLinkedMessage(
			final String scope, final String cartId, final ShoppingItem shoppingItem, final StoreProduct storeProduct) {

		AvailabilityForCartLineItemIdentifier availabilityIdentifier = buildAvailabilityIdentifier(scope, cartId, shoppingItem);
		String skuCode = storeProduct.getSkuByGuid(shoppingItem.getSkuGuid()).getSkuCode();
		Map<String, String> data = ImmutableMap.of("item-code", skuCode);

		if (!storeProduct.isSkuAvailable(skuCode)) {
			String debugMessage = "Item '" + skuCode + "' is not available for purchase.";
			return Maybe.just(buildLinkedMessage(availabilityIdentifier, data, StructuredErrorMessageIdConstants.CART_ITEM_NOT_AVAILABLE,
					debugMessage));
		}

		return Maybe.empty();
	}

	/**
	 * Get the AvailabilityForCartLineItemIdentifier.
	 *
	 * @param scope        scope
	 * @param cartId       cartId
	 * @param shoppingItem shoppingItem
	 * @return the AvailabilityForCartLineItemIdentifier
	 */
	protected AvailabilityForCartLineItemIdentifier buildAvailabilityIdentifier(
			final String scope, final String cartId, final ShoppingItem shoppingItem) {
		LineItemsIdentifier lineItemsIdentifier = LineItemsIdentifier.builder()
				.withCart(CartIdentifier.builder()
						.withScope(StringIdentifier.of(scope))
						.withCartId(StringIdentifier.of(cartId))
						.build())
				.build();

		LineItemIdentifier lineItemIdentifier = LineItemIdentifier.builder()
				.withLineItemId(StringIdentifier.of(shoppingItem.getGuid()))
				.withLineItems(lineItemsIdentifier)
				.build();

		return AvailabilityForCartLineItemIdentifier.builder()
				.withLineItem(lineItemIdentifier)
				.build();
	}

	/**
	 * Build the LinkedMessage from the given information.
	 *
	 * @param availabilityIdentifier availabilityIdentifier
	 * @param data                   data
	 * @param messageId              messageId
	 * @param debugMessage           debugMessage
	 * @return the LinkedMessage
	 */
	protected LinkedMessage<AvailabilityForCartLineItemIdentifier> buildLinkedMessage(
			final AvailabilityForCartLineItemIdentifier availabilityIdentifier,
			final Map<String, String> data,
			final String messageId,
			final String debugMessage) {

		return LinkedMessage.<AvailabilityForCartLineItemIdentifier>builder()
				.withType(StructuredMessageTypes.ERROR)
				.withId(messageId)
				.withDebugMessage(debugMessage)
				.withData(data)
				.withLinkedIdentifier(availabilityIdentifier)
				.build();
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}
}
