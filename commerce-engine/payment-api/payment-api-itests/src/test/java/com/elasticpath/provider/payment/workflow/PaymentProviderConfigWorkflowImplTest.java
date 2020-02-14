/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

public class PaymentProviderConfigWorkflowImplTest extends DbTestCase {

	private static final String PAYMENT_PROVIDER_CONFIG_GUID = "paymentProviderConfiguration";

	@Autowired
	private PaymentProviderConfigWorkflow paymentProviderConfigWorkflow;

	@Test
	@DirtiesDatabase
	public void ensureFindPaymentProviderConfigurationByGuidFindsPaymentProvider() {
		PaymentProviderConfigDTO configuration = createAndPersistPaymentProviderConfiguration();

		PaymentProviderConfigDTO savedConfiguration
				= paymentProviderConfigWorkflow.findByGuid(PAYMENT_PROVIDER_CONFIG_GUID);

		assertThat(savedConfiguration).isEqualTo(configuration);
	}

	@Test
	@DirtiesDatabase
	public void ensureFindPaymentProviderConfigurationByStatusFindsPaymentProvider() {
		PaymentProviderConfigDTO configuration = createAndPersistPaymentProviderConfiguration();
		List<PaymentProviderConfigDTO> savedConfigurationList
				= paymentProviderConfigWorkflow.findByStatus(PaymentProviderConfigurationStatus.ACTIVE);
		assertThat(savedConfigurationList).hasSize(1);
		assertThat(savedConfigurationList.get(0)).isEqualTo(configuration);
	}

	@Test
	@DirtiesDatabase
	public void ensureFindByCustomerFindsCustomerPaymentInstruments() {
		PaymentProviderConfigDTO configuration = createAndPersistPaymentProviderConfiguration();

		List<PaymentProviderConfigDTO> savedConfigurations
				= paymentProviderConfigWorkflow.findAll();

		assertThat(savedConfigurations).contains(configuration);
	}

	private PaymentProviderConfigDTO createAndPersistPaymentProviderConfiguration() {
		PaymentProviderConfigDTO configuration = new PaymentProviderConfigDTO();

		configuration.setGuid(PAYMENT_PROVIDER_CONFIG_GUID);
		configuration.setConfigurationName(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setDefaultDisplayName(Utils.uniqueCode("DEFAULT_DISPLAY_NAME"));
		configuration.setPaymentProviderPluginBeanName("paymentProviderPluginForIntegrationTesting");
		configuration.setPaymentConfigurationData(Collections.emptyMap());
		configuration.setStatus(PaymentProviderConfigurationStatus.ACTIVE);
		configuration.setLocalizedNames(Collections.emptyMap());

		paymentProviderConfigWorkflow.saveOrUpdate(configuration);

		return configuration;
	}
}
