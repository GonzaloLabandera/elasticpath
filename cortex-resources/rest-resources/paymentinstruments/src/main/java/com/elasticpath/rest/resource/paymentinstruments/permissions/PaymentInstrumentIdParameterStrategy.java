/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.permissions;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.authorization.parameter.AbstractCollectionValueStrategy;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.id.Identifier;
import com.elasticpath.rest.id.transform.IdentifierTransformer;
import com.elasticpath.rest.id.transform.IdentifierTransformerProvider;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.PrincipalsUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Strategy to look up permission for payment instrument resource.
 */
@Singleton
@Named("paymentInstrumentIdParameterStrategy")
public final class PaymentInstrumentIdParameterStrategy extends AbstractCollectionValueStrategy {

	@Inject
	@ResourceRepository
	private Provider<Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier>> paymentInstrumentEntityRepository;

	@Inject
	@ResourceRepository
	private Provider<Repository<AccountEntity, AccountIdentifier>> accountRepository;

	@Inject
	@ResourceService
	private Provider<PaymentInstrumentRepository> paymentInstrumentRepository;

	@Inject
	private IdentifierTransformerProvider identifierTransformerProvider;

	@Override
	protected Collection<String> getParameterValues(final PrincipalCollection principals) {
		String scope = PrincipalsUtil.getScope(principals);
		final IdentifierTransformer<Identifier> identifierTransformer =
				identifierTransformerProvider.forUriPart(PaymentInstrumentIdentifier.PAYMENT_INSTRUMENT_ID);
		List<String> paymentInstrumentIds = paymentInstrumentEntityRepository.get()
				.findAll(StringIdentifier.of(scope))
				.map(paymentInstrumentIdentifier -> identifierTransformer.identifierToUri(paymentInstrumentIdentifier.getPaymentInstrumentId()))
				.toList()
				.blockingGet();

		List<String> accountIds = accountRepository.get().findAll(StringIdentifier.of(scope))
				.map(accountIdentifier -> accountIdentifier.getAccountId().getValue()).toList().blockingGet();

		List<String> accountPaymentInstrumentsIds = accountIds.stream()
				.map(accountId -> paymentInstrumentRepository.get().findAllAccountPaymentInstrumentsByAccountId(scope, accountId)
						.map(paymentAccountPaymentInstrumentIdentifier ->
								identifierTransformer.identifierToUri(paymentAccountPaymentInstrumentIdentifier.getAccountPaymentInstrumentId()))
						.toList().blockingGet())
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		paymentInstrumentIds.addAll(accountPaymentInstrumentsIds);

		return paymentInstrumentIds;
	}

}
