/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.permissions;

import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.authorization.parameter.AbstractCollectionValueStrategy;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.Identifier;
import com.elasticpath.rest.id.transform.IdentifierTransformer;
import com.elasticpath.rest.id.transform.IdentifierTransformerProvider;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.PrincipalsUtil;

/**
 * Strategy to look up permission for payment instrument resource.
 */
@Singleton
@Named("paymentInstrumentIdParameterStrategy")
public final class PaymentInstrumentIdParameterStrategy extends AbstractCollectionValueStrategy {

	@Inject
	@ResourceRepository
	private Provider<Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier>> repository;

	@Inject
	private IdentifierTransformerProvider identifierTransformerProvider;

	@Override
	protected Collection<String> getParameterValues(final PrincipalCollection principals) {
		String scope = PrincipalsUtil.getScope(principals);
		final IdentifierTransformer<Identifier> identifierTransformer =
				identifierTransformerProvider.forUriPart(PaymentInstrumentIdentifier.PAYMENT_INSTRUMENT_ID);
		return repository.get()
				.findAll(StringIdentifier.of(scope))
				.map(paymentInstrumentIdentifier -> identifierTransformer.identifierToUri(paymentInstrumentIdentifier.getPaymentInstrumentId()))
				.toList()
				.blockingGet();
	}
}
