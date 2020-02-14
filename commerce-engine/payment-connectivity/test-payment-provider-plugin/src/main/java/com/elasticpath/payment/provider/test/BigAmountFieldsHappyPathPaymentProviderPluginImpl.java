/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import static java.util.Arrays.asList;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKeyBuilder;
import com.elasticpath.plugin.payment.provider.annotations.BillingAddressRequired;
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
 * Big Amount Fields Happy path payment provider plugin.
 */
@Component("bigAmountFieldsHappyPathPaymentProviderPlugin")
@Scope("prototype")
@BillingAddressRequired
public class BigAmountFieldsHappyPathPaymentProviderPluginImpl extends AbstractPaymentProviderPlugin implements
		PICClientInteractionRequestCapability,
		ReserveCapability, ModifyCapability,
		CreditCapability, CancelCapability, ChargeCapability, ReverseChargeCapability, PICCapability {

	/**
	 * Plugin config key A.
	 */
	static final String CONFIG_KEY_A = "Config A";
	/**
	 * Plugin config key B.
	 */
	static final String CONFIG_KEY_B = "Config B";

	private static final String FIELD_PREFIX = "Field ";
	private static final int FIELD_AMOUNT = 10;
	private static final int KEY_LENGTH = 64;
	private static final int VALUE_LENGTH = 1000;
	private static final boolean USE_LETTERS = true;
	private static final boolean USE_NUMBERS = true;

	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request)
			throws PaymentInstrumentCreationFailedException {
		if (!request.getFormData().isEmpty()) {
			return new PaymentInstrumentCreationResponse(generateRandomDataMap());
		}
		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.NEEDINFO,
				StringUtils.EMPTY,
				"PIC input data required.",
				Collections.singletonMap("reason", "PIC input data is required"))));
	}

	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO) {
		List<String> fields = Stream.iterate(1, value -> value + 1)
				.limit(FIELD_AMOUNT)
				.map(fieldNumber -> FIELD_PREFIX + fieldNumber)
				.collect(Collectors.toList());
		return new PaymentInstrumentCreationFields(fields, true);
	}

	@Override
	public String getPaymentVendorId() {
		return "Big Amount Fields Happy Path";
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
	public PaymentCapabilityResponse charge(final ChargeCapabilityRequest request) {
		return createResponse();
	}

	@Override
	public PaymentCapabilityResponse reverseCharge(final ReverseChargeCapabilityRequest request) {
		return createResponse();
	}

	@Override
	public PaymentCapabilityResponse credit(final CreditCapabilityRequest request) {
		return createResponse();
	}

	@Override
	public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO context) {
		return new PICInstructionsFields(Collections.emptyList());
	}

	@Override
	public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest request) {
		return new PICInstructions(Collections.emptyMap(), Collections.emptyMap());
	}

	@Override
	public PaymentCapabilityResponse cancel(final CancelCapabilityRequest request) {
		return createResponse();
	}

	@Override
	public PaymentCapabilityResponse modify(final ModifyCapabilityRequest request) {
		return createResponse();
	}

	@Override
	public PaymentCapabilityResponse reserve(final ReserveCapabilityRequest request) {
		return createResponse();
	}

	private static PaymentCapabilityResponse createResponse() {
		return PaymentCapabilityResponseBuilder.aResponse()
				.withData(generateRandomDataMap())
				.withProcessedDateTime(LocalDateTime.now())
				.withRequestHold(false)
				.build();
	}

	private static Map<String, String> generateRandomDataMap() {
		final Map<String, String> generatedData = new HashMap<>();
		Stream.iterate(1, value -> value + 1)
				.limit(FIELD_AMOUNT)
				.map(fieldNumber -> RandomStringUtils.random(KEY_LENGTH, USE_LETTERS, USE_NUMBERS))
				.forEach(key -> generatedData.put(key, RandomStringUtils.random(VALUE_LENGTH, USE_LETTERS, USE_NUMBERS)));
		return generatedData;
	}

	private PluginConfigurationKey createPluginConfigurationKey(final String key, final String description) {
		return PluginConfigurationKeyBuilder.builder()
				.withKey(key)
				.withDescription(description)
				.build();
	}
}
