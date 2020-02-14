/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import static org.hamcrest.Matchers.greaterThan;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.test.integration.BasicSpringContextTest;

/**
 * Payment provider plugin Spring context test.
 */
public class PaymentProviderPluginSpringContextTest extends BasicSpringContextTest {

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Checks that plugin is defined as prototype.
	 */
	@Test
	public void testBeanFactoryKnowsAboutPluginAsPrototype() {
		Assert.assertNotNull("Happy Path plugin bean was not loaded",
				getBeanFactory().getPrototypeBean("happyPathPaymentProviderPlugin", PaymentProviderPlugin.class));
	}

	/**
	 * Checks that plugin is declared in Spring application context.
	 */
	@Test
	public void testApplicationContextKnowsAboutPlugin() {
		Map<String, PaymentProviderPlugin> plugins = applicationContext.getBeansOfType(PaymentProviderPlugin.class);
		Assert.assertThat("No plugins were loaded", plugins.size(), greaterThan(0));
		Assert.assertNotNull("Happy Path plugin was not loaded", plugins.get("happyPathPaymentProviderPlugin"));
	}

}
