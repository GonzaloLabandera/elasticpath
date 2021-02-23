/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.account;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentForFormEntity;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Tests for {@link AccountPaymentInstrumentFormEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountPaymentInstrumentFormEntityRepositoryImplTest {

	private static final String SCOPE = "MOBEE";
	private static final String PAYMENT_METHOD_ID = "PAYMENT_METHOD_ID";
	private static final String ACCOUNT_ID = "ACCOUNT_ID";
	private static final List<String> FIELDS = ImmutableList.of("FIELD");

	@Mock
	private PaymentInstrumentRepository paymentInstrumentRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	private Customer account;
	private PaymentInstrumentCreationFieldsDTO fields;

	@InjectMocks
	private AccountPaymentInstrumentFormEntityRepositoryImpl<PaymentInstrumentForFormEntity, AccountPaymentInstrumentFormIdentifier> repository;

	@Before
	public void setUp() {
		account = mock(Customer.class);
		when(account.isRegistered()).thenReturn(true);

		fields = mock(PaymentInstrumentCreationFieldsDTO.class);
		when(fields.getFields()).thenReturn(FIELDS);
		when(fields.isSaveable()).thenReturn(true);

		when(paymentInstrumentRepository
				.getAccountPaymentInstrumentCreationFieldsForProviderConfigGuid(PAYMENT_METHOD_ID, ACCOUNT_ID))
				.thenReturn(Single.just(fields));
		when(paymentInstrumentRepository.getAccountIdFromResourceOperationContext())
				.thenReturn(StringIdentifier.of(ACCOUNT_ID));
		when(customerRepository.getCustomer(ACCOUNT_ID)).thenReturn(Single.just(account));
		when(storePaymentProviderConfigRepository.requiresBillingAddress(PAYMENT_METHOD_ID)).thenReturn(Single.just(false));
	}

	private AccountPaymentInstrumentFormIdentifier createTestIdentifier() {
		AccountIdentifier accountIdentifier = AccountIdentifier.builder().withAccountId(StringIdentifier.of(ACCOUNT_ID))
				.withAccounts(AccountsIdentifier.builder()
						.withScope(StringIdentifier.of(SCOPE))
						.build())
				.build();

		return AccountPaymentInstrumentFormIdentifier.builder()
				.withAccountPaymentMethod(AccountPaymentMethodIdentifier.builder()
						.withAccountPaymentMethods(AccountPaymentMethodsIdentifier.builder()
								.withAccount(accountIdentifier)
								.build())
						.withAccountPaymentMethodId(StringIdentifier.of(PAYMENT_METHOD_ID))
						.build())
				.build();
	}

	@Test
	public void findOneReturnsPaymentInstrumentFormFieldsWithSaveWhenCustomerIsRegisteredAndFieldsAreSaveable() {
		final PaymentInstrumentForFormEntity expectedEntity = buildPaymentInstrumentForFormEntity(FIELDS, true, false);

		repository.findOne(createTestIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(expectedEntity);
	}

	@Test
	public void findOneReturnsPaymentInstrumentFormFieldsWithoutSaveWhenCustomerIsNotRegistered() {
		when(account.isRegistered()).thenReturn(false);
		final PaymentInstrumentForFormEntity expectedEntity = buildPaymentInstrumentForFormEntity(FIELDS, false, false);

		repository.findOne(createTestIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(expectedEntity);
	}

	@Test
	public void findOneReturnsPaymentInstrumentFormFieldsWithoutSaveWhenCustomerIsRegisteredAndFieldsAreNotSaveable() {
		when(fields.isSaveable()).thenReturn(false);
		final PaymentInstrumentForFormEntity expectedEntity = buildPaymentInstrumentForFormEntity(FIELDS, false, false);

		repository.findOne(createTestIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(expectedEntity);
	}
}
