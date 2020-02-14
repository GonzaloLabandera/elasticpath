/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;

/**
 * Test charge request for $10 US.
 */
public class TenUsDollarsChargeCapabilityRequest extends ChargeCapabilityRequest {

	private static final MoneyDTO TEN_US_DOLLARS = MoneyDTOBuilder.builder()
			.withAmount(BigDecimal.TEN)
			.withCurrencyCode("USD")
			.build(new MoneyDTO());

	private Map<String, String> pluginConfigData = new HashMap<>();

	@Override
	public MoneyDTO getAmount() {
		return TEN_US_DOLLARS;
	}

	@Override
	public void setAmount(final MoneyDTO amount) {
		// hardcoded value
	}

	@Override
	public Map<String, String> getPaymentInstrumentData() {
		return Collections.emptyMap();
	}

	@Override
	public void setPaymentInstrumentData(final Map<String, String> paymentInstrumentData) {
		// hardcoded value
	}

	@Override
	public Map<String, String> getCustomRequestData() {
		return Collections.emptyMap();
	}

	@Override
	public void setCustomRequestData(final Map<String, String> customRequestData) {
		// hardcoded value
	}

	@Override
	public Map<String, String> getPluginConfigData() {
		return pluginConfigData;
	}

	@Override
	public void setPluginConfigData(final Map<String, String> pluginConfigData) {
		this.pluginConfigData = pluginConfigData;
	}

}
