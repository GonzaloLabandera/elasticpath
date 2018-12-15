/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.email.producer.spi.composer.util.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.WebConstants;
import com.elasticpath.commons.util.impl.StoreThemeMessageSource;
import com.elasticpath.domain.catalogview.impl.SeoUrlBuilderImpl;
import com.elasticpath.domain.misc.Geography;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.email.domain.impl.EmailPropertiesImpl;
import com.elasticpath.money.MoneyFormatter;

/**
 * Unit test for {@link EmailContextFactoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailContextFactoryImplTest {

	private EmailContextFactoryImpl contextFactory;
	private StoreImpl store;
	private EmailPropertiesImpl emailProperties;

	@Mock
	private StoreThemeMessageSource storeThemeMessageSource;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private MoneyFormatter moneyFormatter;

	@Mock
	private Geography geography;

	@Before
	public void setUp() {

		when(beanFactory.getBean(ContextIdNames.SEO_URL_BUILDER)).thenReturn(new SeoUrlBuilderImpl());

		contextFactory = new EmailContextFactoryImpl();
		contextFactory.setBeanFactory(beanFactory);
		contextFactory.setGeography(geography);
		contextFactory.setMoneyFormatter(moneyFormatter);
		contextFactory.setStoreThemeMessageSource(storeThemeMessageSource);

		store = new StoreImpl();
		store.setCode("store");

		emailProperties = new EmailPropertiesImpl();
	}

	/**
	 * testCreateVelocityContextInjectsServices.
	 */
	@Test
	public void testCreateVelocityContextInjectsServices() {
		Map<String, Object> context = contextFactory.createVelocityContext(store, emailProperties);

		assertThat(context.get(WebConstants.MONEY_FORMATTER))
			.as("Context should be created with spring-injected services")
			.isSameAs(moneyFormatter);
		assertThat(context.get(WebConstants.GEOGRAPHY_HELPER))
			.as("Geography should be on the map")
			.isNotNull();
	}

	/**
	 * testCreateVelocityContextInjectsStoreInfo.
	 */
	@Test
	public void testCreateVelocityContextInjectsStoreInfo() {
		//  Given
		emailProperties.setStoreCode(store.getCode());
		store.setUrl("/snap-it-up/");

		//  When
		Map<String, Object> context = contextFactory.createVelocityContext(store, emailProperties);

		//  Then
		assertThat(context.get("store"))
			.as("Context should include store")
			.isSameAs(store);
		assertThat(context.get("baseImgUrl"))
			.as("Context should include store url")
			.isEqualTo(store.getUrl());
		assertThat(context.get("storeUrl"))
			.as("Context should include trimmed store url")
			.isEqualTo("/snap-it-up");
	}
}
