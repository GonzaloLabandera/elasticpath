/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentInstructionsIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildInstructionsEntity;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Test for
 * {@link com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.impl.AccountPaymentInstructionsRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountPaymentInstructionsRepositoryTest {

	private static final String STORE_CODE = "MOBEE";
	private static final String METHOD_ID = "METHOD_ID";
	private static final String ACCOUNT_ID = "ACCOUNT_ID";

	private static final Map<String, String> COMMUNICATION_INSTRUCTIONS_MAP = ImmutableMap.of("CONTROL_KEY", "CONTROL_VALUE");
	private static final Map<String, String> PAYLOAD_MAP = ImmutableMap.of("PAYLOAD_KEY", "PAYLOAD_VALUE");

	private InstructionsEntity instructionsEntity;

	@InjectMocks
	private AccountPaymentInstructionsRepositoryImpl<InstructionsEntity, AccountPaymentInstructionsIdentifier> repository;

	@Before
	public void initialize() {
		instructionsEntity = buildInstructionsEntity(COMMUNICATION_INSTRUCTIONS_MAP, PAYLOAD_MAP);
	}

	@Test
	public void findOneReturnsAppropriateInstructionsEntity() {
		AccountIdentifier accountIdentifier = AccountIdentifier.builder()
				.withAccountId(StringIdentifier.of(ACCOUNT_ID))
				.withAccounts(AccountsIdentifier.builder()
						.withScope(StringIdentifier.of(STORE_CODE)).build())
				.build();

		repository.findOne(buildAccountPaymentInstructionsIdentifier(
				accountIdentifier, StringIdentifier.of(METHOD_ID), COMMUNICATION_INSTRUCTIONS_MAP, PAYLOAD_MAP))
				.test()
				.assertValue(instructionsEntity);
	}

	@Test
	public void findOneReturnsEmptyEntityWhenNoInstructionsDataExists() {
		AccountIdentifier accountIdentifier = AccountIdentifier.builder()
				.withAccountId(StringIdentifier.of(ACCOUNT_ID))
				.withAccounts(AccountsIdentifier.builder()
						.withScope(StringIdentifier.of(STORE_CODE)).build())
				.build();
		InstructionsEntity emptyInstructionsEntity = buildInstructionsEntity(Collections.emptyMap(), Collections.emptyMap());

		repository.findOne(buildAccountPaymentInstructionsIdentifier(
				accountIdentifier, StringIdentifier.of(METHOD_ID), Collections.emptyMap(), Collections.emptyMap()))
				.test()
				.assertValue(emptyInstructionsEntity);
	}
}
