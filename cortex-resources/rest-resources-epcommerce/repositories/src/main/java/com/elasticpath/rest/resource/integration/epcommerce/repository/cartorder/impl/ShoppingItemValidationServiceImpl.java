/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.Availability;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.shoppingcart.ShoppingCartMessageIds;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingItemValidationService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Shopping Item Validation Service.
 */
@Singleton
@Named("shoppingItemValidationService")
public class ShoppingItemValidationServiceImpl implements ShoppingItemValidationService {

	private final ProductSkuRepository productSkuRepository;
	private final PriceRepository priceRepository;
	private final StoreProductRepository storeProductRepository;

	/**
	 * Constructor.
	 *
	 * @param productSkuRepository   the product sku repository
	 * @param priceRepository        the price repository
	 * @param storeProductRepository the store product repository
	 */
	@Inject
	public ShoppingItemValidationServiceImpl(
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository,
			@Named("priceRepository") final PriceRepository priceRepository,
			@Named("storeProductRepository") final StoreProductRepository storeProductRepository) {
		this.productSkuRepository = productSkuRepository;
		this.priceRepository = priceRepository;
		this.storeProductRepository = storeProductRepository;
	}

	@Override
	public Observable<Message> validateItemPurchasable(final String scope, final String itemId) {
		return productSkuRepository.getProductSkuWithAttributesByCodeAsSingle(itemId)
				.flatMapObservable(productSku -> validateItemPurchasable(scope, productSku));
	}

	@Override
	public Observable<Message> validateItemPurchasable(final String scope, final ProductSku productSku) {
		return getStoreProduct(scope, productSku.getProduct())
				.flatMap(storeProduct -> isPurchasable(productSku, scope, storeProduct))
				.flatMapObservable(isPurchasable -> getItemNotPurchasableErrorMessages(productSku, isPurchasable));
	}

	/**
	 * Checks if the item is purchasable.
	 *
	 * @param productSku   the product sku
	 * @param storeCode    store code
	 * @param storeProduct store product
	 * @return purchasable status
	 */
	private Single<Boolean> isPurchasable(final ProductSku productSku, final String storeCode, final StoreProduct storeProduct) {
		boolean skuAvailable = !Objects.equals(storeProduct.getSkuAvailability(productSku.getSkuCode()), Availability.NOT_AVAILABLE);
		if (skuAvailable) {
			boolean isPurchasable = storeProduct.isSkuPurchasable(productSku.getSkuCode()) && !storeProduct.isNotSoldSeparately();
			if (isPurchasable) {
				return priceRepository.priceExists(storeCode, productSku.getSkuCode());
			}
		}
		return Single.just(false);
	}

	/**
	 * Construct the structured message.
	 *
	 * @param productSku    the product sku
	 * @param isPurchasable the purchasable status
	 * @return structured message
	 */
	private Observable<Message> getItemNotPurchasableErrorMessages(final ProductSku productSku, final Boolean isPurchasable) {
		if (!isPurchasable) {
			final Map<String, String> data = ImmutableMap.of("item-code", productSku.getSkuCode());
			Message structuredError = Message.builder()
					.withType(StructuredMessageTypes.ERROR)
					.withId(ShoppingCartMessageIds.ITEM_NOT_AVAILABLE)
					.withDebugMessage("Item '" + productSku.getSkuCode()
							+ "' is not available for purchase.")
					.withData(data)
					.build();
			return Observable.just(structuredError);
		}
		return Observable.empty();
	}

	/**
	 * Get the store product.
	 *
	 * @param storeCode store code
	 * @param product   product
	 * @return store product
	 */
	protected Single<StoreProduct> getStoreProduct(final String storeCode, final Product product) {
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuidAsSingle(storeCode, product.getGuid());
	}

	@Override
	public Completable validateQuantity(final LineItemEntity lineItemEntity) {
		return validateQuantity(lineItemEntity, 1);
	}

	@Override
	public Completable validateQuantity(final LineItemEntity lineItemEntity, final int minimumQuantity) {
		Integer quantity = lineItemEntity.getQuantity();
		if (quantity == null) {
			return this.getInvalidQuantityErrorMessage();
		} else if (quantity < minimumQuantity) {
			return this.getInvalidQuantityErrorMessage(quantity, minimumQuantity);
		}
		return Completable.complete();
	}

	/**
	 * Get structured error message.
	 *
	 * @return the message
	 */
	private Completable getInvalidQuantityErrorMessage() {
		Map<String, String> data = ImmutableMap.of("field-name", "quantity");
		String message = "'quantity' value must be an integer.";
		Message structuredError = Message.builder()
				.withType(StructuredMessageTypes.ERROR)
				.withId(StructuredErrorMessageIdConstants.INVALID_INTEGER_FIELD)
				.withDebugMessage(message)
				.withData(data)
				.build();
		return Completable.error(ResourceOperationFailure.badRequestBody(message, Collections.singletonList(structuredError)));
	}

	/**
	 * Get structured error message.
	 *
	 * @param quantity the quantity
	 * @return the message
	 */
	private Completable getInvalidQuantityErrorMessage(final Integer quantity, final int minimumQuantity) {
		Map<String, String> data = ImmutableMap.of("field-name", "quantity",
				"min-value", String.valueOf(minimumQuantity),
				"invalid-value", String.valueOf(quantity));
		String message = "'quantity' value '" + quantity + "' must be greater than or equal to '" + minimumQuantity + "'.";
		Message structuredError = Message.builder()
				.withType(StructuredMessageTypes.ERROR)
				.withId(StructuredErrorMessageIdConstants.INVALID_MINIMUM_VALUE_FIELD)
				.withDebugMessage(message)
				.withData(data)
				.build();
		return Completable.error(ResourceOperationFailure.badRequestBody(message, Collections.singletonList(structuredError)));
	}

	@Override
	public Completable validate(final ShoppingItemDto shoppingItemDto) {
		if (shoppingItemDto.getQuantity() < 1) {
			return Completable.error(ResourceOperationFailure.badRequestBody("Quantity must be positive"));
		} else {
			return Completable.complete();
		}
	}

}
