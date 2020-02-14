/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.service.provider.ExternalPluginLoader;
import com.elasticpath.provider.payment.service.provider.impl.PaymentProviderServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderServiceTest {

	private static final String INTERNAL_PLUGIN_BEAN_NAME = "INTERNAL_PLUGIN_BEAN_NAME";
	private static final String EXTERNAL_PLUGIN_BEAN_NAME = "EXTERNAL_PLUGIN_BEAN_NAME";

	@InjectMocks
	private PaymentProviderServiceImpl testee;

	@Mock
	private PaymentProviderPlugin internalPlugin;

	@Mock
	private PaymentProviderPlugin externalPlugin;

	@Mock
	private PaymentProviderConfiguration configuration;

	@Mock
	private PaymentProviderConfigurationData configData;

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	private ExternalPluginLoader externalPluginLoader;

	@Before
	public void setUp() throws Exception {
		when(applicationContext.getBeansOfType(PaymentProviderPlugin.class))
				.thenReturn(ImmutableMap.of(INTERNAL_PLUGIN_BEAN_NAME, internalPlugin));
		when(externalPluginLoader.load(eq(PaymentProviderPlugin.class)))
				.thenReturn(ImmutableMap.of(EXTERNAL_PLUGIN_BEAN_NAME, externalPlugin));

		testee.init();

		lenient().when(internalPlugin.getUniquePluginId()).thenReturn(INTERNAL_PLUGIN_BEAN_NAME);
		lenient().when(externalPlugin.getUniquePluginId()).thenReturn(EXTERNAL_PLUGIN_BEAN_NAME);

		when(configData.getKey()).thenReturn("a_key");
		when(configData.getData()).thenReturn("a_data");

		when(configuration.getConfigurationName()).thenReturn("Config_xyz");
		when(configuration.getPaymentConfigurationData())
				.thenReturn(Stream.of(configData).collect(Collectors.toSet()));
	}

	@Test
	public void allPluginsAreLoaded() {
		assertThat(testee.getPlugins(), containsInAnyOrder(internalPlugin, externalPlugin));
		verify(internalPlugin).setUniquePluginId(INTERNAL_PLUGIN_BEAN_NAME);
		verify(externalPlugin).setUniquePluginId(EXTERNAL_PLUGIN_BEAN_NAME);
	}

	@Test
	public void createProvider() {
		when(internalPlugin.getPaymentVendorId()).thenReturn("Plugin Name");
		when(configuration.getPaymentProviderPluginId()).thenReturn(INTERNAL_PLUGIN_BEAN_NAME);

		PaymentProvider provider = testee.createProvider(configuration);

		assertThat(provider.getPaymentProviderPluginId(), equalTo(INTERNAL_PLUGIN_BEAN_NAME));
		assertThat(provider.getConfigurationName(), equalTo("Config_xyz"));
		assertThat(provider.getPaymentVendorId(), equalTo("Plugin Name"));
		assertThat(provider.getConfiguration(), contains(configData));
	}

	@Test(expected = IllegalStateException.class)
	public void createProviderThrowsExceptionWhenPluginIsMissing() {
		when(configuration.getPaymentProviderPluginId()).thenReturn("missing_plugin_id");

		testee.createProvider(configuration);
	}

}