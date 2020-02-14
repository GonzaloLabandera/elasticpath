/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.advise;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormAdvisor;
import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.helix.data.annotation.UserCurrency;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.helix.data.annotation.UserLocale;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.validation.PICInstructionsFieldsValidatingRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;

/**
 * Advisor for order payment instrument form.
 */
public class OrderRequestInstructionsFormAdvisorImpl implements OrderRequestInstructionsFormAdvisor.FormAdvisor {

	private final OrderRequestInstructionsFormIdentifier orderRequestInstructionsFormIdentifier;
	private final String userId;
	private final Currency currency;
	private final Locale locale;
	private final PICInstructionsFieldsValidatingRepository paymentInstrumentFieldsValidationService;
	private final StructuredErrorMessageTransformer messageTransformer;

	/**
	 * Constructor.
	 *
	 * @param orderRequestInstructionsFormIdentifier order payment instrument form identifier.
	 * @param userId                                 user identifier.
	 * @param locale                                 {@link Locale} for this request.
	 * @param currency                               {@link Currency} for this request.
	 * @param validationService                      instruction fields validator.
	 * @param messageTransformer                     transforms {@link com.elasticpath.base.common.dto.StructuredErrorMessage} to  {@link Message}.
	 */
	@Inject
	public OrderRequestInstructionsFormAdvisorImpl(
			@RequestIdentifier final OrderRequestInstructionsFormIdentifier orderRequestInstructionsFormIdentifier,
			@UserId final String userId, @UserLocale final Locale locale, @UserCurrency final Currency currency,
			@ResourceRepository final PICInstructionsFieldsValidatingRepository validationService,
			@ResourceService final StructuredErrorMessageTransformer messageTransformer) {
		this.orderRequestInstructionsFormIdentifier = orderRequestInstructionsFormIdentifier;
		this.userId = userId;
		this.currency = currency;
		this.locale = locale;
		this.paymentInstrumentFieldsValidationService = validationService;
		this.messageTransformer = messageTransformer;
	}

	@Override
	public Observable<Message> onAdvise() {
		final String methodId = orderRequestInstructionsFormIdentifier.getOrderPaymentMethod().getPaymentMethodId().getValue();

		return Observable.fromIterable(messageTransformer.transform(paymentInstrumentFieldsValidationService.validate(methodId,
				currency,
				locale,
				userId), null));
	}
}
