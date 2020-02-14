/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;

/**
 * Payment instrument creation processor.
 */
public interface PaymentInstrumentCreationProcessor {
	/**
	 * Get the fields required for Payment Instrument Creation Instructions.
	 *
	 * @param paymentProviderConfigurationGuid payment provider configuration GUID
	 * @param context                          request context
	 * @return payment instrument creation instructions fields
	 */
	PICInstructionsFieldsDTO getPICInstructionFields(String paymentProviderConfigurationGuid, PICFieldsRequestContextDTO context);

	/**
	 * Get the Payment Instrument Creation Instructions.
	 *
	 * @param configurationGuid payment provider configuration GUID
	 * @param instructionsMap   PIC instructions values map
	 * @param context           request context
	 * @return payment instrument creation instructions
	 */
	PICInstructionsDTO getPICInstructions(String configurationGuid, Map<String, String> instructionsMap, PICRequestContextDTO context);

	/**
	 * Get the required fields for Payment instrument creation.
	 *
	 * @param paymentProviderConfigurationGuid payment provider configuration GUID
	 * @param context                          request context
	 * @return payment instrument creation fields
	 */
	PaymentInstrumentCreationFieldsDTO getPICFields(String paymentProviderConfigurationGuid, PICFieldsRequestContextDTO context);

	/**
	 * Create payment instrument.
	 *
	 * @param configurationGuid payment provider configuration GUID
	 * @param instrumentMap     instrument field values map
	 * @param context           request context
	 * @return payment instrument GUID
	 */
	String createPI(String configurationGuid, Map<String, String> instrumentMap, PICRequestContextDTO context);
}
