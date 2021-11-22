/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import static java.util.Arrays.asList;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKeyBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequest;
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
 * Happy path payment provider plugin.
 */
@Component("happyPathPaymentProviderPlugin")
@Scope("prototype")
public class HappyPathPaymentProviderPlugin extends AbstractPaymentProviderPlugin implements
		PICClientInteractionRequestCapability,
		ReserveCapability, ModifyCapability,
		CreditCapability, CancelCapability, ChargeCapability, ReverseChargeCapability, PICCapability {

	private static final String PIC_INSTRUCTION_FIELD_A = "PIC Instruction Field A";
	private static final String PIC_INSTRUCTION_FIELD_B = "PIC Instruction Field B";
	private static final String PIC_FIELD_A = "PIC Field A";
	private static final String PIC_FIELD_B = "PIC Field B";
	/**
	 * Plugin config key A.
	 */
	static final String CONFIG_KEY_A = "Config A";
	/**
	 * Plugin config field A.
	 */
	static final String CONFIG_KEY_B = "Config B";

	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request)
			throws PaymentInstrumentCreationFailedException {

		if (request.getFormData().containsKey(PIC_FIELD_A)
				&& request.getFormData().containsKey(PIC_FIELD_B)) {
			return new PaymentInstrumentCreationResponse(ImmutableMap.of("details", UUID.randomUUID().toString()));
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.NEEDINFO,
				StringUtils.EMPTY,
				"PIC fields required.",
				Collections.singletonMap("reason", PIC_FIELD_A + ", " + PIC_FIELD_B + " are required"))));
	}

	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO) {
		return new PaymentInstrumentCreationFields(asList(PIC_FIELD_A, PIC_FIELD_B), true);
	}

	@Override
	public String getPaymentVendorId() {
		return "Happy Path";
	}

	@Override
	public String getPaymentMethodId() {
		return "CARD METHOD";
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return asList(createPluginConfigurationKey(CONFIG_KEY_A, CONFIG_KEY_A), createPluginConfigurationKey(CONFIG_KEY_B, CONFIG_KEY_B));
	}

	@Override
	public PaymentCapabilityResponse charge(final ChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request).build();
	}

	@Override
	public PaymentCapabilityResponse reverseCharge(final ReverseChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request).build();
	}

	@Override
	public PaymentCapabilityResponse credit(final CreditCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request).build();
	}

	@Override
	public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO) {
		return new PICInstructionsFields(asList(PIC_INSTRUCTION_FIELD_A, PIC_INSTRUCTION_FIELD_B));
	}

	@Override
	public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest request)
			throws PaymentInstrumentCreationFailedException {

		if (request.getFormData().containsKey(PIC_INSTRUCTION_FIELD_A)
				&& request.getFormData().containsKey(PIC_INSTRUCTION_FIELD_B)) {
			return new PICInstructions(
					ImmutableMap.of("control-secret", UUID.randomUUID().toString()),
					ImmutableMap.of("payload-secret", UUID.randomUUID().toString()));
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.NEEDINFO,
				StringUtils.EMPTY,
				"Billing address required.",
				Collections.singletonMap("reason", PIC_INSTRUCTION_FIELD_A + ", " + PIC_INSTRUCTION_FIELD_B + " and billing address are required"))));
	}

	@Override
	public PaymentCapabilityResponse cancel(final CancelCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request).build();
	}

	@Override
	public PaymentCapabilityResponse modify(final ModifyCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request).build();
	}

	@Override
	public PaymentCapabilityResponse reserve(final ReserveCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		return createResponse(request).build();
	}

	private static PaymentCapabilityResponseBuilder createResponse(final PaymentCapabilityRequest request)
			throws PaymentCapabilityRequestFailedException {

		if (request.getPluginConfigData().containsKey(CONFIG_KEY_A)
				&& request.getPluginConfigData().containsKey(CONFIG_KEY_B)) {

			return PaymentCapabilityResponseBuilder.aResponse()
					.withData(request.getPluginConfigData())
					.withProcessedDateTime(LocalDateTime.now())
					.withRequestHold(false);
		}

		throw new PaymentCapabilityRequestFailedException(
				CONFIG_KEY_A + " and " + CONFIG_KEY_B + " are required", "Configuration failure", false);
	}

	private PluginConfigurationKey createPluginConfigurationKey(final String key, final String description) {
		return PluginConfigurationKeyBuilder.builder()
				.withKey(key)
				.withDescription(description)
				.build();
	}
}
