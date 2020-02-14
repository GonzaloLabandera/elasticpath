/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.provider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.impl.PaymentProviderImpl;
import com.elasticpath.provider.payment.service.provider.ExternalPluginLoader;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;

/**
 * Spring support for payment provider configuration service.
 */
public class PaymentProviderServiceImpl implements PaymentProviderService {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ExternalPluginLoader externalPluginLoader;

	private final List<PaymentProviderPlugin> plugins = new ArrayList<>();

	/**
	 * Initializes service capturing all plugins registered via Spring.
	 *
	 * @throws IOException if external plugin bean definition file location cannot be loaded
	 */
	public void init() throws IOException {
		loadInternalPlugins();
		loadExternalPlugins(externalPluginLoader);
	}

	/**
	 * Collects all plugins which are defined and loaded as beans in the core application context.
	 */
	protected void loadInternalPlugins() {
		final Map<String, PaymentProviderPlugin> internalPluginBeans = applicationContext.getBeansOfType(PaymentProviderPlugin.class);
		useBeanNameWithUniquePluginId(internalPluginBeans);
		plugins.addAll(internalPluginBeans.values());
	}

	/**
	 * Finds and loads all plugins which are defined externally in isolated fat jars.
	 *
	 * @param externalPluginLoader external plugin loader
	 * @throws IOException if bean definition {@code payment-plugin.xml} file location cannot be loaded
	 */
	protected void loadExternalPlugins(final ExternalPluginLoader externalPluginLoader) throws IOException {
		final Map<String, PaymentProviderPlugin> externalPluginBeans = externalPluginLoader.load(PaymentProviderPlugin.class);
		useBeanNameWithUniquePluginId(externalPluginBeans);
		plugins.addAll(externalPluginBeans.values());
	}

	/**
	 * Assigns bean name as plugin unique id value. Bean names are assumed to be unique across whole application.
	 *
	 * @param beanMap map of bean id/name to plugin bean
	 */
	protected void useBeanNameWithUniquePluginId(final Map<String, PaymentProviderPlugin> beanMap) {
		for (final Map.Entry<String, PaymentProviderPlugin> beanEntry : beanMap.entrySet()) {
			beanEntry.getValue().setUniquePluginId(beanEntry.getKey());
		}
	}

	@Override
	public Collection<PaymentProviderPlugin> getPlugins() {
		return plugins;
	}

	@Override
	public PaymentProvider createProvider(final PaymentProviderConfiguration configuration) {
		for (PaymentProviderPlugin plugin : plugins) {
			if (plugin.getUniquePluginId().equals(configuration.getPaymentProviderPluginId())) {
				return createPaymentProvider(plugin, configuration);
			}
		}
		throw new IllegalStateException("Plugin with id " + configuration.getPaymentProviderPluginId() + " is missing");
	}

	/**
	 * Creates payment provider on demand one-off instance.
	 * <p/>
	 * Override this method to produce custom payment providers. Note that payment provider is not a bean.
	 *
	 * @param plugin        payment provider plugin
	 * @param configuration payment provider configuration
	 * @return payment provider
	 */
	protected PaymentProvider createPaymentProvider(final PaymentProviderPlugin plugin,
													final PaymentProviderConfiguration configuration) {
		return new PaymentProviderImpl(plugin, configuration);
	}

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	protected ExternalPluginLoader getExternalPluginLoader() {
		return externalPluginLoader;
	}
}
