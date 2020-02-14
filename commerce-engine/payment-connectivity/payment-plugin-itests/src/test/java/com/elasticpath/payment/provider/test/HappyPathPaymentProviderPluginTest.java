/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.util.Utils;

/**
 * Happy Path payment provider plugin integration test.
 */
public class HappyPathPaymentProviderPluginTest extends DbTestCase {

	private static final String HAPPY_PATH_PAYMENT_INSTRUMENT = "happy-path-payment-instrument";
	private static final String HAPPY_PATH_ANOTHER_PAYMENT_INSTRUMENT = "happy-path-another-payment-instrument";
	private static final String CONFIG_A = "Value of Config A";
	private static final String CONFIG_B = "Value of Config B";
	private static final String ANOTHER_CONFIG_A = "Another value of Config A";
	private static final String ANOTHER_CONFIG_B = "Another value of Config B";

	@Autowired
	private PaymentProviderService paymentProviderService;

	@Autowired
	private PaymentInstrumentService paymentInstrumentService;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Before
	public void setUp() {
		createAndPersistTestPaymentInstrument(HAPPY_PATH_PAYMENT_INSTRUMENT,
				createAndPersistTestPaymentProviderConfiguration(createTestPaymentProviderConfigurationData(CONFIG_A, CONFIG_B)));
		createAndPersistTestPaymentInstrument(HAPPY_PATH_ANOTHER_PAYMENT_INSTRUMENT,
				createAndPersistTestPaymentProviderConfiguration(createTestPaymentProviderConfigurationData(ANOTHER_CONFIG_A, ANOTHER_CONFIG_B)));
	}

	/**
	 * Checks if multiple payment providers can be created without conflicts using instruments referencing same plugin.
	 */
	@Test
	public void testHappyPathPaymentInstruments() {
		PaymentInstrument paymentInstrument = paymentInstrumentService.findByGuid(HAPPY_PATH_PAYMENT_INSTRUMENT);
		PaymentProvider provider = paymentProviderService.createProvider(paymentInstrument.getPaymentProviderConfiguration());
		assertTrue(provider.getCapability(ChargeCapability.class).map(capability -> {
			try {
				return capability.charge(new TenUsDollarsChargeCapabilityRequest());
			} catch (PaymentCapabilityRequestFailedException e) {
				throw new CapabilityException(e);
			}
		}).map(response -> {
			assertThat(response.getData().get(HappyPathPaymentProviderPlugin.CONFIG_KEY_A), equalTo(CONFIG_A));
			assertThat(response.getData().get(HappyPathPaymentProviderPlugin.CONFIG_KEY_B), equalTo(CONFIG_B));
			return true;
		}).isPresent());

		PaymentInstrument anotherPaymentInstrument = paymentInstrumentService.findByGuid(HAPPY_PATH_ANOTHER_PAYMENT_INSTRUMENT);
		PaymentProvider anotherProvider = paymentProviderService.createProvider(anotherPaymentInstrument.getPaymentProviderConfiguration());
		assertTrue(anotherProvider.getCapability(ChargeCapability.class).map(capability -> {
			try {
				return capability.charge(new TenUsDollarsChargeCapabilityRequest());
			} catch (PaymentCapabilityRequestFailedException e) {
				throw new CapabilityException(e);
			}
		}).map(response -> {
			assertThat(response.getData().get(HappyPathPaymentProviderPlugin.CONFIG_KEY_A), equalTo(ANOTHER_CONFIG_A));
			assertThat(response.getData().get(HappyPathPaymentProviderPlugin.CONFIG_KEY_B), equalTo(ANOTHER_CONFIG_B));
			return true;
		}).isPresent());
	}

	private static class CapabilityException extends RuntimeException {

		private static final long serialVersionUID = 5000000001L;

		CapabilityException(final PaymentCapabilityRequestFailedException cause) {
			super(cause.getInternalMessage(), cause);
		}

	}

	private PaymentProviderConfiguration createAndPersistTestPaymentProviderConfiguration(final Set<PaymentProviderConfigurationData> set) {
		final PaymentProviderConfiguration configuration = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class);

		configuration.setConfigurationName(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setPaymentProviderPluginId("happyPathPaymentProviderPlugin");
		configuration.setPaymentConfigurationData(set);

		paymentProviderConfigurationService.saveOrUpdate(configuration);

		return configuration;
	}

	private void createAndPersistTestPaymentInstrument(final String guid, final PaymentProviderConfiguration providerConfiguration) {
		final PaymentInstrument paymentInstrument = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT, PaymentInstrument.class);
		paymentInstrument.setGuid(guid);
		paymentInstrument.setPaymentProviderConfiguration(providerConfiguration);
		paymentInstrument.setName("PAYMENT_INSTRUMENT_NAME");
		paymentInstrument.setPaymentInstrumentData(Collections.emptySet());

		paymentInstrumentService.saveOrUpdate(paymentInstrument);
	}

	private Set<PaymentProviderConfigurationData> createTestPaymentProviderConfigurationData(final String configA, final String configB) {
		final PaymentProviderConfigurationData dataA = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION_DATA, PaymentProviderConfigurationData.class);
		dataA.setKey(HappyPathPaymentProviderPlugin.CONFIG_KEY_A);
		dataA.setData(configA);

		final PaymentProviderConfigurationData dataB = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION_DATA, PaymentProviderConfigurationData.class);
		dataB.setKey(HappyPathPaymentProviderPlugin.CONFIG_KEY_B);
		dataB.setData(configB);

		return Stream.of(dataA, dataB).collect(Collectors.toSet());
	}
}
