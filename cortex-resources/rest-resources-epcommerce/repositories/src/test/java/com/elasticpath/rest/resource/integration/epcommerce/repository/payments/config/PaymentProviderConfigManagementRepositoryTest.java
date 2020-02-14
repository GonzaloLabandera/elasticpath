/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_CONFIG_DISPLAY_NAME;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_CONFIG_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_CONFIG_NAME;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.STORE_PAYMENT_PROVIDER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.createTestPaymentProviderConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.impl.PaymentProviderConfigManagementRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Tests for {@link PaymentProviderConfigManagementRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderConfigManagementRepositoryTest {

	@InjectMocks
	private PaymentProviderConfigManagementRepositoryImpl repository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private PaymentProviderConfigManagementService storePaymentProviderConfigService;

	private PaymentProviderConfigDTO expectedPaymentProviderConfiguration;

	@Before
	public void setUp() {
		expectedPaymentProviderConfiguration = createTestPaymentProviderConfiguration(PAYMENT_PROVIDER_CONFIG_ID,
				PAYMENT_PROVIDER_CONFIG_NAME, PAYMENT_PROVIDER_CONFIG_DISPLAY_NAME);

		repository.setReactiveAdapter(reactiveAdapter);
	}

	@Test
	public void findByGuidReturnsExpectedPaymentProviderConfig() {
		when(storePaymentProviderConfigService.findByGuid(STORE_PAYMENT_PROVIDER_ID)).thenReturn(expectedPaymentProviderConfiguration);

		repository.findByGuid(STORE_PAYMENT_PROVIDER_ID)
				.test()
				.assertNoErrors()
				.assertValue(expectedPaymentProviderConfiguration);
	}

	@Test
	public void findByGuidFailsWhenNoPaymentProviderConfigFound() {
		repository.findByGuid(STORE_PAYMENT_PROVIDER_ID)
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound(
						"No payment provider configuration found for guid " + STORE_PAYMENT_PROVIDER_ID + "."));
	}
}
