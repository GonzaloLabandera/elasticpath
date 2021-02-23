/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstrumentForFormEntity;

import java.math.BigDecimal;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Tests for {@link OrderPaymentInstrumentFormEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentInstrumentFormEntityRepositoryTest {

	private static final String SCOPE = "MOBEE";
	private static final String ORDER_ID = "ORDER_ID";
	private static final String PAYMENT_METHOD_ID = "PAYMENT_METHOD_ID";
	private static final String USER_ID = "SHARED_ID";
	private static final ImmutableList<String> TEST_ATTRIBUTE_DATA = ImmutableList.of("field");

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private PaymentInstrumentRepository instrumentRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	@Mock
	private Customer customer;

	@Mock
	private PaymentInstrumentCreationFieldsDTO fields;

	@InjectMocks
	private OrderPaymentInstrumentFormEntityRepositoryImpl<OrderPaymentInstrumentForFormEntity, OrderPaymentInstrumentFormIdentifier> repository;

	@Before
	public void setUp() {
		fields = mock(PaymentInstrumentCreationFieldsDTO.class);
		when(fields.getFields()).thenReturn(TEST_ATTRIBUTE_DATA);

		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(instrumentRepository.getPaymentInstrumentCreationFieldsForProviderConfigGuid(PAYMENT_METHOD_ID)).thenReturn(Single.just(fields));
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(storePaymentProviderConfigRepository.requiresBillingAddress(PAYMENT_METHOD_ID)).thenReturn(Single.just(false));

		final Subject subject = mock(Subject.class);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(customerRepository.getCustomerGuid(USER_ID, subject)).thenReturn(USER_ID);
	}

	private OrderPaymentInstrumentFormIdentifier createTestIdentifier() {
		return OrderPaymentInstrumentFormIdentifier.builder()
				.withOrderPaymentMethod(OrderPaymentMethodIdentifier.builder()
						.withOrderPaymentMethods(OrderPaymentMethodsIdentifier.builder()
								.withOrder(OrderIdentifier.builder()
										.withScope(StringIdentifier.of(SCOPE))
										.withOrderId(StringIdentifier.of(ORDER_ID))
										.build())
								.build())
						.withPaymentMethodId(StringIdentifier.of(PAYMENT_METHOD_ID))
						.build())
				.build();
	}

	@Test
	public void findOneReturnsPaymentInstrumentFormFieldsWithSaveWhenCustomerIsRegisteredAndFieldsAreSaveable() {
		when(customer.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);
		when(fields.isSaveable()).thenReturn(true);

		final OrderPaymentInstrumentForFormEntity expectedEntity = buildOrderPaymentInstrumentForFormEntity(
				BigDecimal.ZERO, TEST_ATTRIBUTE_DATA, true, false);

		repository.findOne(createTestIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(expectedEntity);
	}

	@Test
	public void findOneReturnsPaymentInstrumentFormFieldsWithoutSaveWhenCustomerIsRegisteredAndFieldsAreNotSaveable() {
		when(customer.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);
		when(fields.isSaveable()).thenReturn(false);

		final OrderPaymentInstrumentForFormEntity expectedEntity = buildOrderPaymentInstrumentForFormEntity(
				BigDecimal.ZERO, TEST_ATTRIBUTE_DATA, false, false);

		repository.findOne(createTestIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(expectedEntity);
	}

	@Test
	public void findOneReturnsPaymentInstrumentFormFieldsWithoutSaveWhenCustomerIsNotRegistered() {
		when(customer.getCustomerType()).thenReturn(CustomerType.SINGLE_SESSION_USER);

		final OrderPaymentInstrumentForFormEntity expectedEntity = buildOrderPaymentInstrumentForFormEntity(
				BigDecimal.ZERO, TEST_ATTRIBUTE_DATA, false, false);

		repository.findOne(createTestIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(expectedEntity);
	}

}
