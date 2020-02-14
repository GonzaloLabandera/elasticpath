/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elastipath.payment.provider.purchaseordernumber;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequest;
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
 * Purchase payment provider implementation.
 */
@Component("purchaseOrderProviderPlugin")
@Scope("prototype")
public class PurchaseOrderProviderPlugin extends AbstractPaymentProviderPlugin implements
		ChargeCapability,
		PICCapability,
		PICClientInteractionRequestCapability,
		ReserveCapability,
		ModifyCapability,
		CreditCapability,
		CancelCapability,
		ReverseChargeCapability {

	private static final String PURCHASE_ORDER_KEY = "purchase-order";
	private static final String FIELD_NAME_KEY = "field-name";

	@Override
	public String getPaymentVendorId() {
		return "ELASTICPATH";
	}

	@Override
	public String getPaymentMethodId() {
		return "PURCHASE_ORDER";
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return Collections.emptyList();
	}

	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest paymentInstrumentCreationRequest)
			throws PaymentInstrumentCreationFailedException {
		validate(paymentInstrumentCreationRequest);
		return new PaymentInstrumentCreationResponse(Collections.singletonMap(PURCHASE_ORDER_KEY,
				paymentInstrumentCreationRequest.getFormData().get(PURCHASE_ORDER_KEY)));
	}

	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO picFieldsRequestContextDTO) {
		return new PaymentInstrumentCreationFields(Collections.singletonList(PURCHASE_ORDER_KEY), false);
	}

	@Override
	public PaymentCapabilityResponse charge(final ChargeCapabilityRequest chargeCapabilityRequest) {
		return createResponse(chargeCapabilityRequest);
	}

	@Override
	public PaymentCapabilityResponse reverseCharge(final ReverseChargeCapabilityRequest reverseChargeCapabilityRequest) {
		return createResponse(reverseChargeCapabilityRequest);
	}

	@Override
	public PaymentCapabilityResponse credit(final CreditCapabilityRequest creditCapabilityRequest) {
		return createResponse(creditCapabilityRequest);
	}

	@Override
	public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO picFieldsRequestContextDTO) {
		return new PICInstructionsFields(Collections.emptyList());
	}

	@Override
	public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest picInstructionsRequest) {
		return new PICInstructions(Collections.emptyMap(), Collections.emptyMap());
	}

	@Override
	public PaymentCapabilityResponse cancel(final CancelCapabilityRequest cancelCapabilityRequest) {
		return createResponse(cancelCapabilityRequest);
	}

	@Override
	public PaymentCapabilityResponse modify(final ModifyCapabilityRequest modifyCapabilityRequest) {
		return createResponse(modifyCapabilityRequest);
	}

	@Override
	public PaymentCapabilityResponse reserve(final ReserveCapabilityRequest reserveCapabilityRequest) {
		return createResponse(reserveCapabilityRequest);
	}

	/**
	 * Validates if request contains {@link #PURCHASE_ORDER_KEY}.
	 *
	 * @param paymentInstrumentCreationRequest is request to check.
	 * @throws PaymentInstrumentCreationFailedException if request does not contain {@link #PURCHASE_ORDER_KEY}.
	 */
	protected void validate(final PaymentInstrumentCreationRequest paymentInstrumentCreationRequest) throws PaymentInstrumentCreationFailedException {
		if (StringUtils.isEmpty(paymentInstrumentCreationRequest.getFormData().get(PURCHASE_ORDER_KEY))) {
			throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.ERROR,
					StringUtils.EMPTY,
					"Purchase order is required.",
					Collections.singletonMap(FIELD_NAME_KEY, PURCHASE_ORDER_KEY))));
		}
	}

	/**
	 * Creates the response for capabilities.
	 *
	 * @param request is given capability request.
	 * @return a {@link PaymentCapabilityResponse} object.
	 */
	protected PaymentCapabilityResponse createResponse(final PaymentCapabilityRequest request) {
		final PaymentCapabilityResponse response = new PaymentCapabilityResponse();
		response.setProcessedDateTime(LocalDateTime.now());
		response.setRequestHold(false);
		response.setData(Collections.singletonMap("PURCHASE_ORDER", request.getPaymentInstrumentData().get(PURCHASE_ORDER_KEY)));
		return response;
	}
}

