/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.payment.provider;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.test.util.Utils;

/**
 * Default implementation for {@link PICCapability}.
 */
public class PICCapabilityImplementationForTesting implements PICCapability {
	private static final Map<String, String> PIC_RESPONSE_DETAILS = ImmutableMap.of(Utils.uniqueCode("KEY"), Utils.uniqueCode("DATA"));

	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request)
			throws PaymentInstrumentCreationFailedException {
		if (Boolean.parseBoolean(request.getFormData().getOrDefault("fail", String.valueOf(false)))) {
			throw new PaymentInstrumentCreationFailedException(Collections.emptyList());
		}
		return new PaymentInstrumentCreationResponse(PIC_RESPONSE_DETAILS);
	}

	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO) {
		return new PaymentInstrumentCreationFields(Collections.emptyList(), false);
	}
}
