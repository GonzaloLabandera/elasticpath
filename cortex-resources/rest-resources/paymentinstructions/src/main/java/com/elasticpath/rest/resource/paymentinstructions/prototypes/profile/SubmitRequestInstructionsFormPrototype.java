/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.profile;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstructions.PaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormResource;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UserCurrency;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.helix.data.annotation.UserLocale;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.InstructionsEntityRepository;

/**
 * Payment Instructions Form prototype for Submit operation.
 */
public class SubmitRequestInstructionsFormPrototype implements RequestInstructionsFormResource.SubmitWithResult {

	private final PaymentMethodConfigurationEntity paymentMethodConfigurationEntity;
	private final RequestInstructionsFormIdentifier requestIdentifier;
	private final InstructionsEntityRepository repository;
	private final String userId;
	private final Locale locale;
	private final Currency currency;

	/**
	 * Constructor.
	 *
	 * @param paymentMethodConfigurationEntity {@link PaymentMethodConfigurationEntity} encapsulating the submitted form data
	 * @param requestIdentifier                {@link RequestInstructionsFormIdentifier}
	 * @param repository                       {@link InstructionsEntityRepository}
	 * @param userId                           user identifier
	 * @param locale                           {@link Locale} for this request
	 * @param currency                         {@link Currency} for this request
	 */
	@Inject
	public SubmitRequestInstructionsFormPrototype(@RequestForm final PaymentMethodConfigurationEntity paymentMethodConfigurationEntity,
												  @RequestIdentifier final RequestInstructionsFormIdentifier requestIdentifier,
												  @ResourceRepository final InstructionsEntityRepository repository,
												  @UserId final String userId,
												  @UserLocale final Locale locale,
												  @UserCurrency final Currency currency) {
		this.paymentMethodConfigurationEntity = paymentMethodConfigurationEntity;
		this.requestIdentifier = requestIdentifier;
		this.repository = repository;
		this.userId = userId;
		this.locale = locale;
		this.currency = currency;
	}

	@Override
	public Single<SubmitResult<PaymentInstructionsIdentifier>> onSubmitWithResult() {
		IdentifierPart<String> methodId = requestIdentifier.getProfilePaymentMethod().getPaymentMethodId();

		return repository.submitRequestInstructionsForm(methodId.getValue(), locale, currency, paymentMethodConfigurationEntity, userId)
				.map(picInstructionsDTO -> PaymentResourceHelpers.buildPaymentInstructionsIdentifier(
						requestIdentifier.getProfilePaymentMethod().getProfilePaymentMethods().getProfile(),
						methodId, picInstructionsDTO.getCommunicationInstructions(),
						picInstructionsDTO.getPayload()))
				.map(this::buildSubmitResult);
	}

	private SubmitResult<PaymentInstructionsIdentifier> buildSubmitResult(final PaymentInstructionsIdentifier identifier) {
		return SubmitResult.<PaymentInstructionsIdentifier>builder()
				.withIdentifier(identifier)
				.withStatus(SubmitStatus.CREATED)
				.build();
	}

}
