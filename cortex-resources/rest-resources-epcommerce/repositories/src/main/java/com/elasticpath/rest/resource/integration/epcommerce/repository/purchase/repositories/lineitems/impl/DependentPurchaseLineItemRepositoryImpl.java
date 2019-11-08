/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.lineitems.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.commons.tree.TreeNode;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.lineitems.DependentPurchaseLineItemRepository;

/**
 * Implements {@link DependentPurchaseLineItemRepository}.
 */
@Component
public class DependentPurchaseLineItemRepositoryImpl implements DependentPurchaseLineItemRepository {

	private OrderRepository orderRepository;

	@Override
	public Maybe<PurchaseLineItemIdentifier> findParentPurchaseLineItem(final PurchaseLineItemIdentifier dependentLineItem) {

		return findOrderSkuByPurchaseLineItem(dependentLineItem)
				.filter(dependentOrderSku -> dependentOrderSku.getParent() != null)
				.map(OrderSku::getParent)
				.map(parentOrderSku -> PurchaseLineItemIdentifier.builder()
						.withLineItemId(PathIdentifier.of(buildGuidListFromRootOrderSku(parentOrderSku)))
						.withPurchaseLineItems(dependentLineItem.getPurchaseLineItems())
						.build());

	}

	@Override
	public Observable<PurchaseLineItemIdentifier> findDependentPurchaseLineItems(final PurchaseLineItemIdentifier parentLineItem) {

		return findOrderSkuByPurchaseLineItem(parentLineItem)
				.flattenAsObservable(TreeNode::getChildren)
				.map(OrderSku.class::cast)
				.map(childOrderSku -> PurchaseLineItemIdentifier.builder()
						.withLineItemId(PathIdentifier.of(buildGuidListFromRootOrderSku(childOrderSku)))
						.withPurchaseLineItems(parentLineItem.getPurchaseLineItems())
						.build());

	}

	/**
	 * Build order sku guid list from root order sku.
	 *
	 * @param orderSku the starting order sku
	 * @return the guid list from root order sku.
	 */
	private List<String> buildGuidListFromRootOrderSku(final OrderSku orderSku) {
		final List<String> guidListToRootOrderSku = buildGuidListToRootOrderSku(orderSku, new ArrayList<>());
		return Lists.reverse(guidListToRootOrderSku);
	}

	/**
	 * Recursively build order sku guid list from starting order sku to root order sku.
	 *
	 * @param orderSku               the starting order sku. it could be a top order sku or a dependent order sku.
	 * @param guidListToRootOrderSku the list of guids to the root order sku.
	 * @return the guid list to root order sku.
	 */
	private List<String> buildGuidListToRootOrderSku(final OrderSku orderSku, final List<String> guidListToRootOrderSku) {

		Objects.requireNonNull(guidListToRootOrderSku);

		if (orderSku == null) {
			return guidListToRootOrderSku;
		}

		guidListToRootOrderSku.add(orderSku.getGuid());
		return buildGuidListToRootOrderSku(orderSku.getParent(), guidListToRootOrderSku);

	}

	private Single<OrderSku> findOrderSkuByPurchaseLineItem(final PurchaseLineItemIdentifier lineItem) {

		final String storeCode = lineItem.getPurchaseLineItems().getPurchase().getPurchases().getScope().getValue();
		final String orderGuid = lineItem.getPurchaseLineItems().getPurchase().getPurchaseId().getValue();
		final List<String> dependentGuidPathFromRootItem = lineItem.getLineItemId().getValue();

		return orderRepository.findOrderSku(storeCode, orderGuid, dependentGuidPathFromRootItem);

	}

	@Reference
	public void setOrderRepository(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}
}
