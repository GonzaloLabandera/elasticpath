/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.payment.provider.test;

import static com.elasticpath.payment.provider.test.AbstractSmartPathPaymentProviderPlugin.CapabilityConfig.FAILS_2ND_TIME;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.InjectMocks;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;

public class SmartPathPaymentProviderPluginImplTest extends AbstractSmartPathPaymentProviderPluginTest {

	@InjectMocks
	private SmartPathPaymentProviderPluginImpl testee;

	@Override
	protected AbstractSmartPathPaymentProviderPlugin getTestee() {
		return testee;
	}

	@Test
	public void testSupportsAllCapabilities() {
		assertThat(getTestee().getCapability(PICClientInteractionRequestCapability.class)).isPresent();
		assertThat(getTestee().getCapability(ReserveCapability.class)).isPresent();
		assertThat(getTestee().getCapability(ModifyCapability.class)).isPresent();
		assertThat(getTestee().getCapability(CreditCapability.class)).isPresent();
		assertThat(getTestee().getCapability(CancelCapability.class)).isPresent();
		assertThat(getTestee().getCapability(ChargeCapability.class)).isPresent();
		assertThat(getTestee().getCapability(ReverseChargeCapability.class)).isPresent();
	}

	@Test
	public void failingCapabilityFails2ndTimePerTransactionType() throws PaymentCapabilityRequestFailedException {
		final ImmutableMap<String, String> configData = ImmutableMap.of(
				RESERVE.name(), FAILS_2ND_TIME.name(),
				CHARGE.name(), FAILS_2ND_TIME.name()
		);
		final Optional<ReserveCapability> reserveCapability = getTestee().getCapability(ReserveCapability.class);
		final Optional<ChargeCapability> chargeCapability = getTestee().getCapability(ChargeCapability.class);
		final ReserveCapabilityRequest reserveCapabilityRequest = mock(ReserveCapabilityRequest.class);
		final OrderContext orderContext = mock(OrderContext.class);
		when(orderContext.getOrderNumber()).thenReturn("order-number");
		when(reserveCapabilityRequest.getPluginConfigData()).thenReturn(configData);
		when(reserveCapabilityRequest.getOrderContext()).thenReturn(orderContext);
		final ChargeCapabilityRequest chargeCapabilityRequest = mock(ChargeCapabilityRequest.class);
		when(chargeCapabilityRequest.getPluginConfigData()).thenReturn(configData);
		when(chargeCapabilityRequest.getOrderContext()).thenReturn(orderContext);

		if (reserveCapability.isPresent() && chargeCapability.isPresent()) {
			reserveCapability.get().reserve(reserveCapabilityRequest);
			chargeCapability.get().charge(chargeCapabilityRequest);

			boolean hasFailed2ndTime = false;
			try {
				reserveCapability.get().reserve(reserveCapabilityRequest);
			} catch (PaymentCapabilityRequestFailedException expected) {
				hasFailed2ndTime = true;
			}
			if (!hasFailed2ndTime) {
				fail("Reserve capability must fail second time");
			}

			hasFailed2ndTime = false;
			try {
				chargeCapability.get().charge(chargeCapabilityRequest);
			} catch (PaymentCapabilityRequestFailedException expected) {
				hasFailed2ndTime = true;
			}
			if (!hasFailed2ndTime) {
				fail("Reserve capability must fail second time");
			}
		} else {
			fail("Reserve and Charge capabilities must be supported");
		}
	}

}
