/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.orderpaymentapi.management.impl;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_PLUGIN_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKeyBuilder;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTOBuilder;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginsDTO;
import com.elasticpath.provider.payment.workflow.PaymentProviderWorkflow;

/**
 * Unit test for {@link PaymentProviderManagementServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderManagementServiceImplTest {

	private static final String VENDOR_ID = "vendorId";
	private static final String TEST_PAYMENT_PROVIDER = "test-payment-provider";
	private static final String METHOD_ID = "methodId";
	private static final String KEY = "key";
	private static final String DESCRIPTION = "description";

	@Mock
	private PaymentProviderWorkflow paymentProviderWorkflow;

	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private PaymentProviderManagementServiceImpl paymentProviderManagementService;

	@Before
	public void setUp() {
		when(beanFactory.getPrototypeBean(PAYMENT_PROVIDER_PLUGIN_DTO, PaymentProviderPluginDTO.class))
				.thenAnswer((Answer<PaymentProviderPluginDTO>) invocation -> new PaymentProviderPluginDTO());

		final PaymentProviderPluginsDTO paymentProviderPluginsDTO = createTestPaymentProviderPluginsDTO();
		when(paymentProviderWorkflow.findAll()).thenReturn(paymentProviderPluginsDTO);
	}

	@Test
	public void testGetCustomerPaymentInstrumentMapsPaymentInstrumentToDTO() {
		Map<String, PaymentProviderPluginDTO> paymentProviderPlugins = paymentProviderManagementService.findAll();

		assertThat(paymentProviderPlugins.keySet()).containsOnly(TEST_PAYMENT_PROVIDER);
		assertThat(createTestPaymentProviderPluginDTO())
				.isEqualTo(createTestPaymentProviderPluginDTO());
	}

	private PaymentProviderPluginDTO createTestPaymentProviderPluginDTO() {
		return PaymentProviderPluginDTOBuilder.builder()
				.withPluginBeanName(TEST_PAYMENT_PROVIDER)
				.withPaymentVendorId(VENDOR_ID)
				.withPaymentMethodId(METHOD_ID)
				.withConfigurationKeys(Collections.singletonList(createPluginConfigurationKey()))
				.build(beanFactory);
	}

	private PluginConfigurationKey createPluginConfigurationKey() {
		return PluginConfigurationKeyBuilder.builder()
				.withKey(KEY)
				.withDescription(DESCRIPTION)
				.build();
	}

	private PaymentProviderPluginsDTO createTestPaymentProviderPluginsDTO() {
		PaymentProviderPluginsDTO paymentProviderPluginsDTO = new PaymentProviderPluginsDTO();
		paymentProviderPluginsDTO.setPaymentProviderPluginDTOs(Collections.singletonList(createTestPaymentProviderPluginDTO()));
		return paymentProviderPluginsDTO;
	}
}