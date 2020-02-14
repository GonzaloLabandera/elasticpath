/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;

/**
 * The main Payment API workflow facade for Order Payment API (ep-core) interaction.
 */
public interface PaymentAPIWorkflow {

	/**
	 * Get the fields required for Payment Instrument Creation Instructions.
	 *
	 * @param paymentProviderConfigurationGuid payment provider configuration GUID.
	 * @param context                          request context
	 * @return payment instrument creation instructions fields
	 */
	PICInstructionsFieldsDTO getPICInstructionFields(String paymentProviderConfigurationGuid, PICFieldsRequestContextDTO context);

	/**
	 * Get the Payment Instrument Creation Instructions.
	 *
	 * @param configurationGuid payment provider configuration GUID
	 * @param instructionsMap   map of PIC instructions
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
	 * @param instrumentMap     map of instrument field values
	 * @param context           request context
	 * @return payment instrument GUID
	 */
	String createPI(String configurationGuid, Map<String, String> instrumentMap, PICRequestContextDTO context);

	/**
	 * Payment Reservation.
	 *
	 * @param reserveRequest the reserve request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse reserve(ReserveRequest reserveRequest);

	/**
	 * Cancel reservation.
	 *
	 * @param cancelRequest the cancel request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse cancelReservation(CancelReservationRequest cancelRequest);

	/**
	 * Payment Modify reservation.
	 *
	 * @param modifyRequest the modification of reservation request.
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse modifyReservation(ModifyReservationRequest modifyRequest);

	/**
	 * Charge payment.
	 *
	 * @param chargeRequest the charge request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse chargePayment(ChargeRequest chargeRequest);

	/**
	 * Refund payment on payment instrument.
	 *
	 * @param creditRequest the implicit credit request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse credit(CreditRequest creditRequest);

	/**
	 * Manual refund payment.
	 *
	 * @param creditRequest the manual credit request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse manualCredit(ManualCreditRequest creditRequest);

	/**
	 * Cancel all reservation.
	 *
	 * @param cancelAllReservationsRequest the cancel request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse cancelAllReservations(CancelAllReservationsRequest cancelAllReservationsRequest);

	/**
	 * Reverse charge.
	 *
	 * @param reverseChargeRequest the reverse charge request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse reverseCharge(ReverseChargeRequest reverseChargeRequest);

	/**
	 * Determines whether a payment provider, identified by its configuration GUID, requires a billing address during payment instrument creation.
	 *
	 * @param configurationGuid payment provider configuration identifier
	 * @return boolean indicating whether this payment provider requires a billing address during the payment instrument creation flow
	 */
	boolean requiresBillingAddress(String configurationGuid);
}
