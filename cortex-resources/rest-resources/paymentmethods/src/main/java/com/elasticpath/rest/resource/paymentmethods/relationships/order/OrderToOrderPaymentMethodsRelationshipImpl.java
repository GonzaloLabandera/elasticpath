/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsForOrderRelationship;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Order to Order Payment Methods link.
 */
public class OrderToOrderPaymentMethodsRelationshipImpl implements OrderPaymentMethodsForOrderRelationship.LinkTo {

	private final OrderIdentifier orderIdentifier;

	/**
	 * Constructor.
	 *
	 * @param orderIdentifier orderIdentifier
	 */
	@Inject
	public OrderToOrderPaymentMethodsRelationshipImpl(@RequestIdentifier final OrderIdentifier orderIdentifier) {
		this.orderIdentifier = orderIdentifier;
	}

	@Override
	public Observable<OrderPaymentMethodsIdentifier> onLinkTo() {
		return Observable.just(OrderPaymentMethodsIdentifier.builder().withOrder(orderIdentifier).build());
	}
}
