/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.constants;

/**
 * <code>PaymentProviderApiContextIdNames</code> contains spring container bean id constants.
 * <p/>
 * Only Payment API classes should be added in here.
 */
public final class PaymentProviderApiContextIdNames {
	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.PaymentInstrument}.
	 */
	public static final String PAYMENT_INSTRUMENT = "paymentInstrument";
	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.PaymentInstrumentData}.
	 */
	public static final String PAYMENT_INSTRUMENT_DATA = "paymentInstrumentData";
	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.PaymentProviderConfiguration}.
	 */
	public static final String PAYMENT_PROVIDER_CONFIGURATION = "paymentProviderConfiguration";
	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData}.
	 */
	public static final String PAYMENT_PROVIDER_CONFIGURATION_DATA = "paymentProviderConfigurationData";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.PaymentLocalizedProperties}.
	 */
	public static final String PAYMENT_LOCALIZED_PROPERTIES = "paymentLocalizedProperties";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.PaymentLocalizedPropertyValue}.
	 */
	public static final String PAYMENT_LOCALIZED_PROPERTY_VALUE = "paymentLocalizedPropertyValue";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.processor.PaymentInstrumentCreationProcessor}.
	 */
	public static final String PIC_PROCESSOR = "paymentInstrumentCreationProcessor";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.processor.ReservationProcessor}.
	 */
	public static final String RESERVATION_PROCESSOR = "reservationProcessor";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.processor.CancelReservationProcessor}.
	 */
	public static final String CANCEL_RESERVATION_PROCESSOR = "cancelReservationProcessor";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.processor.ModifyReservationProcessor}.
	 */
	public static final String MODIFY_RESERVATION_PROCESSOR = "modifyReservationProcessor";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.processor.ChargeProcessor}.
	 */
	public static final String CHARGE_PROCESSOR = "chargeProcessor";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.processor.CreditProcessor}.
	 */
	public static final String CREDIT_PROCESSOR = "creditProcessor";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest}.
	 */
	public static final String RESERVE_REQUEST = "reserveRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest}.
	 */
	public static final String CHARGE_REQUEST = "chargeRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest}.
	 */
	public static final String MODIFY_RESERVATION_REQUEST = "modifyReservationRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest}.
	 */
	public static final String CANCEL_RESERVATION_REQUEST = "cancelReservationRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest}.
	 */
	public static final String CANCEL_ALL_RESERVATIONS_REQUEST = "cancelAllReservationsRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest}.
	 */
	public static final String CREDIT_REQUEST = "creditRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest}.
	 */
	public static final String MANUAL_CREDIT_REQUEST = "manualCreditRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest}.
	 */
	public static final String REVERSE_CHARGE_REQUEST = "reverseChargeRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO}.
	 */
	public static final String PIC_INSTRUCTIONS_DTO = "picInstructionsDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO}.
	 */
	public static final String PIC_INSTRUCTIONS_FIELDS_DTO = "picInstructionsFieldsDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO}.
	 */
	public static final String PIC_FIELDS_DTO = "paymentInstrumentCreationFieldsDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO}.
	 */
	public static final String PAYMENT_INSTRUMENT_DTO = "paymentInstrumentDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO}.
	 */
	public static final String ORDER_PAYMENT_INSTRUMENT_DTO = "orderPaymentInstrumentDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.event.PaymentEvent}.
	 */
	public static final String PAYMENT_EVENT = "paymentEvent";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO}.
	 */
	public static final String PAYMENT_PROVIDER_PLUGIN_DTO = "paymentProviderPluginDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.provider.PaymentProviderPluginsDTO}.
	 */
	public static final String PAYMENT_PROVIDER_PLUGINS_DTO = "paymentProviderPluginsDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO}.
	 */
	public static final String PAYMENT_PROVIDER_CONFIGURATION_DTO = "paymentProviderConfigDTO";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest}.
	 */
	public static final String RESERVE_CAPABILITY_REQUEST = "reserveCapabilityRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest}.
	 */
	public static final String CANCEL_CAPABILITY_REQUEST = "cancelCapabilityRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest}.
	 */
	public static final String MODIFY_CAPABILITY_REQUEST = "modifyCapabilityRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest}.
	 */
	public static final String CHARGE_CAPABILITY_REQUEST = "chargeCapabilityRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest}.
	 */
	public static final String REVERSE_CHARGE_CAPABILITY_REQUEST = "reverseChargeCapabilityRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest}.
	 */
	public static final String CREDIT_CAPABILITY_REQUEST = "creditCapabilityRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest}.
	 */
	public static final String PIC_INSTRUCTIONS_REQUEST = "paymentInstrumentCreationInstructionsRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest}.
	 */
	public static final String PIC_REQUEST = "paymentInstrumentCreationRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequest}.
	 */
	public static final String PAYMENT_EVENT_HISTORY_REQUEST = "paymentEventHistoryRequest";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponse}.
	 */
	public static final String PAYMENT_EVENT_HISTORY_RESPONSE = "paymentEventHistoryResponse";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.history.PaymentEventRelationshipRegistry}.
	 */
	public static final String PAYMENT_EVENT_RELATIONSHIP_REGISTRY = "paymentEventRelationshipRegistry";

	/**
	 * bean id for implementation of {@link com.elasticpath.provider.payment.service.history.PaymentEventChain}.
	 */
	public static final String PAYMENT_EVENT_CHAIN = "paymentEventChain";

	private PaymentProviderApiContextIdNames() {
		// Do not instantiate this class
	}
}
