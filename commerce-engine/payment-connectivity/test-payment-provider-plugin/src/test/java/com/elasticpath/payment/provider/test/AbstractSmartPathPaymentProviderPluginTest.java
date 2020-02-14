/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.payment.provider.test;

import static com.elasticpath.payment.provider.test.AbstractSmartPathPaymentProviderPlugin.CapabilityConfig.FAILS;
import static com.elasticpath.payment.provider.test.AbstractSmartPathPaymentProviderPlugin.CapabilityConfig.FAILS_2ND_TIME;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractSmartPathPaymentProviderPluginTest {

	protected abstract AbstractSmartPathPaymentProviderPlugin getTestee();

	@Before
	public void setUp() {
		AbstractSmartPathPaymentProviderPlugin.clearCurrentReferenceNumberPerTransactionForTest();
	}

	@Test
	public void noValueDefaultsToSuccess() throws PaymentCapabilityRequestFailedException {
		final Optional<ChargeCapability> capability = getTestee().getCapability(ChargeCapability.class);
		if (capability.isPresent()) {
			final ChargeCapabilityRequest request = mock(ChargeCapabilityRequest.class);
			when(request.getPluginConfigData()).thenReturn(ImmutableMap.of());
			capability.get().charge(request);
		}
	}

	@Test
	public void emptyValueDefaultsToSuccess() throws PaymentCapabilityRequestFailedException {
		final Optional<ChargeCapability> capability = getTestee().getCapability(ChargeCapability.class);
		if (capability.isPresent()) {
			final ChargeCapabilityRequest request = mock(ChargeCapabilityRequest.class);
			when(request.getPluginConfigData()).thenReturn(ImmutableMap.of(CHARGE.name(), ""));
			capability.get().charge(request);
		}
	}

	@Test(expected = PaymentCapabilityRequestFailedException.class)
	public void failingCapability() throws PaymentCapabilityRequestFailedException {
		final Optional<ChargeCapability> capability = getTestee().getCapability(ChargeCapability.class);
		if (capability.isPresent()) {
			final ChargeCapabilityRequest request = mock(ChargeCapabilityRequest.class);
			when(request.getPluginConfigData()).thenReturn(ImmutableMap.of(CHARGE.name(), FAILS.name()));
			capability.get().charge(request);
		} else {
			fail("Capability must be supported");
		}
	}

	@Test
	public void failingCapabilityFails2ndTime() throws PaymentCapabilityRequestFailedException {
		final Optional<ChargeCapability> capability = getTestee().getCapability(ChargeCapability.class);
		if (capability.isPresent()) {
			final ChargeCapabilityRequest request = mock(ChargeCapabilityRequest.class);
			final OrderContext orderContext = mock(OrderContext.class);
			when(orderContext.getOrderNumber()).thenReturn("order-number");
			when(request.getOrderContext()).thenReturn(orderContext);
			when(request.getPluginConfigData()).thenReturn(ImmutableMap.of(CHARGE.name(), FAILS_2ND_TIME.name()));
			checkChargeFails2ndTime(capability.get(), request);
		} else {
			fail("Capability must be supported");
		}
	}

	@Test
	public void failingCapabilityFails2ndTimePerReferenceNumber() throws PaymentCapabilityRequestFailedException {
		final Optional<ChargeCapability> capability = getTestee().getCapability(ChargeCapability.class);
		if (capability.isPresent()) {
			final ChargeCapabilityRequest request = mock(ChargeCapabilityRequest.class);
			when(request.getPluginConfigData()).thenReturn(ImmutableMap.of(CHARGE.name(), FAILS_2ND_TIME.name()));
			final OrderContext orderContext = mock(OrderContext.class);
			when(request.getOrderContext()).thenReturn(orderContext);

			when(orderContext.getOrderNumber()).thenReturn("order-number-1");
			capability.get().charge(request);

			when(orderContext.getOrderNumber()).thenReturn("order-number-2");
			checkChargeFails2ndTime(capability.get(), request);
		} else {
			fail("Capability must be supported");
		}
	}

	private void checkChargeFails2ndTime(final ChargeCapability chargeCapability,
										 final ChargeCapabilityRequest request)
			throws PaymentCapabilityRequestFailedException {
		chargeCapability.charge(request);
		boolean hasFailed2ndTime = false;
		try {
			chargeCapability.charge(request);
		} catch (PaymentCapabilityRequestFailedException expected) {
			hasFailed2ndTime = true;
		}
		if (!hasFailed2ndTime) {
			fail("Capability must fail second time");
		}
	}

}