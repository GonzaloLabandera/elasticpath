/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.payment.provider.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;

import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;

public class SmartPathNoReservePaymentProviderPluginImplTest extends AbstractSmartPathPaymentProviderPluginTest {

	@InjectMocks
	private SmartPathReserveUnsupportedPaymentProviderPluginImpl testee;

	@Override
	protected AbstractSmartPathPaymentProviderPlugin getTestee() {
		return testee;
	}

	@Test
	public void testDoesNotSupportReserveCapability() {
		assertThat(getTestee().getCapability(ReserveCapability.class)).isNotPresent();
	}

	@Test
	public void testSupportsAllOtherCapabilities() {
		assertThat(getTestee().getCapability(PICClientInteractionRequestCapability.class)).isPresent();
		assertThat(getTestee().getCapability(CreditCapability.class)).isPresent();
		assertThat(getTestee().getCapability(ChargeCapability.class)).isPresent();
		assertThat(getTestee().getCapability(ReverseChargeCapability.class)).isPresent();
	}

}