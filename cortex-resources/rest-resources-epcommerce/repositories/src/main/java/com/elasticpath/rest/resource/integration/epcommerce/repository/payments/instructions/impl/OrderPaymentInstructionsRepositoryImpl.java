/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildInstructionsEntity;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.definition.paymentinstructions.OrderPaymentInstructionsIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers;

/**
 * RequestInstructions Entity Repository for Profile.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class OrderPaymentInstructionsRepositoryImpl<E extends InstructionsEntity, I extends OrderPaymentInstructionsIdentifier>
		implements Repository<InstructionsEntity, OrderPaymentInstructionsIdentifier> {

	@Override
	public Single<InstructionsEntity> findOne(final OrderPaymentInstructionsIdentifier identifier) {
		Map<String, String> communicationInstructions = new HashMap<>(identifier.getCommunicationInstructionsId().getValue());
		Map<String, String> payload = new HashMap<>(identifier.getPayloadId().getValue());

		communicationInstructions.remove(PaymentResourceHelpers.FAKE_INSTRUCTIONS_FIELD);
		payload.remove(PaymentResourceHelpers.FAKE_INSTRUCTIONS_FIELD);

		return Single.just(buildInstructionsEntity(communicationInstructions, payload));
	}
}
