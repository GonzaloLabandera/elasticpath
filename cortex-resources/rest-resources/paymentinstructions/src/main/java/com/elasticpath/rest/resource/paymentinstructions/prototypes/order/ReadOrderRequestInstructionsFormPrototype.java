/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.order;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormResource;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UserCurrency;
import com.elasticpath.rest.helix.data.annotation.UserId;
import com.elasticpath.rest.helix.data.annotation.UserLocale;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.InstructionsEntityRepository;

/**
 * Request Instructions Form prototype for Read operation.
 */
public class ReadOrderRequestInstructionsFormPrototype implements OrderRequestInstructionsFormResource.Read {

	private final OrderRequestInstructionsFormIdentifier identifier;
	private final InstructionsEntityRepository repository;
	private final String userId;
	private final Locale locale;
	private final Currency currency;

	/**
	 * Constructor.
	 *
	 * @param identifier {@link OrderRequestInstructionsFormIdentifier}
	 * @param repository {@link InstructionsEntityRepository}
	 * @param userId     unique user identifier
	 * @param locale     locale for the this request
	 * @param currency   currency for this request
	 */
	@Inject
	public ReadOrderRequestInstructionsFormPrototype(@RequestIdentifier final OrderRequestInstructionsFormIdentifier identifier,
													 @ResourceRepository final InstructionsEntityRepository repository,
													 @UserId final String userId,
													 @UserLocale final Locale locale,
													 @UserCurrency final Currency currency) {
		this.identifier = identifier;
		this.repository = repository;
		this.userId = userId;
		this.locale = locale;
		this.currency = currency;
	}

	@Override
	public Single<PaymentMethodConfigurationEntity> onRead() {
		String methodId = identifier.getOrderPaymentMethod().getPaymentMethodId().getValue();

		return repository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(methodId, userId, currency, locale);
	}
}
