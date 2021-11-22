/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.ErrorMessage;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.plugin.payment.provider.exception.StructuredMessageType;

/**
 * Email required payment provider plugin.
 */
@Component("emailRequiredPaymentProviderPlugin")
@Scope("prototype")
public class EmailRequiredPaymentProviderPlugin extends AbstractPaymentProviderPlugin implements PICClientInteractionRequestCapability,
		PICCapability {

	private static final String EMAIL_IS_REQUIRED_MESSAGE = "Email is required.";

	@Override
	public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request)
			throws PaymentInstrumentCreationFailedException {
		if (Objects.nonNull(request.getPICRequestContextDTO().getCustomerContextDTO().getEmail())) {
			return new PaymentInstrumentCreationResponse(ImmutableMap.of("details", UUID.randomUUID().toString()));
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.NEEDINFO,
				StringUtils.EMPTY,
				EMAIL_IS_REQUIRED_MESSAGE,
				Collections.emptyMap())));
	}

	@Override
	public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO)
			throws PaymentInstrumentCreationFailedException {
		if (Objects.nonNull(pICFieldsRequestContextDTO.getCustomerContextDTO().getEmail())) {
			return new PaymentInstrumentCreationFields(Collections.emptyList(), true);
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.NEEDINFO,
				StringUtils.EMPTY,
				EMAIL_IS_REQUIRED_MESSAGE,
				Collections.emptyMap())));
	}

	@Override
	public String getPaymentVendorId() {
		return "Email Required";
	}

	@Override
	public String getPaymentMethodId() {
		return "CARD METHOD";
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return Collections.emptyList();
	}

	@Override
	public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO pICFieldsRequestContextDTO)
			throws PaymentInstrumentCreationFailedException {
		if (Objects.nonNull(pICFieldsRequestContextDTO.getCustomerContextDTO().getEmail())) {
			return new PICInstructionsFields(Collections.emptyList());
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.NEEDINFO,
				StringUtils.EMPTY,
				EMAIL_IS_REQUIRED_MESSAGE,
				Collections.emptyMap())));
	}

	@Override
	public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest request)
			throws PaymentInstrumentCreationFailedException {
		if (Objects.nonNull(request.getPICRequestContextDTO().getCustomerContextDTO().getEmail())) {
			return new PICInstructions(
					ImmutableMap.of("control-secret", UUID.randomUUID().toString()),
					ImmutableMap.of("payload-secret", UUID.randomUUID().toString()));
		}

		throw new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.NEEDINFO,
				StringUtils.EMPTY,
				EMAIL_IS_REQUIRED_MESSAGE,
				Collections.emptyMap())));
	}

}
