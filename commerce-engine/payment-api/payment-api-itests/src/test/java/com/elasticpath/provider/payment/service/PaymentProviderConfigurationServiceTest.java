/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.provider.payment.PaymentLocalizedProperties;
import com.elasticpath.provider.payment.PaymentLocalizedPropertiesImpl;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValue;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValueImpl;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.domain.impl.PaymentProviderConfigurationImpl;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Integration test for {@link PaymentProviderConfigurationService}.
 */
public class PaymentProviderConfigurationServiceTest extends DbTestCase {

	private static final String NAME_FR = "GST (Le Canada)";

	@Autowired
	private PaymentProviderConfigurationService testee;

	/**
	 * Verifies that created entity can be found by its GUID.
	 */
	@Test
	@DirtiesDatabase
	public void verifyFindByGuid() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();

		testee.saveOrUpdate(configuration);

		PaymentProviderConfiguration persistedConfiguration = testee.findByGuid(configuration.getGuid());
		assertEquals(configuration.getConfigurationName(), persistedConfiguration.getConfigurationName());
		assertEquals(configuration.getPaymentProviderPluginId(), persistedConfiguration.getPaymentProviderPluginId());
		assertEquals(PaymentProviderConfigurationStatus.DRAFT, persistedConfiguration.getStatus());
	}

	/**
	 * Verifies that created entity can be found by its GUID.
	 */
	@Test
	@DirtiesDatabase
	public void verifyFindByGuidList() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();

		testee.saveOrUpdate(configuration);

		List<PaymentProviderConfiguration> persistedConfigurations = testee.findByGuids(Collections.singletonList(configuration.getGuid()));
		assertEquals(1, persistedConfigurations.size());
		assertEquals(configuration.getConfigurationName(), persistedConfigurations.get(0).getConfigurationName());
		assertEquals(configuration.getPaymentProviderPluginId(), persistedConfigurations.get(0).getPaymentProviderPluginId());
		assertEquals(PaymentProviderConfigurationStatus.DRAFT, persistedConfigurations.get(0).getStatus());
	}

	/**
	 * Verifies that created entity can be found by {@link PaymentProviderConfigurationStatus}.
	 */
	@Test
	@DirtiesDatabase
	public void verifyFindByStatus() {
		PaymentProviderConfiguration configuration1 = createTestPaymentProviderConfiguration();
		PaymentProviderConfiguration configuration2 = createTestPaymentProviderConfiguration();
		configuration1.setStatus(PaymentProviderConfigurationStatus.ACTIVE);
		configuration2.setStatus(PaymentProviderConfigurationStatus.ACTIVE);
		testee.saveOrUpdate(configuration1);
		testee.saveOrUpdate(configuration2);

		List<PaymentProviderConfiguration> persistedConfigurations = testee.findByStatus(PaymentProviderConfigurationStatus.ACTIVE);
		assertEquals(2, persistedConfigurations.size());
		assertEquals(PaymentProviderConfigurationStatus.ACTIVE, persistedConfigurations.get(0).getStatus());
		assertEquals(PaymentProviderConfigurationStatus.ACTIVE, persistedConfigurations.get(1).getStatus());
	}

	/**
	 * Verifies that <code>PaymentConfigurationData</code> can be added before creating an entity.
	 */
	@Test
	@DirtiesDatabase
	public void verifyAddingDataToNewEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		configuration.setPaymentConfigurationData(createTestPaymentProviderConfigurationData());
		testee.saveOrUpdate(configuration);

		PaymentProviderConfiguration persistedConfiguration = testee.get(configuration.getUidPk());
		verifyConfigurationData(persistedConfiguration);
	}

	/**
	 * Verifies that <code>PaymentLocalizedProperties</code> can be added before creating an entity.
	 */
	@Test
	@DirtiesDatabase
	public void test() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		configuration.setPaymentLocalizedProperties(createTestPaymentProviderConfigurationLocalizedProperties());
		testee.saveOrUpdate(configuration);

		PaymentProviderConfiguration persistedConfiguration = testee.get(configuration.getUidPk());
		verifyConfigurationLocalizedProperties(persistedConfiguration, configuration);
	}

	private void verifyConfigurationLocalizedProperties(final PaymentProviderConfiguration configuration,
														final PaymentProviderConfiguration paymentProviderConfiguration) {
		assertEquals(configuration.getPaymentLocalizedProperties().getPaymentLocalizedPropertiesMap(),
				paymentProviderConfiguration.getPaymentLocalizedProperties().getPaymentLocalizedPropertiesMap());
	}

	private PaymentLocalizedProperties createTestPaymentProviderConfigurationLocalizedProperties() {
		final PaymentLocalizedPropertiesImpl paymentLocalizedProperties = new PaymentLocalizedPropertiesImpl() {
			@Override
			protected PaymentLocalizedPropertyValue getNewPaymentLocalizedPropertyValue() {
				return new PaymentLocalizedPropertyValueImpl();
			}
		};
		paymentLocalizedProperties.setValue(PaymentProviderConfigurationImpl.LOCALIZED_PROPERTY_DISPLAY_NAME, Locale.FRANCE, NAME_FR);
		return paymentLocalizedProperties;
	}

	/**
	 * Verifies that <code>PaymentConfigurationData</code> can be added to persisted entity.
	 */
	@Test
	@DirtiesDatabase
	public void verifyAddingDataToPersistedEntity() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		testee.saveOrUpdate(configuration);

		configuration.setPaymentConfigurationData(createTestPaymentProviderConfigurationData());
		configuration.setStatus(PaymentProviderConfigurationStatus.ACTIVE);
		testee.saveOrUpdate(configuration);

		PaymentProviderConfiguration persistedConfiguration = testee.get(configuration.getUidPk());
		verifyConfigurationData(persistedConfiguration);
		assertEquals(configuration.getStatus(), persistedConfiguration.getStatus());
	}

	/**
	 * Verify entity can be removed.
	 */
	@Test
	@DirtiesDatabase
	public void verifyRemoval() {
		PaymentProviderConfiguration configuration = createTestPaymentProviderConfiguration();
		configuration.setPaymentConfigurationData(createTestPaymentProviderConfigurationData());
		testee.saveOrUpdate(configuration);

		testee.remove(configuration);

		assertNull(testee.get(configuration.getUidPk()));
	}

	private PaymentProviderConfiguration createTestPaymentProviderConfiguration() {
		PaymentProviderConfiguration configuration = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class);
		configuration.setConfigurationName(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setPaymentProviderPluginId(Utils.uniqueCode("PAYMENT_PROVIDER_PLUGIN_ID"));
		configuration.setPaymentConfigurationData(Collections.emptySet());
		return configuration;
	}

	private Set<PaymentProviderConfigurationData> createTestPaymentProviderConfigurationData() {
		PaymentProviderConfigurationData dataA = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION_DATA, PaymentProviderConfigurationData.class);
		dataA.setKey("a-key");
		dataA.setData("a-data");

		PaymentProviderConfigurationData dataB = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION_DATA, PaymentProviderConfigurationData.class);
		dataB.setKey("b-key");
		dataB.setData("b-data");

		return Stream.of(dataA, dataB).collect(Collectors.toSet());
	}

	private void verifyConfigurationData(final PaymentProviderConfiguration persistedConfiguration) {
		Set<PaymentProviderConfigurationData> paymentConfigurationData = persistedConfiguration.getPaymentConfigurationData();
		assertNotNull("Configuration data is null", paymentConfigurationData);
		Map<String, String> data = paymentConfigurationData.stream()
				.collect(Collectors.toMap(PaymentProviderConfigurationData::getKey, PaymentProviderConfigurationData::getData));
		assertThat(data.keySet(), hasItems("a-key", "b-key"));
		assertThat(data.values(), hasItems("a-data", "b-data"));
	}

}