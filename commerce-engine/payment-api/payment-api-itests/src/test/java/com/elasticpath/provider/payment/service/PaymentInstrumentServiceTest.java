/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.PaymentInstrumentData;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Integration test for {@link PaymentInstrumentService}.
 */
public class PaymentInstrumentServiceTest extends DbTestCase {

	private static final String BILLING_GIUD = "BILLING_GIUD";

	@Autowired
	private PaymentInstrumentService testee;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	/**
	 * Verifies that created entity can be found by its GUID.
	 */
	@Test
	@DirtiesDatabase
	public void verifyFindByGuid() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.findByGuid(paymentInstrument.getGuid());
		assertEquals(paymentInstrument.getName(), persistedInstrument.getName());
		assertEquals(configuration, persistedInstrument.getPaymentProviderConfiguration());
	}

	/**
	 * Verifies that <code>PaymentInstrumentData</code> can be added before creating an entity.
	 */
	@Test
	@DirtiesDatabase
	public void verifyAddingDataToNewEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		paymentInstrument.setPaymentInstrumentData(createTestPaymentInstrumentData());
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		verifyPaymentInstrumentData(persistedInstrument);
	}

	/**
	 * Verifies that <code>PaymentInstrumentData</code> can be added to persisted entity.
	 */
	@Test
	@DirtiesDatabase
	public void verifyAddingDataToPersistedEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		testee.saveOrUpdate(paymentInstrument);

		paymentInstrument.setPaymentInstrumentData(createTestPaymentInstrumentData());
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		verifyPaymentInstrumentData(persistedInstrument);
	}

	/**
	 * Verify entity can be removed.
	 */
	@Test
	@DirtiesDatabase
	public void verifyRemoval() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		testee.saveOrUpdate(paymentInstrument);

		testee.remove(paymentInstrument);

		assertNull(testee.get(paymentInstrument.getUidPk()));
	}

	@Test
	@DirtiesDatabase
	public void verifyAddingNotNullBillingAddressToNewEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		assertEquals(paymentInstrument.getBillingAddressGuid(), persistedInstrument.getBillingAddressGuid());
	}

	@Test
	@DirtiesDatabase
	public void verifySingleReservePerPIAndSupportsMultiChargesValuesInNewEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		assertEquals(paymentInstrument.isSingleReservePerPI(), persistedInstrument.isSingleReservePerPI());
		assertEquals(paymentInstrument.isSupportingMultiCharges(), persistedInstrument.isSupportingMultiCharges());
	}

	@Test
	@DirtiesDatabase
	public void verifyAddingNullBillingAddressToNewEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		paymentInstrument.setBillingAddressGuid(null);
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		assertEquals(paymentInstrument.getBillingAddressGuid(), persistedInstrument.getBillingAddressGuid());
	}

	@Test
	@DirtiesDatabase
	public void testEditAddressToPersistedEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		testee.saveOrUpdate(paymentInstrument);

		paymentInstrument.setBillingAddressGuid("EDITED_BILLING_GIUD");
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		assertEquals(paymentInstrument.getBillingAddressGuid(), persistedInstrument.getBillingAddressGuid());
	}

	@Test
	@DirtiesDatabase
	public void testSetNullAddressToPersistedEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		testee.saveOrUpdate(paymentInstrument);
		paymentInstrument.setBillingAddressGuid(null);
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		assertThat(persistedInstrument).isNotNull();
		assertThat(persistedInstrument.getBillingAddressGuid()).isNull();
	}

	@Test
	@DirtiesDatabase
	public void testSetNotNullAddressToPersistedEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		paymentProviderConfigurationService.saveOrUpdate(configuration);

		PaymentInstrument paymentInstrument = createTestPaymentInstrument(configuration);
		paymentInstrument.setBillingAddressGuid(null);
		testee.saveOrUpdate(paymentInstrument);
		paymentInstrument.setBillingAddressGuid(BILLING_GIUD);
		testee.saveOrUpdate(paymentInstrument);

		PaymentInstrument persistedInstrument = testee.get(paymentInstrument.getUidPk());
		assertThat(persistedInstrument).isNotNull();
		assertEquals(BILLING_GIUD, persistedInstrument.getBillingAddressGuid());
	}

	private PaymentInstrument createTestPaymentInstrument(final PaymentProviderConfiguration configuration) {
		PaymentInstrument paymentInstrument = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT, PaymentInstrument.class);
		paymentInstrument.setPaymentProviderConfiguration(configuration);
		paymentInstrument.setName(Utils.uniqueCode("PAYMENT_INSTRUMENT_NAME"));
		paymentInstrument.setPaymentInstrumentData(Collections.emptySet());
		paymentInstrument.setBillingAddressGuid(BILLING_GIUD);
		paymentInstrument.setSingleReservePerPI(false);
		paymentInstrument.setSupportingMultiCharges(false);
		return paymentInstrument;
	}

	private PaymentProviderConfiguration createTestPaymentProviderConfiguration() {
		PaymentProviderConfiguration configuration = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class);
		configuration.setConfigurationName(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setPaymentProviderPluginId(Utils.uniqueCode("PAYMENT_PROVIDER_PLUGIN_ID"));
		configuration.setPaymentConfigurationData(Collections.emptySet());
		return configuration;
	}

	private Set<PaymentInstrumentData> createTestPaymentInstrumentData() {
		PaymentInstrumentData dataA = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT_DATA, PaymentInstrumentData.class);
		dataA.setKey("a-key");
		dataA.setData("a-data");

		PaymentInstrumentData dataB = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT_DATA, PaymentInstrumentData.class);
		dataB.setKey("b-key");
		dataB.setData("b-data");

		return Stream.of(dataA, dataB).collect(Collectors.toSet());
	}

	private void verifyPaymentInstrumentData(final PaymentInstrument paymentInstrument) {
		Set<PaymentInstrumentData> paymentInstrumentData = paymentInstrument.getPaymentInstrumentData();
		assertNotNull("Configuration data is null", paymentInstrumentData);
		Map<String, String> data = paymentInstrumentData.stream()
				.collect(Collectors.toMap(PaymentInstrumentData::getKey, PaymentInstrumentData::getData));
		assertThat(data.keySet(), hasItems("a-key", "b-key"));
		assertThat(data.values(), hasItems("a-data", "b-data"));
	}

}
