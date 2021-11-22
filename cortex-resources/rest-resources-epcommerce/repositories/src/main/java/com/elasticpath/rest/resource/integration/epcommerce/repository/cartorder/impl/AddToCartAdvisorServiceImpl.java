/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Completable;
import io.reactivex.Observable;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.AddToCartAdvisorService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;
import com.elasticpath.rest.schema.StructuredMessageTypes;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.context.builders.ProductSkuValidationContextBuilder;
import com.elasticpath.xpf.converters.StructuredErrorMessageConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Shopping Item Validation Service.
 */
@Singleton
@Named("addToCartAdvisorService")
public class AddToCartAdvisorServiceImpl implements AddToCartAdvisorService {

	private final ProductSkuRepository productSkuRepository;
	private final ProductSkuValidationContextBuilder productSkuValidationContextBuilder;
	private final StructuredErrorMessageTransformer structuredErrorMessageTransformer;
	private final ShopperRepository shopperRepository;
	private final StoreRepository storeRepository;
	private final XPFExtensionLookup extensionLookup;
	private final StructuredErrorMessageConverter structuredErrorMessageConverter;
	/**
	 * Constructor.
	 * @param productSkuRepository               the product sku repository
	 * @param productSkuValidationContextBuilder the product sku validation context builder
	 * @param structuredErrorMessageTransformer  the structured error message transformer
	 * @param shopperRepository                  the shopper repository
	 * @param storeRepository                    the store repository
	 * @param extensionLookup                    the extension lookup
	 * @param structuredErrorMessageConverter    the structured error message converter
	 */
	@Inject
	@SuppressWarnings({"checkstyle:parameternumber"})
	public AddToCartAdvisorServiceImpl(
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository,
			@Named("xpfProductSkuValidationContextBuilder") final ProductSkuValidationContextBuilder productSkuValidationContextBuilder,
			@Named("structuredErrorMessageTransformer") final StructuredErrorMessageTransformer structuredErrorMessageTransformer,
			@Named("shopperRepository") final ShopperRepository shopperRepository,
			@Named("storeRepository") final StoreRepository storeRepository,
			@Named("xpfExtensionLookup") final XPFExtensionLookup extensionLookup,
			@Named("structuredErrorMessageConverter") final StructuredErrorMessageConverter structuredErrorMessageConverter) {
		this.productSkuRepository = productSkuRepository;
		this.productSkuValidationContextBuilder = productSkuValidationContextBuilder;
		this.structuredErrorMessageTransformer = structuredErrorMessageTransformer;
		this.shopperRepository = shopperRepository;
		this.storeRepository = storeRepository;
		this.extensionLookup = extensionLookup;
		this.structuredErrorMessageConverter = structuredErrorMessageConverter;
	}

	@Override
	public Observable<Message> validateItemPurchasable(final String scope, final String skuCode) {
		return productSkuRepository.getProductSkuWithAttributesByCode(skuCode)
				.flatMapObservable(prodSku -> validateItemPurchasable(scope, prodSku, null));
	}

	@Override
	public Observable<Message> validateItemPurchasable(final String scope, final ProductSku productSku,
													   final ProductSku parentProductSku) {
		return shopperRepository.findOrCreateShopper()
				.flatMapObservable(shopper -> storeRepository.findStoreAsSingle(scope)
						.flatMapObservable(store ->
								Observable.fromIterable(structuredErrorMessageTransformer.transform(
										validateItems(
												productSkuValidationContextBuilder.build(productSku, parentProductSku, shopper, store),
												store),
										productSku.getSkuCode()))));
	}

	private List<StructuredErrorMessage> validateItems(final XPFProductSkuValidationContext context, final Store store) {
		return extensionLookup.getMultipleExtensions(ProductSkuValidator.class,
				XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ,
				new XPFExtensionSelectorByStoreCode(store.getCode()))
				.stream()
				.map(strategy -> strategy.validate(context))
				.flatMap(Collection::stream)
				.map(structuredErrorMessageConverter::convert)
				.collect(Collectors.toList());
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
	 * @return the messageProductSkuConverter
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
