/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.profile;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UserCurrency;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.helix.data.annotation.UserLocale;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.InstructionsEntityRepository;

/**
 * Request Instructions Form prototype for Read operation.
 */
public class ReadRequestInstructionsFormPrototype implements RequestInstructionsFormResource.Read {

	private final RequestInstructionsFormIdentifier requestInstructionsFormIdentifier;
	private final InstructionsEntityRepository repository;
	private final String userId;
	private final Currency currency;
	private final Locale locale;

	/**
	 * Constructor.
	 *
	 * @param requestInstructionsFormIdentifier {@link RequestInstructionsFormIdentifier}
	 * @param repository                        {@link InstructionsEntityRepository}
	 * @param userId                            user identifier
	 * @param locale                            {@link Locale} for this request
	 * @param currency                          {@link Currency} for this request
	 */
	@Inject
	public ReadRequestInstructionsFormPrototype(@RequestIdentifier final RequestInstructionsFormIdentifier requestInstructionsFormIdentifier,
												@ResourceRepository final InstructionsEntityRepository repository, @UserId final String userId,
												@UserLocale final Locale locale, @UserCurrency final Currency currency) {
		this.requestInstructionsFormIdentifier = requestInstructionsFormIdentifier;
		this.repository = repository;
		this.userId = userId;
		this.currency = currency;
		this.locale = locale;
	}

	@Override
	public Single<PaymentMethodConfigurationEntity> onRead() {
		String methodId = requestInstructionsFormIdentifier.getProfilePaymentMethod().getPaymentMethodId().getValue();
		return repository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(methodId, userId, currency, locale);
	}
}
