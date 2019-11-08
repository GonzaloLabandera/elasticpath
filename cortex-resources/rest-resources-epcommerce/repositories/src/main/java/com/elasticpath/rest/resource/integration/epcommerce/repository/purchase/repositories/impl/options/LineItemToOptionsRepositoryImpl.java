/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionsIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Repository that returns options for the given line item.
 *
 * @param <I> line item identifier
 * @param <O> line item options identifier
 */
@Component
public class LineItemToOptionsRepositoryImpl<I extends PurchaseLineItemIdentifier, O extends PurchaseLineItemOptionsIdentifier>
		implements LinksRepository<PurchaseLineItemIdentifier, PurchaseLineItemOptionsIdentifier> {

	private static final Logger LOG = LoggerFactory.getLogger(LineItemToOptionsRepositoryImpl.class);
	private ProductSkuRepository productSkuRepository;
	private OrderRepository orderRepository;

	@Override
	public Observable<PurchaseLineItemOptionsIdentifier> getElements(final PurchaseLineItemIdentifier identifier) {
		PurchaseIdentifier purchaseIdentifier = identifier.getPurchaseLineItems().getPurchase();
		List<String> guidPathFromRootItem = identifier.getLineItemId().getValue();
		String scope = purchaseIdentifier.getPurchases().getScope().getValue();
		String purchaseId = purchaseIdentifier.getPurchaseId().getValue();
		return orderRepository.findProductSku(scope, purchaseId, guidPathFromRootItem)
				.map(ProductSku::getSkuCode)
				.doOnError(throwable -> LOG.info("Error looking for product sku."))
				.flatMap(this::doesSkuContainOptions)
				.flatMapObservable(containsOptions -> containsOptions ? buildPurchaseLineItemOptionsIdentifier(identifier) : Observable.empty())
				.onErrorResumeNext(Observable.empty());
	}

	private Observable<PurchaseLineItemOptionsIdentifier> buildPurchaseLineItemOptionsIdentifier(final PurchaseLineItemIdentifier identifier) {
		return Observable.just(PurchaseLineItemOptionsIdentifier.builder()
				.withPurchaseLineItem(identifier)
				.build());
	}

	/**
	 * Checks if item has options.
	 *
	 * @param skuCode skuCode
	 * @return true any options are present for this item
	 */
	protected Single<Boolean> doesSkuContainOptions(final String skuCode) {
		return productSkuRepository.getProductSkuOptionsByCode(skuCode)
				.doOnError(throwable -> LOG.info("Error looking for sku options for sku code '{}'.", skuCode))
				.isEmpty()
				.map(empty -> !empty);
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}

	@Reference
	public void setOrderRepository(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}
}
