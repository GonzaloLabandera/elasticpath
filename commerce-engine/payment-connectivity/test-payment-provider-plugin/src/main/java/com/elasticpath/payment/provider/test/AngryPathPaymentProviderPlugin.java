/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.ErrorMessage;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.plugin.payment.provider.exception.StructuredMessageType;

/**
 * Angry path payment provider plugin, throwing exception for every use case.
 */
@Component("angryPathPaymentProviderPlugin")
@Scope("prototype")
public class AngryPathPaymentProviderPlugin extends AbstractPaymentProviderPlugin implements
		PICClientInteractionRequestCapability,
		ReserveCapability, ModifyCapability,
		CreditCapability, CancelCapability, ChargeCapability, ReverseChargeCapability, PICCapability {

	private static final PaymentCapabilityRequestFailedException CAPABILITY_EXCEPTION =
			new PaymentCapabilityRequestFailedException("Plugin operations always fail by design", "I am angry", false);

	private static final PaymentInstrumentCreationFailedException PIC_EXCEPTION =
			new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.ERROR,
					StringUtils.EMPTY,
					StringUtils.EMPTY,
					Collections.singletonMap("details", "Plugin operations always fail by design"))));

	@Override
	public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO)
			throws PaymentInstrumentCreationFailedException {
		throw PIC_EXCEPTION;
	}

	@Override
	public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest request)
			throws PaymentInstrumentCreationFailedException {
		throw PIC_EXCEPTION;
	}

	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request)
			throws PaymentInstrumentCreationFailedException {
		throw PIC_EXCEPTION;
	}

	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO) {
		return new PaymentInstrumentCreationFields(Collections.emptyList(), false);
	}

	@Override
	public String getPaymentVendorId() {
		return "Angry Path";
	}

	@Override
	public String getPaymentMethodId() {
		return "CARD";
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return Collections.emptyList();
	}

	@Override
	public PaymentCapabilityResponse charge(final ChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		throw CAPABILITY_EXCEPTION;
	}

	@Override
	public PaymentCapabilityResponse reverseCharge(final ReverseChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		throw CAPABILITY_EXCEPTION;
	}

	@Override
	public PaymentCapabilityResponse credit(final CreditCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		throw CAPABILITY_EXCEPTION;
	}

	@Override
	public PaymentCapabilityResponse cancel(final CancelCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		throw CAPABILITY_EXCEPTION;
	}

	@Override
	public PaymentCapabilityResponse modify(final ModifyCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		throw CAPABILITY_EXCEPTION;
	}

	@Override
	public PaymentCapabilityResponse reserve(final ReserveCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		throw CAPABILITY_EXCEPTION;
	}

}
