/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.email.producer.spi.composer.util.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.commons.util.StoreMessageSource;
import com.elasticpath.domain.store.Store;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.domain.impl.EmailPropertiesImpl;
import com.elasticpath.email.producer.spi.composer.util.EmailCompositionConfiguration;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Test class for {@link EmailCompositionConfigurationFactoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailCompositionConfigurationFactoryImplTest {

	@Mock
	private StoreService storeService;

	@Mock
	private SettingsReader settingsReader;

	@Mock
	private StoreMessageSource storeMessageSource;

	@Mock
	private Store store;

	@InjectMocks
	private EmailCompositionConfigurationFactoryImpl factory;

	@Before
	public void setUp() {
		when(settingsReader.getSettingValue(any(String.class)))
				.thenReturn(mock(SettingValue.class));

		when(storeMessageSource.getMessage(any(String.class), any(String.class), any(Locale.class)))
				.thenReturn(null);
	}

	@Test
	public void verifyFactoryCreatesNewInstanceWithStore() throws Exception {
		final String storeCode = "MYSTORE";

		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);

		final EmailProperties emailProperties = new EmailPropertiesImpl();
		emailProperties.setStoreCode(storeCode);

		final EmailCompositionConfiguration emailCompositionConfiguration = factory.create(emailProperties);

		assertThat(emailCompositionConfiguration).isNotNull();
	}

	@Test
	public void verifyFactoryCreatesNewInstanceWithStoreNotFound() throws Exception {
		final String storeCode = "MYSTORE";

		// We have a store code, but no such store exists
		when(storeService.findStoreWithCode(storeCode)).thenReturn(null);

		final EmailProperties emailProperties = new EmailPropertiesImpl();
		emailProperties.setStoreCode(storeCode);

		final EmailCompositionConfiguration emailCompositionConfiguration = factory.create(emailProperties);

		assertThat(emailCompositionConfiguration).isNotNull();
	}

	@Test
	public void verifyFactoryCreatesNewInstanceWithoutStore() throws Exception {
		final EmailProperties emailProperties = new EmailPropertiesImpl();

		// No store code set
		emailProperties.setStoreCode(null);

		final EmailCompositionConfiguration emailCompositionConfiguration = factory.create(emailProperties);

		assertThat(emailCompositionConfiguration).isNotNull();
	}

}