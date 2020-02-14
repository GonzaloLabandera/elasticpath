/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKeyBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;

/**
 * Payment provider plugin without capabilities.
 */
@Component("noCapabilitiesPaymentProviderPlugin")
@Scope("prototype")
public class NoCapabilitiesPaymentProviderPlugin extends AbstractPaymentProviderPlugin implements PICCapability, ChargeCapability {

	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request) {
		return new PaymentInstrumentCreationResponse(Collections.emptyMap());
	}

	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO) {
		return new PaymentInstrumentCreationFields(Collections.emptyList(), true);
	}

	@Override
	public String getPaymentVendorId() {
		return "No Capabilities";
	}

	@Override
	public String getPaymentMethodId() {
		return "CARD";
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return createKeys();
	}

	private List<PluginConfigurationKey> createKeys() {
		final PluginConfigurationKey propertyA = PluginConfigurationKeyBuilder.builder()
				.withKey("A key")
				.build();
		final PluginConfigurationKey propertyB = PluginConfigurationKeyBuilder.builder()
				.withKey("B key")
				.withDescription("B description")
				.build();
		final PluginConfigurationKey propertyC = PluginConfigurationKeyBuilder.builder()
				.withKey("C key")
				.withDescription("C description")
				.build();

		return Arrays.asList(propertyB, propertyA, propertyC);
	}

	@Override
	public PaymentCapabilityResponse charge(final ChargeCapabilityRequest request) {
		final PaymentCapabilityResponse response = new PaymentCapabilityResponse();
		response.setProcessedDateTime(LocalDateTime.now());
		response.setRequestHold(false);
		response.setData(Collections.singletonMap("response-key", "No Capabilities"));
		return response;
	}
}
