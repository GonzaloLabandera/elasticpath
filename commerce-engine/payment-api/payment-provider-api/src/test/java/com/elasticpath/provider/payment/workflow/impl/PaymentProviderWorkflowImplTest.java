/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow.impl;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_PLUGINS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_PLUGIN_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginsDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;

/**
 * PaymentAPIWorkflowImpl tests.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderWorkflowImplTest {

	private static final String PAYMENT_PLUGIN_ID = "pluginId";
	private static final String PAYMENT_VENDOR_ID = "Vendor";
	private static final String PAYMENT_METHOD_ID = "Card";

	@Mock
	private PaymentProviderService paymentProviderService;

	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private PaymentProviderWorkflowImpl testee;

	@Mock
	private PaymentProviderPlugin plugin;

	@Before
	public void setUp() throws PaymentInstrumentCreationFailedException {
		when(beanFactory.getPrototypeBean(PAYMENT_PROVIDER_PLUGIN_DTO, PaymentProviderPluginDTO.class))
				.thenReturn(new PaymentProviderPluginDTO());
		when(beanFactory.getPrototypeBean(PAYMENT_PROVIDER_PLUGINS_DTO, PaymentProviderPluginsDTO.class))
				.thenReturn(new PaymentProviderPluginsDTO());

		when(plugin.getUniquePluginId()).thenReturn(PAYMENT_PLUGIN_ID);
		when(plugin.getPaymentVendorId()).thenReturn(PAYMENT_VENDOR_ID);
		when(plugin.getPaymentMethodId()).thenReturn(PAYMENT_METHOD_ID);
		when(plugin.getConfigurationKeys()).thenReturn(Collections.emptyList());

		when(paymentProviderService.getPlugins()).thenReturn(Collections.singletonList(plugin));
	}

	@Test
	public void allAvailablePluginsListContainsOnlyWhatHasBeenInjected() {
		PaymentProviderPluginDTO expectedDto = new PaymentProviderPluginDTO();
		expectedDto.setPluginBeanName(PAYMENT_PLUGIN_ID);
		expectedDto.setPaymentVendorId(PAYMENT_VENDOR_ID);
		expectedDto.setPaymentMethodId(PAYMENT_METHOD_ID);
		expectedDto.setConfigurationKeys(Collections.emptyList());
		PaymentProviderPluginsDTO paymentProviderPlugins = testee.findAll();

		assertThat(paymentProviderPlugins.getPaymentProviderPluginDTOs())
				.containsOnly(expectedDto);
	}


}
