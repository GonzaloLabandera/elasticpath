/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

public class PaymentAPIWorkflowServiceTest extends BasicSpringContextTest {

	@Autowired
	private PaymentAPIWorkflow testee;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Autowired
	private PaymentInstrumentService paymentInstrumentService;

	private static final String PAYMENT_INSTRUMENT_NAME = "display-name";

	/**
	 * Test creating a new payment instrument fails when configuration is missing.
	 */
	@Test(expected = IllegalStateException.class)
	public void testCreatePaymentInstrumentWithoutConfiguration() {
		testee.createPI("not-existing-configuration-guid", Collections.emptyMap(), new PICRequestContextDTO());
	}

	/**
	 * Test creating a new payment instrument fails when plugin is missing.
	 */
	@DirtiesDatabase
	@Test(expected = IllegalStateException.class)
	public void testCreatePaymentInstrumentWithoutPlugin() {
		String configurationGuid = createAndPersistTestPaymentProviderConfiguration("missing-plugin");
		testee.createPI(configurationGuid, ImmutableMap.of(PAYMENT_INSTRUMENT_NAME, PAYMENT_INSTRUMENT_NAME), new PICRequestContextDTO());
	}

	/**
	 * Test creating a new payment instrument fails when plugin is failing the process.
	 */
	@DirtiesDatabase
	@Test(expected = PaymentsException.class)
	public void testCreatePaymentInstrumentFails() {
		String configurationGuid = createAndPersistTestPaymentProviderConfiguration("paymentProviderPluginForIntegrationTesting");

		testee.createPI(configurationGuid, ImmutableMap.of("fail", "true"), new PICRequestContextDTO());
	}

	/**
	 * Test creating a new payment instrument succeeds.
	 */
	@DirtiesDatabase
	@Test
	public void testCreatePaymentInstrument() {
		String configurationGuid = createAndPersistTestPaymentProviderConfiguration("paymentProviderPluginForIntegrationTesting");

		String paymentInstrumentGuid = testee.createPI(configurationGuid, ImmutableMap.of(PAYMENT_INSTRUMENT_NAME, PAYMENT_INSTRUMENT_NAME),
				createPICRequestContextDTO());

		PaymentInstrument paymentInstrument = paymentInstrumentService.findByGuid(paymentInstrumentGuid);
		assertNotNull("Payment instrument is null", paymentInstrument);
		assertNotNull("Payment instrument data is null", paymentInstrument.getPaymentInstrumentData());
		assertFalse("Payment instrument data is empty", paymentInstrument.getPaymentInstrumentData().isEmpty());
	}

	private PICRequestContextDTO createPICRequestContextDTO() {
		final PICRequestContextDTO picRequestContextDTO = new PICRequestContextDTO();
		picRequestContextDTO.setAddressDTO(createAddressDTO());

		return picRequestContextDTO;
	}

	private AddressDTO createAddressDTO() {
		final AddressDTO addressDTO = new AddressDTO();
		addressDTO.setGuid("addressGuid");

		return addressDTO;
	}

	private String createAndPersistTestPaymentProviderConfiguration(final String pluginId) {
		PaymentProviderConfiguration configuration = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class);

		configuration.setConfigurationName(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setPaymentProviderPluginId(pluginId);
		configuration.setPaymentConfigurationData(Collections.emptySet());

		paymentProviderConfigurationService.saveOrUpdate(configuration);

		return configuration.getGuid();
	}

}
