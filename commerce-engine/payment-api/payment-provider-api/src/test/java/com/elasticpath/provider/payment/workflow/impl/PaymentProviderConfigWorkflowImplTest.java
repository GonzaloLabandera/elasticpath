/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow.impl;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.PaymentLocalizedProperties;
import com.elasticpath.provider.payment.PaymentLocalizedPropertiesImpl;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValue;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValueImpl;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.domain.impl.PaymentProviderConfigurationImpl;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.workflow.PaymentProviderConfigWorkflow;

/**
 * Tests for PaymentProviderConfigWorkflowImpl.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderConfigWorkflowImplTest {

	private static final String GUID = "guid";
	private static final String DEFAULT_DISPLAY_NAME = "displayName";
	private static final String LANGUAGE = "fr_CA";
	private static final String LOCALIZED_PROPERTIES_KEY = "paymentLocalizedPropertyDisplayName_" + LANGUAGE;
	private static final String LOCALIZED_PROPERTIES_VALUE = "localizedValue";

	@Mock
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Mock
	private BeanFactory beanFactory;

	private PaymentProviderConfigWorkflow paymentProviderConfigWorkflow;

	@Before
	public void setUp() {
		paymentProviderConfigWorkflow = new PaymentProviderConfigWorkflowImpl(paymentProviderConfigurationService, beanFactory);

		final PaymentLocalizedPropertyValue paymentLocalizedPropertyValue = mock(PaymentLocalizedPropertyValue.class);
		when(paymentLocalizedPropertyValue.getValue()).thenReturn(LOCALIZED_PROPERTIES_VALUE);

		final PaymentLocalizedProperties paymentLocalizedProperties = mock(PaymentLocalizedProperties.class);
		when(paymentLocalizedProperties.getPaymentLocalizedPropertiesMap())
				.thenReturn(Collections.singletonMap(LOCALIZED_PROPERTIES_KEY, paymentLocalizedPropertyValue));

		final PaymentProviderConfiguration paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
		when(paymentProviderConfiguration.getGuid()).thenReturn(GUID);
		when(paymentProviderConfiguration.getConfigurationName()).thenReturn("Configuration");
		when(paymentProviderConfiguration.getPaymentProviderPluginId()).thenReturn("plugin-id");
		when(paymentProviderConfiguration.getStatus()).thenReturn(PaymentProviderConfigurationStatus.ACTIVE);
		when(paymentProviderConfiguration.getPaymentConfigurationData()).thenReturn(Collections.emptySet());
		when(paymentProviderConfiguration.getDefaultDisplayName()).thenReturn(DEFAULT_DISPLAY_NAME);
		when(paymentProviderConfiguration.getPaymentLocalizedProperties()).thenReturn(paymentLocalizedProperties);

		when(paymentProviderConfigurationService.findByGuid(GUID)).thenReturn(paymentProviderConfiguration);

		when(beanFactory.getPrototypeBean(PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class))
				.thenReturn(new PaymentProviderConfigurationImpl());
		when(beanFactory.getPrototypeBean(PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTIES, PaymentLocalizedProperties.class))
				.thenReturn(new PaymentLocalizedPropertiesImpl());
		when(beanFactory.getPrototypeBean(PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTY_VALUE, PaymentLocalizedPropertyValue.class))
				.thenReturn(new PaymentLocalizedPropertyValueImpl());
		when(beanFactory.getPrototypeBean(PAYMENT_PROVIDER_CONFIGURATION_DTO, PaymentProviderConfigDTO.class))
				.thenReturn(new PaymentProviderConfigDTO());
	}

	@Test
	public void findByGuidShouldReturnPaymentProviderConfigDTOWithDefaultDisplayNameAndLocalizedNamesKeyAndLocalizedNamesValues() {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = paymentProviderConfigWorkflow.findByGuid(GUID);

		assertThat(paymentProviderConfigDTO.getDefaultDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
		assertThat(paymentProviderConfigDTO.getLocalizedNames()).containsOnlyKeys(LANGUAGE);
		assertThat(paymentProviderConfigDTO.getLocalizedNames().values()).containsOnly(LOCALIZED_PROPERTIES_VALUE);
	}

	@Test
	public void saveOrUpdateShouldBeCallForPaymentProviderConfigurationWithDefaultDisplayNameAndLocalizedNamesKeyAndLocalizedNamesValues() {
		final ArgumentCaptor<PaymentProviderConfiguration> paymentProviderConfigurationArgumentCaptor =
				ArgumentCaptor.forClass(PaymentProviderConfiguration.class);

		final PaymentProviderConfigDTO paymentProviderConfigDTO = createPaymentProviderConfigDTO();
		paymentProviderConfigWorkflow.saveOrUpdate(paymentProviderConfigDTO);

		verify(paymentProviderConfigurationService).saveOrUpdate(paymentProviderConfigurationArgumentCaptor.capture());

		final PaymentProviderConfiguration paymentProviderConfiguration = paymentProviderConfigurationArgumentCaptor.getValue();
		assertThat(paymentProviderConfiguration.getDefaultDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
		assertThat(paymentProviderConfiguration.getPaymentLocalizedProperties().getPaymentLocalizedPropertiesMap().keySet())
				.containsOnly(LOCALIZED_PROPERTIES_KEY);
		assertThat(paymentProviderConfiguration.getPaymentLocalizedProperties().getPaymentLocalizedPropertiesMap().values())
				.extracting(PaymentLocalizedPropertyValue::getPaymentLocalizedPropertyKey)
				.containsOnly(LOCALIZED_PROPERTIES_KEY);
		assertThat(paymentProviderConfiguration.getPaymentLocalizedProperties().getPaymentLocalizedPropertiesMap().values())
				.extracting(PaymentLocalizedPropertyValue::getValue)
				.containsOnly(LOCALIZED_PROPERTIES_VALUE);
	}

	private PaymentProviderConfigDTO createPaymentProviderConfigDTO() {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = new PaymentProviderConfigDTO();
		paymentProviderConfigDTO.setGuid("another-guid");
		paymentProviderConfigDTO.setConfigurationName("Configuration");
		paymentProviderConfigDTO.setPaymentProviderPluginBeanName("plugin-bean-name");
		paymentProviderConfigDTO.setStatus(PaymentProviderConfigurationStatus.ACTIVE);
		paymentProviderConfigDTO.setPaymentConfigurationData(Collections.emptyMap());
		paymentProviderConfigDTO.setDefaultDisplayName(DEFAULT_DISPLAY_NAME);
		paymentProviderConfigDTO.setLocalizedNames(Collections.singletonMap(LANGUAGE, LOCALIZED_PROPERTIES_VALUE));

		return paymentProviderConfigDTO;
	}

}