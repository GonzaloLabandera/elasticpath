/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstructions.prototypes.order;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstructions.OrderPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormResource;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
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
 * Order Payment Instructions Form prototype for Submit operation.
 */
public class SubmitOrderRequestInstructionsFormPrototype implements OrderRequestInstructionsFormResource.SubmitWithResult {

	private final PaymentMethodConfigurationEntity paymentMethodConfigurationEntity;
	private final OrderRequestInstructionsFormIdentifier requestIdentifier;
	private final InstructionsEntityRepository repository;
	private final String userId;
	private final Locale locale;
	private final Currency currency;


	/**
	 * Constructor.
	 *
	 * @param paymentMethodConfigurationEntity {@link PaymentMethodConfigurationEntity} encapsulating the submitted form data
	 * @param requestIdentifier                {@link OrderRequestInstructionsFormIdentifier}
	 * @param repository                       {@link InstructionsEntityRepository}
	 * @param userId                           user identifier
	 * @param locale                           {@link Locale} for this request
	 * @param currency                         {@link Currency} for this request
	 */
	@Inject
	public SubmitOrderRequestInstructionsFormPrototype(@RequestForm final PaymentMethodConfigurationEntity paymentMethodConfigurationEntity,
													   @RequestIdentifier final OrderRequestInstructionsFormIdentifier requestIdentifier,
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
	public Single<SubmitResult<OrderPaymentInstructionsIdentifier>> onSubmitWithResult() {
		IdentifierPart<String> scope = requestIdentifier.getOrderPaymentMethod().getOrderPaymentMethods().getOrder().getScope();
		IdentifierPart<String> paymentMethodId = requestIdentifier.getOrderPaymentMethod().getPaymentMethodId();
		IdentifierPart<String> orderId = requestIdentifier.getOrderPaymentMethod().getOrderPaymentMethods().getOrder().getOrderId();

		return repository.submitRequestInstructionsForm(paymentMethodId.getValue(), locale, currency, paymentMethodConfigurationEntity, userId)
				.map(picInstructions -> PaymentResourceHelpers.buildOrderPaymentInstructionsIdentifier(scope, paymentMethodId, orderId,
						picInstructions.getCommunicationInstructions(), picInstructions.getPayload()))
				.map(this::buildSubmitResult);
	}

	private SubmitResult<OrderPaymentInstructionsIdentifier> buildSubmitResult(final OrderPaymentInstructionsIdentifier identifier) {
		return SubmitResult.<OrderPaymentInstructionsIdentifier>builder()
				.withIdentifier(identifier)
				.withStatus(SubmitStatus.CREATED)
				.build();
	}

}
