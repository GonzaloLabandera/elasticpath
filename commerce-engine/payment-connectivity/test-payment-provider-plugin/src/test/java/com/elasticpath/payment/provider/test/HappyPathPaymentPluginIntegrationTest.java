/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.payment.provider.test;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.elasticpath.plugin.payment.provider.AbstractPaymentPluginTest;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
public class HappyPathPaymentPluginIntegrationTest extends AbstractPaymentPluginTest {

	private final HappyPathPaymentProviderPlugin plugin = new HappyPathPaymentProviderPlugin();

	@Override
	protected PaymentProviderPlugin getPlugin() {
		return plugin;
	}

	@Override
	protected Map<String, String> populatePICForm(final PaymentInstrumentCreationFields fields, final PICInstructions instructions) {
		return fields.getFields()
				.stream()
				.collect(Collectors.toMap(identity(), field -> field + " Value"));
	}

	@Override
	protected Map<String, String> populatePICInstructionsForm(final PICInstructionsFields fields) {
		return fields.getFields()
				.stream()
				.collect(Collectors.toMap(identity(), field -> field + " Value"));
	}

	@Override
	protected String pluginConfigData(final PluginConfigurationKey key) {
		return key.getKey() + " Value";
	}

	@Test
	public void checkPaymentInstrumentContainsDetails() {
		assertThat(getPaymentInstrument().getDetails()).containsKeys("details");
	}

	@Test
	public void checkReserve() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		assertThat(reservationResponse.getData()).isEqualToComparingFieldByField(getPluginConfigData());
	}

	@Test
	public void checkCancel() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse cancelResponse = cancel(reservationResponse, getReservationAmount());
		assertThat(cancelResponse.getData()).isEqualToComparingFieldByField(getPluginConfigData());
	}

	@Test
	public void checkModify() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse modificationResponse = modify(reservationResponse, getModifiedAmount());
		assertThat(modificationResponse.getData()).isEqualToComparingFieldByField(getPluginConfigData());
	}

	@Test
	public void checkCharge() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse chargeResponse = charge(reservationResponse, getChargeAmount());
		assertThat(chargeResponse.getData()).isEqualToComparingFieldByField(getPluginConfigData());
	}

	@Test
	public void checkCredit() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse chargeResponse = charge(reservationResponse, getChargeAmount());
		final PaymentCapabilityResponse creditResponse = credit(chargeResponse, getCreditAmount());
		assertThat(creditResponse.getData()).isEqualToComparingFieldByField(getPluginConfigData());
	}

	@Test
	public void checkReverseCharge() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse chargeResponse = charge(reservationResponse, getChargeAmount());
		final PaymentCapabilityResponse reverseChargeResponse = reverseCharge(chargeResponse);
		assertThat(reverseChargeResponse.getData()).isEqualToComparingFieldByField(getPluginConfigData());
	}

}
