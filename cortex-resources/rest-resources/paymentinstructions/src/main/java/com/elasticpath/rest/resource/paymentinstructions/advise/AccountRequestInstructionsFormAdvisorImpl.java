/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstructions.advise;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormAdvisor;
import com.elasticpath.rest.definition.paymentinstructions.AccountRequestInstructionsFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.helix.data.annotation.UserCurrency;
import com.elasticpath.rest.helix.data.annotation.UserLocale;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.validation.PICInstructionsFieldsValidatingRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;

/**
 * Advisor for payment instrument form for customer profile.
 */
public class AccountRequestInstructionsFormAdvisorImpl implements AccountRequestInstructionsFormAdvisor.FormAdvisor {

	private final AccountRequestInstructionsFormIdentifier accountRequestInstructionsFormIdentifier;
	private final Currency currency;
	private final Locale locale;
	private final PICInstructionsFieldsValidatingRepository paymentInstrumentFieldsValidationService;
	private final StructuredErrorMessageTransformer messageTransformer;

	/**
	 * Constructor.
	 *
	 * @param accountRequestInstructionsFormIdentifier request instructions form identifier.
	 * @param locale                                   {@link Locale} for this request.
	 * @param currency                                 {@link Currency} for this request.
	 * @param validationService                        instruction fields validator.
	 * @param messageTransformer                       transforms {@link com.elasticpath.base.common.dto.StructuredErrorMessage} to  {@link Message}.
	 */
	@Inject
	public AccountRequestInstructionsFormAdvisorImpl(
			@RequestIdentifier final AccountRequestInstructionsFormIdentifier accountRequestInstructionsFormIdentifier,
			@UserLocale final Locale locale, @UserCurrency final Currency currency,
			@ResourceRepository final PICInstructionsFieldsValidatingRepository validationService,
			@ResourceService final StructuredErrorMessageTransformer messageTransformer) {
		this.accountRequestInstructionsFormIdentifier = accountRequestInstructionsFormIdentifier;
		this.currency = currency;
		this.locale = locale;
		this.paymentInstrumentFieldsValidationService = validationService;
		this.messageTransformer = messageTransformer;
	}

	@Override
	public Observable<Message> onAdvise() {
		final String methodId = accountRequestInstructionsFormIdentifier.getAccountPaymentMethod().getAccountPaymentMethodId().getValue();
		final String accountId =
				accountRequestInstructionsFormIdentifier.getAccountPaymentMethod().getAccountPaymentMethods().getAccount().getAccountId().getValue();

		return Observable.fromIterable(messageTransformer.transform(paymentInstrumentFieldsValidationService.validate(methodId,
				currency,
				locale,
				accountId), null));
	}
}
