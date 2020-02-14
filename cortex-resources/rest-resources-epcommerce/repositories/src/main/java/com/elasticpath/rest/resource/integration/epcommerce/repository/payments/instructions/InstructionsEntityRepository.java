/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions;

import java.util.Currency;
import java.util.Locale;

import io.reactivex.Single;

import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;

/**
 * The facade for operations with payment instructions.
 */
public interface InstructionsEntityRepository {

	/**
	 * Gets the instrument creation instructions fields, i.e. the parameters required to request payment instrument
	 * creation instructions, for the given parameters.
	 *
	 * @param methodId method id used to retrieve the appropriate payment provider
	 * @param userId   unique user identifier
	 * @param currency {@link Currency} for this request
	 * @param locale   {@link Locale} for this request
	 * @return an entity with creation instructions fields
	 */
	Single<PaymentMethodConfigurationEntity> getPaymentInstrumentCreationInstructionsFieldsForMethodId(String methodId, String userId,
																									   Currency currency, Locale locale);

	/**
	 * Submits the request instructions form with provided form details, returning payment instrument creation instructions
	 * for use during the payment instrument creation flow.
	 *
	 * @param methodId     method id used to retrieve the appropriate payment provider
	 * @param locale       {@link Locale} for this request
	 * @param currency     {@link Currency} for this request
	 * @param configEntity configuration entity containing the provided form details
	 * @param userId       unique user identifier
	 * @return {@link PICInstructionsDTO} for creating a corresponding payment instrument.
	 */
	Single<PICInstructionsDTO> submitRequestInstructionsForm(String methodId, Locale locale, Currency currency,
															 PaymentMethodConfigurationEntity configEntity, String userId);
}
