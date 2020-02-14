/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.relationships.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsForOrderPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Order Payment Methods to Orders link.
 */
public class OrderPaymentMethodToOrderPaymentMethodsRelationshipImpl implements OrderPaymentMethodsForOrderPaymentMethodRelationship.LinkTo {

	private final OrderPaymentMethodIdentifier orderPaymentMethodIdentifier;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentMethodIdentifier orderPaymentMethodIdentifier
	 */
	@Inject
	public OrderPaymentMethodToOrderPaymentMethodsRelationshipImpl(@RequestIdentifier final OrderPaymentMethodIdentifier
																		   orderPaymentMethodIdentifier) {
		this.orderPaymentMethodIdentifier = orderPaymentMethodIdentifier;
	}

	@Override
	public Observable<OrderPaymentMethodsIdentifier> onLinkTo() {
		return Observable.just(orderPaymentMethodIdentifier.getOrderPaymentMethods());
	}
}