/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.account;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormResource;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UserCurrency;
import com.elasticpath.rest.helix.data.annotation.UserLocale;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.InstructionsEntityRepository;

/**
 * Request Instructions Form prototype for Read operation.
 */
public class ReadAccountRequestInstructionsFormPrototype implements AccountRequestInstructionsFormResource.Read {

	private final AccountRequestInstructionsFormIdentifier accountRequestInstructionsFormIdentifier;
	private final InstructionsEntityRepository repository;
	private final Currency currency;
	private final Locale locale;

	/**
	 * Constructor.
	 *
	 * @param accountRequestInstructionsFormIdentifier {@link AccountRequestInstructionsFormIdentifier}
	 * @param repository                               {@link InstructionsEntityRepository}
	 * @param locale                                   {@link Locale} for this request
	 * @param currency                                 {@link Currency} for this request
	 */
	@Inject
	public ReadAccountRequestInstructionsFormPrototype(
			@RequestIdentifier final AccountRequestInstructionsFormIdentifier accountRequestInstructionsFormIdentifier,
			@ResourceRepository final InstructionsEntityRepository repository,
			@UserLocale final Locale locale, @UserCurrency final Currency currency) {
		this.accountRequestInstructionsFormIdentifier = accountRequestInstructionsFormIdentifier;
		this.repository = repository;
		this.currency = currency;
		this.locale = locale;
	}

	@Override
	public Single<PaymentMethodConfigurationEntity> onRead() {
		String accountId = accountRequestInstructionsFormIdentifier
				.getAccountPaymentMethod().getAccountPaymentMethods().getAccount().getAccountId().getValue();
		String methodId = accountRequestInstructionsFormIdentifier.getAccountPaymentMethod().getAccountPaymentMethodId().getValue();
		return repository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(methodId, accountId, currency, locale);
	}
}
