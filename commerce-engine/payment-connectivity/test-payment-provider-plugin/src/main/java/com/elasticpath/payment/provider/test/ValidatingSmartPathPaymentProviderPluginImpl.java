/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.payment.provider.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKeyBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.ChargeCapabilityFollowupRequest;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.ReserveCapabilityFollowupRequest;
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
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.plugin.payment.provider.exception.ErrorMessage;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.plugin.payment.provider.exception.StructuredMessageType;

/**
 * Smart path payment provider plugin, which validates request/response data propagation.
 */
@Component
@Scope("prototype")
public class ValidatingSmartPathPaymentProviderPluginImpl extends AbstractSmartPathPaymentProviderPlugin implements
		PICCapability, PICClientInteractionRequestCapability,
		ReserveCapability, ModifyCapability, CancelCapability,
		ChargeCapability, ReverseChargeCapability, CreditCapability {

	/**
	 * Single plugin configuration key.
	 */
	public static final String PLUGIN_CONFIG_KEY = "plugin-config";
	/**
	 * Single PIC instructions field.
	 */
	public static final String PIC_INSTRUCTIONS_FIELD = "pic-instructions-field";
	/**
	 * Single payment instrument field.
	 */
	public static final String PI_FIELD = "pi-field";
	/**
	 * Single reserve data key.
	 */
	public static final String RESERVE_DATA_KEY = "reserve-data";
	/**
	 * Single reserve data key.
	 */
	public static final String CHARGE_DATA_KEY = "charge-data";
	/**
	 * Single modify reservation data key.
	 */
	public static final String MODIFY_RESERVATION_DATA = "modify-reservation-data";
	/**
	 * Single cancel reservation data key.
	 */
	public static final String CANCEL_RESERVATION_DATA = "cancel-reservation-data";
	/**
	 * Single reverse charge data key.
	 */
	public static final String REVERSE_CHARGE_DATA = "reverse-charge-data";
	/**
	 * Single credit data key.
	 */
	public static final String CREDIT_DATA = "credit-data";

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability} method.
	 *
	 * @param context request context
	 * @return instructions fields
	 */
	@Override
	public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO context)
			throws PaymentInstrumentCreationFailedException {

		if (context.getPluginConfigData().containsKey(PLUGIN_CONFIG_KEY)) {
			return new PICInstructionsFields(Collections.singletonList(PIC_INSTRUCTIONS_FIELD));
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(
				StructuredMessageType.NEEDINFO, StringUtils.EMPTY, PLUGIN_CONFIG_KEY, Collections.emptyMap())));

	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability} method.
	 *
	 * @param request request
	 * @return instructions
	 */
	@Override
	public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest request)
			throws PaymentInstrumentCreationFailedException {

		if (request.getFormData().containsKey(PIC_INSTRUCTIONS_FIELD)
				&& request.getPluginConfigData().containsKey(PLUGIN_CONFIG_KEY)) {
			return new PICInstructions(request.getFormData(), request.getPluginConfigData());
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(
				StructuredMessageType.NEEDINFO, StringUtils.EMPTY, PIC_INSTRUCTIONS_FIELD, Collections.emptyMap())));

	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability} method.
	 *
	 * @param request request
	 * @return response
	 */
	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request)
			throws PaymentInstrumentCreationFailedException {

		if (request.getFormData().containsKey(PI_FIELD)
				&& request.getPluginConfigData().containsKey(PLUGIN_CONFIG_KEY)) {
			return new PaymentInstrumentCreationResponse(request.getFormData());
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(
				StructuredMessageType.NEEDINFO, StringUtils.EMPTY, PI_FIELD, Collections.emptyMap())));
	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability} method.
	 *
	 * @param context request context
	 * @return PIC fields
	 */
	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO context)
			throws PaymentInstrumentCreationFailedException {

		if (context.getPluginConfigData().containsKey(PLUGIN_CONFIG_KEY)) {
			return new PaymentInstrumentCreationFields(Collections.singletonList(PI_FIELD), true);
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(
				StructuredMessageType.NEEDINFO, StringUtils.EMPTY, PI_FIELD, Collections.emptyMap())));
	}

	private void checkCommonData(final PaymentCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		if (!request.getPaymentInstrumentData().containsKey(PI_FIELD)) {
			throw new PaymentCapabilityRequestFailedException("Request does not contain PI data", PI_FIELD, false);
		}

		if (!request.getPluginConfigData().containsKey(PLUGIN_CONFIG_KEY)) {
			throw new PaymentCapabilityRequestFailedException(
					"Request does not contain plugin configuration data", PLUGIN_CONFIG_KEY, false);
		}
	}

	private void checkReservationData(final ReserveCapabilityFollowupRequest request) throws PaymentCapabilityRequestFailedException {
		if (!request.getReservationData().containsKey(RESERVE_DATA_KEY)) {
			throw new PaymentCapabilityRequestFailedException("Request does not contain reservation data", RESERVE_DATA_KEY, false);
		}
	}

	private void checkChargeData(final ChargeCapabilityFollowupRequest request) throws PaymentCapabilityRequestFailedException {
		if (!request.getChargeData().containsKey(CHARGE_DATA_KEY)) {
			throw new PaymentCapabilityRequestFailedException("Request does not contain charge data", CHARGE_DATA_KEY, false);
		}
	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability} method.
	 *
	 * @param request request
	 * @return response
	 * @throws PaymentCapabilityRequestFailedException to simulate that request failed
	 */
	@Override
	public PaymentCapabilityResponse reserve(final ReserveCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		checkCommonData(request);

		final Map<String, String> data = new HashMap<>(request.getCustomRequestData());
		data.put(RESERVE_DATA_KEY, UUID.randomUUID().toString());
		return createResponse(request, TransactionType.RESERVE)
				.withData(data)
				.build();
	}


	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability} method.
	 *
	 * @param request request
	 * @return response
	 * @throws PaymentCapabilityRequestFailedException to simulate that request failed
	 */
	@Override
	public PaymentCapabilityResponse modify(final ModifyCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		checkCommonData(request);
		checkReservationData(request);

		final Map<String, String> data = new HashMap<>(request.getReservationData());
		data.putAll(request.getCustomRequestData());
		data.put(MODIFY_RESERVATION_DATA, UUID.randomUUID().toString());
		return createResponse(request, TransactionType.MODIFY_RESERVE)
				.withData(data)
				.build();
	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability} method.
	 *
	 * @param request request
	 * @return response
	 * @throws PaymentCapabilityRequestFailedException to simulate that request failed
	 */
	@Override
	public PaymentCapabilityResponse cancel(final CancelCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		checkCommonData(request);
		checkReservationData(request);

		final Map<String, String> data = new HashMap<>(request.getReservationData());
		data.putAll(request.getCustomRequestData());
		data.put(CANCEL_RESERVATION_DATA, UUID.randomUUID().toString());
		return createResponse(request, TransactionType.CANCEL_RESERVE)
				.withData(data)
				.build();
	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability} method.
	 *
	 * @param request request
	 * @return response
	 * @throws PaymentCapabilityRequestFailedException to simulate that request failed
	 */
	@Override
	public PaymentCapabilityResponse charge(final ChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		checkCommonData(request);
		checkReservationData(request);

		final Map<String, String> data = new HashMap<>(request.getReservationData());
		data.putAll(request.getCustomRequestData());
		data.put(CHARGE_DATA_KEY, UUID.randomUUID().toString());
		return createResponse(request, TransactionType.CHARGE)
				.withData(data)
				.build();
	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability} method.
	 *
	 * @param request request
	 * @return response
	 * @throws PaymentCapabilityRequestFailedException to simulate that request failed
	 */
	@Override
	public PaymentCapabilityResponse reverseCharge(final ReverseChargeCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		checkCommonData(request);
		checkChargeData(request);

		final Map<String, String> data = new HashMap<>(request.getChargeData());
		data.putAll(request.getCustomRequestData());
		data.put(REVERSE_CHARGE_DATA, UUID.randomUUID().toString());
		return createResponse(request, TransactionType.REVERSE_CHARGE)
				.withData(data)
				.build();
	}

	/**
	 * Test implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability} method.
	 *
	 * @param request request
	 * @return response
	 * @throws PaymentCapabilityRequestFailedException to simulate that request failed
	 */
	@Override
	public PaymentCapabilityResponse credit(final CreditCapabilityRequest request) throws PaymentCapabilityRequestFailedException {
		checkCommonData(request);
		checkChargeData(request);

		final Map<String, String> data = new HashMap<>(request.getChargeData());
		data.putAll(request.getCustomRequestData());
		data.put(CREDIT_DATA, UUID.randomUUID().toString());
		return createResponse(request, TransactionType.CREDIT)
				.withData(data)
				.build();
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		final List<PluginConfigurationKey> configurationKeys = super.getConfigurationKeys();
		configurationKeys.add(PluginConfigurationKeyBuilder.builder()
				.withKey(PLUGIN_CONFIG_KEY)
				.withDescription("Test plugin configuration key")
				.build());
		return configurationKeys;
	}

	@Override
	public String getPaymentVendorId() {
		return "Validating Smart Path";
	}

}
