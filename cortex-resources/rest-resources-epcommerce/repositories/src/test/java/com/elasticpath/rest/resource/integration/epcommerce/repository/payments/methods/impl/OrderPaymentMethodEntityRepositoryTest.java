/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.methods.impl;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentMethodIdentifier;

import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.StorePaymentProviderConfigImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.impl.OrderPaymentMethodEntityRepositoryImpl;

/**
 * Test for {@link OrderPaymentMethodEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentMethodEntityRepositoryTest {

	private static final String SCOPE = "MOBEE";
	private static final String ORDER_ID = "ORDER_ID";
	private static final String CONFIG_ID_1 = "CONFIG_GUID_1";
	private static final String CONFIG_ID_2 = "CONFIG_GUID_2";

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@InjectMocks
	private OrderPaymentMethodEntityRepositoryImpl<PaymentMethodEntity, OrderPaymentMethodIdentifier> repository;

	@Before
	public void setup() {
		OrderPaymentMethodsIdentifier orderPaymentMethodsIdentifier = OrderPaymentMethodsIdentifier.builder()
				.withOrder(OrderIdentifier.builder()
						.withOrderId(StringIdentifier.of(ORDER_ID))
						.withScope(StringIdentifier.of(SCOPE))
						.build())
				.build();
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(orderPaymentMethodsIdentifier));
	}

	@Test
	public void findAllReturnsExpectedPaymentMethods() {
		StorePaymentProviderConfig storePaymentProviderConfig1 = createTestStorePaymentProviderConfig(CONFIG_ID_1);
		StorePaymentProviderConfig storePaymentProviderConfig2 = createTestStorePaymentProviderConfig(CONFIG_ID_2);

		when(paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(SCOPE))
				.thenReturn(Observable.just(storePaymentProviderConfig1, storePaymentProviderConfig2));

		OrderPaymentMethodIdentifier orderPaymentMethodIdentifier1 = buildOrderPaymentMethodIdentifier(StringIdentifier.of(SCOPE),
				StringIdentifier.of(CONFIG_ID_1),
				StringIdentifier.of(ORDER_ID));

		OrderPaymentMethodIdentifier orderPaymentMethodIdentifier2 = buildOrderPaymentMethodIdentifier(StringIdentifier.of(SCOPE),
				StringIdentifier.of(CONFIG_ID_2),
				StringIdentifier.of(ORDER_ID));

		repository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertValues(orderPaymentMethodIdentifier1, orderPaymentMethodIdentifier2);
	}

	@Test
	public void findAllReturnsEmptyWhenNoPaymentMethodsExist() {
		when(paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(SCOPE)).thenReturn(Observable.empty());

		repository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertNoValues()
				.assertNoErrors();
	}

	@Test
	public void findAllReturnsEmptyWhenResourceIdentifierDoesNotExist() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.empty());
		repository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertNoValues()
				.assertNoErrors();
	}

	@Test
	public void findOneReturnsExpectedPaymentMethod() {
		PaymentMethodEntity paymentMethodEntity = PaymentMethodEntity.builder()
				.withName("Test Payment Method Name")
				.build();

		when(paymentMethodRepository.findOnePaymentMethodEntityForMethodId(CONFIG_ID_1)).thenReturn(Single.just(paymentMethodEntity));

		repository.findOne(buildOrderPaymentMethodIdentifier(StringIdentifier.of(SCOPE), StringIdentifier.of(CONFIG_ID_1),
				StringIdentifier.of(ORDER_ID)))
				.test()
				.assertNoErrors()
				.assertValue(paymentMethodEntity);
	}

	@Test
	public void findOneReturnsErrorWhenPaymentMethodDoesNotExist() {
		when(paymentMethodRepository.findOnePaymentMethodEntityForMethodId(CONFIG_ID_1))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		repository.findOne(buildOrderPaymentMethodIdentifier(StringIdentifier.of(SCOPE), StringIdentifier.of(CONFIG_ID_1),
				StringIdentifier.of(ORDER_ID)))
				.test()
				.assertError(ResourceOperationFailure.notFound());
	}

	private StorePaymentProviderConfig createTestStorePaymentProviderConfig(final String guid) {
		StorePaymentProviderConfig storePaymentProviderConfig = new StorePaymentProviderConfigImpl();
		storePaymentProviderConfig.setGuid(guid);
		return storePaymentProviderConfig;
	}
}
