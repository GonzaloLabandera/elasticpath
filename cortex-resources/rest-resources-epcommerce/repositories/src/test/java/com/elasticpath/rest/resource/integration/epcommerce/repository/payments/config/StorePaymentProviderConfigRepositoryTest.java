/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_CONFIG_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.STORE_PAYMENT_PROVIDER_ID;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.impl.StorePaymentProviderConfigRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;

/**
 * Tests for {@link StorePaymentProviderConfigRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StorePaymentProviderConfigRepositoryTest {

	@InjectMocks
	private StorePaymentProviderConfigRepositoryImpl repository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	@Mock
	private OrderPaymentApiRepository orderPaymentApiRepository;

	@Mock
	private Store store;

	private final StorePaymentProviderConfig storePaymentProviderConfig = mock(StorePaymentProviderConfig.class);

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);

		when(storePaymentProviderConfigService.findByGuid(STORE_PAYMENT_PROVIDER_ID)).thenReturn(storePaymentProviderConfig);
		when(storePaymentProviderConfigService.findByStore(store)).thenReturn(ImmutableList.of(storePaymentProviderConfig));

		when(storePaymentProviderConfig.getPaymentProviderConfigGuid()).thenReturn(PAYMENT_PROVIDER_CONFIG_ID);
	}

	@Test
	public void findByGuidReturnsExpectedStorePaymentProviderConfig() {
		repository.findByGuid(STORE_PAYMENT_PROVIDER_ID)
				.test()
				.assertNoErrors()
				.assertValue(storePaymentProviderConfig);
	}

	@Test
	public void findByStoreReturnsExpectedStorePaymentProviderConfigs() {
		repository.findByStore(store)
				.test()
				.assertNoErrors()
				.assertValue(storePaymentProviderConfig);
	}

	@Test
	public void findByStoreReturnsEmptyWhenNoStorePaymentProviderConfigsExistForStore() {
		when(storePaymentProviderConfigService.findByStore(store)).thenReturn(Collections.emptyList());

		repository.findByStore(store)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void getPaymentProviderConfigIdByStorePaymentProviderConfigIdReturnsExpectedProviderId() {
		repository.getPaymentProviderConfigIdByStorePaymentProviderConfigId(STORE_PAYMENT_PROVIDER_ID)
				.test()
				.assertNoErrors()
				.assertValue(PAYMENT_PROVIDER_CONFIG_ID);
	}

	@Test
	public void requiresBillingAddressReturnsPaymentProviderBillingAddressRequirement() {
		when(orderPaymentApiRepository.requiresBillingAddress(PAYMENT_PROVIDER_CONFIG_ID)).thenReturn(Single.just(true));

		repository.requiresBillingAddress(STORE_PAYMENT_PROVIDER_ID)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}
}
