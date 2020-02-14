/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.instructions;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;

/**
 * <p>Provides an ability to provide instructions required to create payment instrument.</p>
 */
public interface PICClientInteractionRequestCapability extends Capability {

	/**
	 * Returns an object which encapsulates fields required to be filled in order to request instructions for payment instrument creation.
	 *
	 * @param pICFieldsRequestContextDTO {@link PICFieldsRequestContextDTO} containing required context information
	 * @return {@link PICInstructionsFields}.
	 * @throws PaymentInstrumentCreationFailedException if the process failed
	 */
	PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(PICFieldsRequestContextDTO pICFieldsRequestContextDTO)
			throws PaymentInstrumentCreationFailedException;

	/**
	 * Provides payment instrument creation instruction.
	 *
	 * @param request request to be used to get instructions for.
	 * @return {@link PICInstructions}.
	 * @throws PaymentInstrumentCreationFailedException if the process failed
	 */
	PICInstructions getPaymentInstrumentCreationInstructions(PICInstructionsRequest request)
			throws PaymentInstrumentCreationFailedException;
}
