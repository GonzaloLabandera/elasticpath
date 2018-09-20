/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.transformer.handler.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import com.elasticpath.rest.definition.paymentmethods.CreditCardEntity;
import com.elasticpath.rest.definition.paymentmethods.PaymentmethodsMediaTypes;
import com.elasticpath.rest.resource.commons.handler.PaymentHandler;
import com.elasticpath.rest.schema.ResourceEntity;

/**
 * Implementation of {@link PaymentHandler} that handles {@link CreditCardEntity}s.
 */
@Singleton
@Named("creditCardPaymentMethodHandler")
public final class CreditCardPaymentMethodHandler implements PaymentHandler {
	@Override
	public Class<? extends ResourceEntity> handledType() {
		return CreditCardEntity.class;
	}

	@Override
	public String representationType() {
		return PaymentmethodsMediaTypes.CREDIT_CARD.id();
	}
}
