/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildInstructionsEntity;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstructionsIdentifier;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.definition.paymentinstructions.OrderPaymentInstructionsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Test for {@link com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.InstructionsEntityRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentInstructionsRepositoryTest {

	private static final String STORE_CODE = "MOBEE";
	private static final String METHOD_ID = "METHOD_ID";
	private static final String ORDER_ID = "ORDER_ID";
	private static final Map<String, String> COMMUNICATION_INSTRUCTIONS_MAP = ImmutableMap.of("CONTROL_KEY", "CONTROL_VALUE");
	private static final Map<String, String> PAYLOAD_MAP = ImmutableMap.of("PAYLOAD_KEY", "PAYLOAD_VALUE");

	private InstructionsEntity instructionsEntity;

	@InjectMocks
	private OrderPaymentInstructionsRepositoryImpl<InstructionsEntity, OrderPaymentInstructionsIdentifier> orderInstructionsEntityRepository;

	@Before
	public void initialize() {
		instructionsEntity = buildInstructionsEntity(COMMUNICATION_INSTRUCTIONS_MAP, PAYLOAD_MAP);
	}

	@Test
	public void findOneReturnsAppropriateInstructionsEntity() {
		orderInstructionsEntityRepository.findOne(buildOrderPaymentInstructionsIdentifier(StringIdentifier.of(STORE_CODE),
				StringIdentifier.of(METHOD_ID), StringIdentifier.of(ORDER_ID), COMMUNICATION_INSTRUCTIONS_MAP, PAYLOAD_MAP))
				.test()
				.assertValue(instructionsEntity);
	}

	@Test
	public void findOneReturnsEmptyEntityWhenNoInstructionsDataExists() {
		InstructionsEntity emptyInstructionsEntity = buildInstructionsEntity(Collections.emptyMap(), Collections.emptyMap());

		orderInstructionsEntityRepository.findOne(buildOrderPaymentInstructionsIdentifier(
				StringIdentifier.of(STORE_CODE), StringIdentifier.of(METHOD_ID), StringIdentifier.of(ORDER_ID),
				Collections.emptyMap(), Collections.emptyMap()))
				.test()
				.assertValue(emptyInstructionsEntity);
	}
}
