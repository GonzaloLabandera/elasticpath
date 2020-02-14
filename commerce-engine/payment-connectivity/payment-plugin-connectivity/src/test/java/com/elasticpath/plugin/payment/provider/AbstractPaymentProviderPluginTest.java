/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/payment-plugin-connectivity-spring-verification.xml")
@ActiveProfiles("dummyProfile")
public class AbstractPaymentProviderPluginTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNoPluginsWiredByAPI() {
		Map<String, PaymentProviderPlugin> beans = applicationContext.getBeansOfType(PaymentProviderPlugin.class);

		assertThat("No REAL plugins should be implemented in plugin API, use extension modules",
				beans.keySet(), hasItems("dummyPaymentProviderPlugin"));
	}

}