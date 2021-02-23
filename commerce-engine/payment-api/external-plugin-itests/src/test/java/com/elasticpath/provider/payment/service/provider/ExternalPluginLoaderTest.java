/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Condition;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.strategy.SelfFirstStrategy;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;
import com.elasticpath.test.integration.BasicSpringContextTest;

public class ExternalPluginLoaderTest extends BasicSpringContextTest {

	private static final String EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME = "externalTestPaymentPlugin";

	@Autowired
	private ExternalPluginLoader externalPluginLoader;

	@BeforeClass
	public static void setUpLibJarHandler() {
        LibJarHandlerInitializer.initialize();
	}

    @After
	public void tearDown() {
		PaymentProviderPluginForIntegrationTesting.resetCapabilities();
	}

	@Test
	public void checkLoadedExternalBeans() throws IOException {
		final Map<String, PaymentProviderPlugin> externalPlugins = externalPluginLoader.load(PaymentProviderPlugin.class);
		assertThat(externalPlugins.keySet()).contains(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME);
	}

	@Test
	public void checkLibrariesAreLoaded() throws IOException {
		final Map<String, PaymentProviderPlugin> externalPlugins = externalPluginLoader.load(PaymentProviderPlugin.class);
		final PaymentProviderPlugin externalTestPaymentPlugin = externalPlugins.get(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME);
		final List<URL> loadedURLs = ImmutableList.copyOf(((ClassRealm) externalTestPaymentPlugin.getClassLoader()).getURLs());

		assertThat(loadedURLs)
				.extracting(URL::getProtocol)
				.containsOnly("lib-jar", "file");
		assertThat(loadedURLs)
				.filteredOn(url -> url.getProtocol().equals("file"))
				.extracting(URL::getFile)
				.areExactly(1, new Condition<String>() {
					@Override
					public boolean matches(final String value) {
						return value.matches(".*external-test-plugin.*\\.jar");
					}
				});
		assertThat(loadedURLs)
				.filteredOn(url -> url.getProtocol().equals("lib-jar"))
				.extracting(URL::getFile)
				.areExactly(1, new Condition<String>() {
					@Override
					public boolean matches(final String value) {
						return value.matches("lib/spring-context.*\\.jar");
					}
				});
	}

	@Test
	public void checkPluginClassLoaderClassVisibility() throws IOException, ClassNotFoundException {
		final Map<String, PaymentProviderPlugin> externalPlugins = externalPluginLoader.load(PaymentProviderPlugin.class);
		final PaymentProviderPlugin externalTestPaymentPlugin = externalPlugins.get(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME);
		final ClassRealm pluginClassLoader = (ClassRealm) externalTestPaymentPlugin.getClassLoader();

		try {
			pluginClassLoader.loadClass(PaymentProviderService.class.getName());
			fail("Plugin ClassLoader should not be able to load any ep-commerce core classes");
		} catch (ClassNotFoundException expected) {
			// expected
		}

		pluginClassLoader.loadClass(ReserveCapability.class.getName()); // check connectivity classes are visible
		pluginClassLoader.loadClass(ImmutableList.class.getName()); // check library classes are visible

		assertThat(pluginClassLoader.getStrategy()).isInstanceOf(SelfFirstStrategy.class);
	}

	@Test
	public void checkPluginMethodProxying() throws Exception {
		final Map<String, PaymentProviderPlugin> externalPlugins = externalPluginLoader.load(PaymentProviderPlugin.class);
		final PaymentProviderPlugin externalTestPaymentPlugin = externalPlugins.get(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME);

		setCallback(externalTestPaymentPlugin, new AbstractPaymentProviderPlugin() {
			@Override
			public String getPaymentVendorId() {
				return null;
			}

			@Override
			public String getPaymentMethodId() {
				return null;
			}

			@Override
			public List<PluginConfigurationKey> getConfigurationKeys() {
				assertThat(Thread.currentThread().getContextClassLoader()).isInstanceOf(ClassRealm.class);
				return Collections.emptyList();
			}
		});
		externalTestPaymentPlugin.getConfigurationKeys();
		assertThat(Thread.currentThread().getContextClassLoader()).isNotInstanceOf(ClassRealm.class);
	}

	@Test
	public void checkExceptionProxying() throws Exception {
		final Map<String, PaymentProviderPlugin> externalPlugins = externalPluginLoader.load(PaymentProviderPlugin.class);
		final PaymentProviderPlugin externalTestPaymentPlugin = externalPlugins.get(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME);

		setCallback(externalTestPaymentPlugin, new AbstractPaymentProviderPlugin() {
			@Override
			public String getPaymentVendorId() {
				return null;
			}

			@Override
			public String getPaymentMethodId() {
				return null;
			}

			@Override
			public List<PluginConfigurationKey> getConfigurationKeys() {
				throw new IllegalStateException("Original exception");
			}
		});
		try {
			externalTestPaymentPlugin.getConfigurationKeys();
		} catch (IllegalStateException e) {
			assertThat(e.getMessage()).isEqualTo("Original exception");
		}
	}

	private void setCallback(final PaymentProviderPlugin plugin, final PaymentProviderPlugin callback) {
		try {
			plugin.getClassLoader()
					.loadClass("com.elastipath.payment.provider.test.external.ExternalTestPaymentPlugin")
					.getDeclaredMethod("setCallback", PaymentProviderPlugin.class)
					.invoke(null, callback);
		} catch (Exception e) {
			fail("Cannot set real test plugin into external one", e);
		}
	}

}
