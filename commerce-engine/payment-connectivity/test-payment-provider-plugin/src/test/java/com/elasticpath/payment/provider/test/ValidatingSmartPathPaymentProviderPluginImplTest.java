/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.payment.provider.test;

import static com.elasticpath.payment.provider.test.ValidatingSmartPathPaymentProviderPluginImpl.CANCEL_RESERVATION_DATA;
import static com.elasticpath.payment.provider.test.ValidatingSmartPathPaymentProviderPluginImpl.CHARGE_DATA_KEY;
import static com.elasticpath.payment.provider.test.ValidatingSmartPathPaymentProviderPluginImpl.CREDIT_DATA;
import static com.elasticpath.payment.provider.test.ValidatingSmartPathPaymentProviderPluginImpl.MODIFY_RESERVATION_DATA;
import static com.elasticpath.payment.provider.test.ValidatingSmartPathPaymentProviderPluginImpl.RESERVE_DATA_KEY;
import static com.elasticpath.payment.provider.test.ValidatingSmartPathPaymentProviderPluginImpl.REVERSE_CHARGE_DATA;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.elasticpath.plugin.payment.provider.AbstractPaymentPluginTest;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;

public class ValidatingSmartPathPaymentProviderPluginImplTest extends AbstractPaymentPluginTest {

	private final ValidatingSmartPathPaymentProviderPluginImpl plugin = new ValidatingSmartPathPaymentProviderPluginImpl();

	@Test
	public void testReserveChargeAndCreditDataPropagation() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		assertThat(reservationResponse.getData()).containsKey(RESERVE_DATA_KEY);

		final PaymentCapabilityResponse chargeResponse = charge(reservationResponse, getChargeAmount());
		assertThat(chargeResponse.getData()).containsKey(CHARGE_DATA_KEY);

		final PaymentCapabilityResponse creditResponse = credit(chargeResponse, getCreditAmount());
		assertThat(creditResponse.getData()).containsKey(CREDIT_DATA);
	}

	@Test
	public void testModifyAndCancelDataPropagation() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final MoneyDTO modifiedAmount = getModifiedAmount();

		final PaymentCapabilityResponse modificationResponse = modify(reservationResponse, modifiedAmount);
		assertThat(modificationResponse.getData()).containsKey(MODIFY_RESERVATION_DATA);

		final PaymentCapabilityResponse cancelResponse = cancel(reservationResponse, modifiedAmount);
		assertThat(cancelResponse.getData()).containsKey(CANCEL_RESERVATION_DATA);
	}

	@Test
	public void testReverseChargeDataPropagation() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse chargeResponse = charge(reservationResponse, getChargeAmount());

		final PaymentCapabilityResponse reverseChargeResponse = reverseCharge(chargeResponse);
		assertThat(reverseChargeResponse.getData()).containsKey(REVERSE_CHARGE_DATA);
	}

	@Override
	protected PaymentProviderPlugin getPlugin() {
		return plugin;
	}

	@Override
	protected Map<String, String> populatePICInstructionsForm(final PICInstructionsFields fields) {
		return fields.getFields().stream().collect(Collectors.toMap(identity(), field -> ""));
	}

	@Override
	protected Map<String, String> populatePICForm(final PaymentInstrumentCreationFields fields, final PICInstructions instructions) {
		return fields.getFields().stream().collect(Collectors.toMap(identity(), field -> ""));
	}

}