/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Completable;
import io.reactivex.Observable;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.ShopperReference;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.AddToCartAdvisorService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;
import com.elasticpath.rest.schema.StructuredMessageTypes;
import com.elasticpath.service.shoppingcart.validation.AddProductSkuToCartValidationService;
import com.elasticpath.service.shoppingcart.validation.ProductSkuValidationContext;

/**
 * Shopping Item Validation Service.
 */
@Singleton
@Named("addToCartAdvisorService")
public class AddToCartAdvisorServiceImpl implements AddToCartAdvisorService {

	private final ProductSkuRepository productSkuRepository;
	private final AddProductSkuToCartValidationService addToCartValidationService;
	private final StructuredErrorMessageTransformer structuredErrorMessageTransformer;
	private final CustomerSessionRepository customerSessionRepository;
	private final StoreRepository storeRepository;

	/**
	 * Constructor.
	 *
	 * @param productSkuRepository              the product sku repository
	 * @param addToCartValidationService        the add to cart validation service
	 * @param structuredErrorMessageTransformer the structured error message transformer
	 * @param customerSessionRepository         the customer session repository
	 * @param storeRepository                   the store repository
	 */
	@Inject
	@SuppressWarnings({"checkstyle:parameternumber"})
	public AddToCartAdvisorServiceImpl(
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository,
			@Named("addProductSkuToCartValidationService") final AddProductSkuToCartValidationService addToCartValidationService,
			@Named("structuredErrorMessageTransformer") final StructuredErrorMessageTransformer structuredErrorMessageTransformer,
			@Named("customerSessionRepository") final CustomerSessionRepository customerSessionRepository,
			@Named("storeRepository") final StoreRepository storeRepository) {
		this.productSkuRepository = productSkuRepository;
		this.addToCartValidationService = addToCartValidationService;
		this.structuredErrorMessageTransformer = structuredErrorMessageTransformer;
		this.customerSessionRepository = customerSessionRepository;
		this.storeRepository = storeRepository;
	}

	@Override
	public Observable<Message> validateItemPurchasable(final String scope, final String skuCode) {
		return productSkuRepository.getProductSkuWithAttributesByCode(skuCode)
				.flatMapObservable(prodSku -> validateItemPurchasable(scope, prodSku, null));
	}

	@Override
	public Observable<Message> validateItemPurchasable(final String scope, final ProductSku productSku,
													   final ProductSku parentProductSku) {
		return customerSessionRepository.findOrCreateCustomerSessionAsSingle()
				.map(ShopperReference::getShopper)
				.flatMapObservable(shopper -> storeRepository.findStoreAsSingle(scope)
						.flatMapObservable(store -> {
							ProductSkuValidationContext context = addToCartValidationService.buildContext(productSku, parentProductSku,
									store, shopper);
							return Observable.fromIterable(structuredErrorMessageTransformer.transform(ImmutableList.copyOf(addToCartValidationService
									.validate(context)), productSku.getSkuCode()));
						}));
	}

	@Override
	public Completable validateLineItemEntity(final LineItemEntity lineItemEntity) {
		Integer quantity = lineItemEntity.getQuantity();
		if (quantity == null) {
			return this.getInvalidQuantityErrorMessage();
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
}
