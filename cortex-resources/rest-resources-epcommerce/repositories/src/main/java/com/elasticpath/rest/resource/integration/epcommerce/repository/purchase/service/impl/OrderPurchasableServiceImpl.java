/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.service.impl;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.service.OrderPurchasableService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;
import com.elasticpath.service.shoppingcart.validation.PurchaseCartValidationService;
import com.elasticpath.service.store.StoreService;

/**
 * Implementation of the order purchasable service.
 */
@Component
public class OrderPurchasableServiceImpl implements OrderPurchasableService {

	private StoreService storeService;

	private CartOrderRepository cartOrderRepository;

	private StructuredErrorMessageTransformer messageConverter;

	private PurchaseCartValidationService purchaseCartValidationService;

	@Override
	public Observable<Message> validateOrderPurchasable(final OrderIdentifier order) {
		String storeCode = order.getScope().getValue();
		String cartOrderGuid = order.getOrderId().getValue();
		Store store = storeService.findStoreWithCode(storeCode);

		return cartOrderRepository.findByGuid(storeCode, cartOrderGuid)
				.flatMapObservable(cartOrder -> cartOrderRepository.getEnrichedShoppingCart(storeCode, cartOrder)
					.flatMap(shoppingCart ->
						Single.just(purchaseCartValidationService.validate(shoppingCart, shoppingCart.getShopper(), store))
						.map(structuredErrorMessages -> messageConverter.transform(structuredErrorMessages, shoppingCart.getGuid()))
					)
					.flatMapObservable(Observable::fromIterable));
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setMessageConverter(final StructuredErrorMessageTransformer messageConverter) {
		this.messageConverter = messageConverter;
	}

	@Reference
	public void setPurchaseCartValidationService(final PurchaseCartValidationService purchaseCartValidationService) {
		this.purchaseCartValidationService = purchaseCartValidationService;
	}

	@Reference
	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}
}
