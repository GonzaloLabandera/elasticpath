/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.payment.provider.test;

import java.util.Collections;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
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
import com.elasticpath.plugin.payment.provider.dto.TransactionType;

/**
 * Smart path payment provider plugin, that does not support cancel capability.
 */
@Component("smartPathCreditUnsupportedPaymentProviderPlugin")
@Scope("prototype")
public class SmartPathCreditUnsupportedPaymentProviderPluginImpl extends AbstractSmartPathPaymentProviderPlugin implements
		PICClientInteractionRequestCapability,
		ReserveCapability, ModifyCapability, CancelCapability,
		ChargeCapability, ReverseChargeCapability {

	@Override
	public String getPaymentVendorId() {
		return "Smart Path Credit Unsupported";
	}

	@Override
	public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO) {
		return new PICInstructionsFields(Collections.emptyList());
	}

	@Override
	public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest request) {
		return new PICInstructions(Collections.emptyMap(), Collections.emptyMap());
	}

	@Override
	public PaymentCapabilityResponse charge(final ChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request, TransactionType.CHARGE).build();
	}

	@Override
	public PaymentCapabilityResponse reverseCharge(final ReverseChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request, TransactionType.REVERSE_CHARGE).build();
	}

	@Override
	public PaymentCapabilityResponse cancel(final CancelCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request, TransactionType.CANCEL_RESERVE).build();
	}

	@Override
	public PaymentCapabilityResponse modify(final ModifyCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request, TransactionType.MODIFY_RESERVE).build();
	}

	@Override
	public PaymentCapabilityResponse reserve(final ReserveCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request, TransactionType.RESERVE).build();
	}

}
