/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.email.handler.order.helper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.domain.impl.EmailPropertiesImpl;
import com.elasticpath.service.email.EmailAddressesExtractionStrategy;

/**
 * Tests of {@link ReturnExchangeEmailPropertyHelperImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReturnExchangeEmailPropertyHelperImplTest {

	private static final String STORE_CODE = "testStoreCode";
	private static final String RECIPIENT_ADDRESSES = "testRecipientAddresses1,testRecipientAddresses2";
	private static final Locale LOCALE = Locale.CANADA;

	@InjectMocks
	private ReturnExchangeEmailPropertyHelperImpl target;

	@Mock
	private OrderReturn orderReturn;

	@Mock
	private Order orderOfOrderReturn;

	@Mock
	private EmailAddressesExtractionStrategy emailAddressesExtractionStrategy;

	@Mock
	private BeanFactory beanFactory;

	@Before
	public void setUp() {

		// when
		when(orderReturn.getOrder()).thenReturn(orderOfOrderReturn);
		when(orderReturn.getPhysicalReturn()).thenReturn(true);
		when(orderOfOrderReturn.getLocale()).thenReturn(LOCALE);
		when(orderOfOrderReturn.getStoreCode()).thenReturn(STORE_CODE);
		when(emailAddressesExtractionStrategy.extractToInline(orderOfOrderReturn)).thenReturn(RECIPIENT_ADDRESSES);
		when(beanFactory.getPrototypeBean(ContextIdNames.EMAIL_PROPERTIES, EmailProperties.class))
				.thenAnswer(invocationOnMock -> new EmailPropertiesImpl());
	}

	@Test
	public void testGetOrderReturnEmailProperties() {

		// then
		final EmailProperties emailProperties = target.getOrderReturnEmailProperties(orderReturn);

		// verify
		assertThat(emailProperties.getTemplateResources()).contains(entry("orderReturn", orderReturn), entry("locale", LOCALE));
		assertThat(emailProperties.getDefaultSubject()).isEqualTo("Order Return Confirmation");
		assertThat(emailProperties.getLocaleDependentSubjectKey()).isEqualTo("RMA.emailSubject");
		assertThat(emailProperties.getEmailLocale()).isEqualTo(LOCALE);
		assertThat(emailProperties.getStoreCode()).isEqualTo(STORE_CODE);
		assertThat(emailProperties.getRecipientAddress()).isEqualTo(RECIPIENT_ADDRESSES);
		assertThat(emailProperties.getHtmlTemplate()).isEqualTo("RMA.html");
		assertThat(emailProperties.getTextTemplate()).isEqualTo("RMA.txt");

	}

	@Test
	public void testGetOrderReturnEmailPropertiesWithNonPhysicalSku() {

		// when
		when(orderReturn.getPhysicalReturn()).thenReturn(false);

		// then
		final EmailProperties emailProperties = target.getOrderReturnEmailProperties(orderReturn);

		// verify
		assertThat(emailProperties.getHtmlTemplate()).isEqualTo("GRMA.html");
		assertThat(emailProperties.getTextTemplate()).isEqualTo("GRMA.txt");

	}

}
