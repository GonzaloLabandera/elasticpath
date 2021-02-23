/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildInstructionsEntity;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstructions.AccountPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers;

/**
 * RequestInstructions Entity Repository for Profile.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountPaymentInstructionsRepositoryImpl<E extends InstructionsEntity, I extends AccountPaymentInstructionsIdentifier>
		implements Repository<InstructionsEntity, AccountPaymentInstructionsIdentifier> {

	@Override
	public Single<InstructionsEntity> findOne(final AccountPaymentInstructionsIdentifier identifier) {
		Map<String, String> communicationInstructions = new HashMap<>(identifier.getAccountCommunicationInstructionsId().getValue());
		Map<String, String> payload = new HashMap<>(identifier.getPayloadId().getValue());

		communicationInstructions.remove(PaymentResourceHelpers.FAKE_INSTRUCTIONS_FIELD);
		payload.remove(PaymentResourceHelpers.FAKE_INSTRUCTIONS_FIELD);

		return Single.just(buildInstructionsEntity(communicationInstructions, payload));
	}
}
