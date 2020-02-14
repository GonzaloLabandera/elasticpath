/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.creation;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;

/**
 * Payment instrument creation request.
 */
public interface PICCapability extends Capability {

	/**
	 * Creates payment instrument. Performs additional validation of passed data, if required.
	 *
	 * @param request contains data to create payment instrument with.
	 * @return response object containing creation response details.
	 * @throws PaymentInstrumentCreationFailedException if the process failed.
	 */
	PaymentInstrumentCreationResponse createPaymentInstrument(PaymentInstrumentCreationRequest request)
			throws PaymentInstrumentCreationFailedException;

	/**
	 * This method returns the fields needed to be filled for creating a new payment instrument.
	 *
	 * @param pICFieldsRequestContextDTO {@link PICFieldsRequestContextDTO} containing context information for this request.
	 * @return payment instrument creation fields.
	 * @throws PaymentInstrumentCreationFailedException if the process failed.
	 */
	PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(PICFieldsRequestContextDTO pICFieldsRequestContextDTO)
			throws PaymentInstrumentCreationFailedException;
}
