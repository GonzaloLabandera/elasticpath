/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.profile.impl;

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
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Tests for {@link ProfilePaymentInstrumentFormEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfilePaymentInstrumentFormEntityRepositoryTest {

	private static final String SCOPE = "MOBEE";
	private static final String PAYMENT_METHOD_ID = "PAYMENT_METHOD_ID";
	private static final String USER_ID = "SHARED_ID";
	private static final List<String> FIELDS = ImmutableList.of("FIELD");

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private PaymentInstrumentRepository instrumentRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	private Customer customer;
	private PaymentInstrumentCreationFieldsDTO fields;

	@InjectMocks
	private ProfilePaymentInstrumentFormEntityRepositoryImpl<PaymentInstrumentForFormEntity, ProfilePaymentInstrumentFormIdentifier> repository;

	@Before
	public void setUp() {
		customer = mock(Customer.class);
		when(customer.isRegistered()).thenReturn(true);

		fields = mock(PaymentInstrumentCreationFieldsDTO.class);
		when(fields.getFields()).thenReturn(FIELDS);
		when(fields.isSaveable()).thenReturn(true);

		when(instrumentRepository.getPaymentInstrumentCreationFieldsForProviderConfigGuid(PAYMENT_METHOD_ID)).thenReturn(Single.just(fields));
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(storePaymentProviderConfigRepository.requiresBillingAddress(PAYMENT_METHOD_ID)).thenReturn(Single.just(false));
	}

	private ProfilePaymentInstrumentFormIdentifier createTestIdentifier() {
		ProfileIdentifier profileIdentifier = ProfileIdentifier.builder().withProfileId(StringIdentifier.of(USER_ID))
				.withScope(StringIdentifier.of(SCOPE))
				.build();

		return ProfilePaymentInstrumentFormIdentifier.builder()
				.withProfilePaymentMethod(ProfilePaymentMethodIdentifier.builder()
						.withProfilePaymentMethods(ProfilePaymentMethodsIdentifier.builder()
								.withProfile(profileIdentifier)
								.build())
						.withPaymentMethodId(StringIdentifier.of(PAYMENT_METHOD_ID))
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
		when(customer.isRegistered()).thenReturn(false);
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
