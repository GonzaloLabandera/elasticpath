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
 * Order Payment Methods to Orders link.
 */
public class OrderPaymentMethodsToOrdersRelationshipImpl implements OrderPaymentMethodsForOrderRelationship.LinkFrom {

	private final OrderPaymentMethodsIdentifier orderPaymentMethodsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentMethodsIdentifier orderPaymentMethodsIdentifier
	 */
	@Inject
	public OrderPaymentMethodsToOrdersRelationshipImpl(@RequestIdentifier final OrderPaymentMethodsIdentifier orderPaymentMethodsIdentifier) {
		this.orderPaymentMethodsIdentifier = orderPaymentMethodsIdentifier;
	}

	@Override
	public Observable<OrderIdentifier> onLinkFrom() {
		return Observable.just(orderPaymentMethodsIdentifier.getOrder());
	}
}
