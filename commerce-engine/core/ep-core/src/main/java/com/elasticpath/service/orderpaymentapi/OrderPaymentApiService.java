/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.List;
import java.util.Map;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentAmounts;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.money.Money;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Service methods for order payment.
 */
public interface OrderPaymentApiService {

	/**
	 * Initiates payment actions associated with creation of the given order, reserving the corresponding payments.
	 *
	 * @param order the new order created
	 */
	void orderCreated(Order order);

	/**
	 * Rolls back payment actions associated with creation of the given order, cancelling the corresponding reservations.
	 *
	 * @param order the order
	 */
	void rollbackOrderCreated(Order order);

	/**
	 * Initiates payment actions associated with completing a shipment for the given order.
	 *
	 * @param orderShipment the order shipment failed to be completed.
	 * @return payment events ledger after completion.
	 */
	List<PaymentEvent> shipmentCompleted(OrderShipment orderShipment);

	/**
	 * Initiates payment actions associated with rollback for a completed shipment for the given order.
	 *
	 * @param orderShipment the order shipment to be completed
	 * @param paymentEvents payment events ledger after completion.
	 */
	void rollbackShipmentCompleted(OrderShipment orderShipment, List<PaymentEvent> paymentEvents);

	/**
	 * Initiates cancellation of payments associated with an in-progress shipment.
	 *
	 * @param orderShipment the order shipment
	 */
	void shipmentCanceled(OrderShipment orderShipment);

	/**
	 * Initiates cancellation of payments associated with in-progress shipments.
	 *
	 * @param order the order
	 */
	void orderCanceled(Order order);

	/**
	 * Initiates payment actions associated with the modification of a given order.
	 * The supplied payment instruments represent ones connected to the given order.
	 *
	 * @param order              the order
	 * @param paymentInstruments empty if original PI's to be used, otherwise
	 *                           the explicit profile payment instruments that need to
	 *                           be used for amount increase/decrease.
	 * @param newTotal           the new total
	 */
	void orderModified(Order order, List<PaymentInstrumentDTO> paymentInstruments, Money newTotal);

	/**
	 * Initiates payment actions associated with an explicit refund for the given order.  An explicit refund is a refund
	 * processed against specified payment instruments, if provided in the paymentInstrumentIds parameter.
	 *
	 * @param order              the order
	 * @param paymentInstruments empty if original PI's to be used (implicit refund), otherwise
	 *                           the explicit profile payment instruments that need to be used for refund.
	 * @param amountToRefund     {@link Money} amount to refund
	 */
	void refund(Order order, List<PaymentInstrumentDTO> paymentInstruments, Money amountToRefund);

	/**
	 * Initiates payment actions associated with a manual refund for the given order.
	 *
	 * @param order          the order
	 * @param amountToRefund {@link Money} amount to refund
	 */
	void manualRefund(Order order, Money amountToRefund);

	/**
	 * Get the fields required for Payment Instrument Creation Instructions.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param pICFieldsRequestContext   the relevant {@link PICFieldsRequestContext}.
	 * @return Payment Instrument Creation Instructions fields.
	 */
	PICInstructionsFieldsDTO getPICInstructionsFields(String paymentProviderConfigGuid, PICFieldsRequestContext pICFieldsRequestContext);

	/**
	 * Get the Payment Instrument Creation Instructions.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param pICInstructions           map of PIC instructions.
	 * @param pICRequestContext         the relevant {@link PICRequestContext}.
	 * @return Payment Instrument Creation Instructions.
	 */
	PICInstructionsDTO getPICInstructions(String paymentProviderConfigGuid, Map<String, String> pICInstructions,
										  PICRequestContext pICRequestContext);

	/**
	 * Get the fields required for Payment Instrument Creation.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param pICFieldsRequestContext   the relevant {@link PICFieldsRequestContext}.
	 * @return Payment Instrument Creation fields
	 */
	PaymentInstrumentCreationFieldsDTO getPICFields(String paymentProviderConfigGuid, PICFieldsRequestContext pICFieldsRequestContext);

	/**
	 * Create payment instrument.
	 *
	 * @param paymentProviderConfigGuid the payment provider config GUID.
	 * @param paymentInstrumentForm     payment instrument creation form.
	 * @param pICRequestContext         the relevant {@link PICRequestContext}.
	 * @return payment instrument GUID
	 * @throws com.elasticpath.provider.payment.service.PaymentsException inform shopper about recoverable error
	 */
	String createPI(String paymentProviderConfigGuid, Map<String, String> paymentInstrumentForm, PICRequestContext pICRequestContext);

    /**
     * Converts {@link OrderPayment} entity to {@link PaymentEvent}.
     *
     * @param orderPayment order payment that's been converted.
     * @param order        order corresponding to the payment
     * @return payment event after conversion.
     */
    PaymentEvent buildPaymentEvent(OrderPayment orderPayment, Order order);

	/**
	 * Checks whether the shipment is the last shippable shipment for the order.
	 *
	 * @param configurationId the payment provider configuration GUID.
	 * @return boolean indicating whether the provider requires a billing address
	 */
	boolean requiresBillingAddress(String configurationId);

	/**
	 * Returns the calculated total amounts for an order.
	 *
	 * @param order the order
	 * @return the calculated total amounts for an order.
	 */
	OrderPaymentAmounts getOrderPaymentAmounts(Order order);
}
