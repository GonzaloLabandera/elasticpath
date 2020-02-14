/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment;

import java.util.Map;

import io.reactivex.Single;

import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;

/**
 * The facade for operations with {@link com.elasticpath.service.orderpaymentapi.OrderPaymentApiService}.
 */
public interface OrderPaymentApiRepository {

	/**
	 * Gets the fields required for payment instrument creation instructions.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param picFieldsRequestContext   the context for this request
	 * @return Payment Instrument Creation Instructions fields.
	 */
	Single<PICInstructionsFieldsDTO> getPICInstructionsFields(String paymentProviderConfigGuid, PICFieldsRequestContext picFieldsRequestContext);

	/**
	 * Gets the payment instrument creation instructions.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param formData                  form data submitted with the instructions request
	 * @param picRequestContext         the context for this request
	 * @return payment instrument creation instructions
	 */
	Single<PICInstructionsDTO> getPICInstructions(String paymentProviderConfigGuid, Map<String, String> formData,
												  PICRequestContext picRequestContext);

	/**
	 * Gets the fields required for payment instrument creation.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param picFieldsRequestContext   the context for this request
	 * @return payment instrument creation fields
	 */
	Single<PaymentInstrumentCreationFieldsDTO> getPICFields(String paymentProviderConfigGuid, PICFieldsRequestContext picFieldsRequestContext);

	/**
	 * Creates a payment instrument.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param paymentInstrumentForm     form submitted with the creation request
	 * @param picRequestContext         the context for this request
	 * @return the created payment intrument's guid
	 */
	Single<String> createPI(String paymentProviderConfigGuid, Map<String, String> paymentInstrumentForm, PICRequestContext picRequestContext);


	/**
	 * Checks whether the given payment provider requires a billing address during payment instrument creation.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @return boolean indicating whether the provider requires a billing address
	 */
	Single<Boolean> requiresBillingAddress(String paymentProviderConfigGuid);
}
