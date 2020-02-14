/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.handlers.libjar.LibJarURLStreamHandlerService;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.test.integration.BasicSpringContextTest;

public class PaymentProviderServiceTest extends BasicSpringContextTest {

	private static final String EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME = "externalTestPaymentPlugin";

	@Autowired
	private PaymentProviderService paymentProviderService;

	private PaymentProviderConfiguration paymentProviderConfiguration;

	@BeforeClass
	public static void setUpLibJarHandler() {
		URL.setURLStreamHandlerFactory(protocol -> {
			if (LibJarURLStreamHandlerService.LIB_JAR_PROTOCOL.equals(protocol)) {
				return new LibJarURLStreamHandler();
			}
			return null;
		});
	}

	@Before
	public void setUp() {
		paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
		when(paymentProviderConfiguration.getPaymentProviderPluginId()).thenReturn(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME);
		when(paymentProviderConfiguration.getPaymentConfigurationData()).thenReturn(Collections.emptySet());
	}

	@Test
	public void getPluginsMustContainBothInternalAndExternalPlugins() {
		final Collection<PaymentProviderPlugin> plugins = paymentProviderService.getPlugins();

		assertThat(plugins).extracting(PaymentProviderPlugin::getUniquePluginId)
				.containsExactlyInAnyOrder(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME, "paymentProviderPluginForIntegrationTesting");
	}

	@Test
	public void checkCapabilityProxying() {
		paymentProviderService.getPlugins().stream()
				.filter(plugin -> plugin.getUniquePluginId().equals(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME))
				.forEach(plugin -> addCapability(plugin, ReserveCapability.class, (ReserveCapability) request -> {
					assertThat(Thread.currentThread().getContextClassLoader()).isInstanceOf(ClassRealm.class);
					return new PaymentCapabilityResponse();
				}));

		final Optional<ReserveCapability> capabilityOptional =
				paymentProviderService.createProvider(paymentProviderConfiguration).getCapability(ReserveCapability.class);
		assertThat(capabilityOptional).isPresent();
		capabilityOptional.ifPresent(reserveCapability -> {
			try {
				reserveCapability.reserve(new ReserveCapabilityRequest());
				assertThat(Thread.currentThread().getContextClassLoader()).isNotInstanceOf(ClassRealm.class);
			} catch (PaymentCapabilityRequestFailedException exception) {
				fail("Capability failed unexpectedly", exception);
			}
		});
	}

	@Test
	public void checkCapabilityExceptionProxying() {
		paymentProviderService.getPlugins().stream()
				.filter(plugin -> plugin.getUniquePluginId().equals(EXTERNAL_TEST_PAYMENT_PLUGIN_BEAN_NAME))
				.forEach(plugin -> addCapability(plugin, ReserveCapability.class, (ReserveCapability) request -> {
					throw new PaymentCapabilityRequestFailedException("Original exception", "Testing exception proxying", true);
				}));

		final Optional<ReserveCapability> capabilityOptional =
				paymentProviderService.createProvider(paymentProviderConfiguration).getCapability(ReserveCapability.class);
		assertThat(capabilityOptional).isPresent();
		capabilityOptional.ifPresent(reserveCapability -> {
			try {
				reserveCapability.reserve(new ReserveCapabilityRequest());
				fail("Reserve capability is expected to throw an exception");
			} catch (PaymentCapabilityRequestFailedException expected) {
				assertThat(expected.getInternalMessage()).isEqualTo("Original exception");
			}
		});
	}

	private void addCapability(final PaymentProviderPlugin plugin,
							   final Class<? extends Capability> capabilityClass,
							   final Capability capability) {
		try {
			plugin.getClassLoader()
					.loadClass("com.elastipath.payment.provider.test.external.ExternalTestPaymentPlugin")
					.getDeclaredMethod("addCapability", Class.class, Capability.class)
					.invoke(null, capabilityClass, capability);
		} catch (Exception e) {
			fail("Cannot set real test plugin into external one", e);
		}
	}

}
