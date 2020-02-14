/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow.impl;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_RESERVATION_PROCESSOR;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CHARGE_PROCESSOR;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CREDIT_PROCESSOR;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MODIFY_RESERVATION_PROCESSOR;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_PROCESSOR;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.RESERVATION_PROCESSOR;

import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.provider.payment.service.processor.CancelReservationProcessor;
import com.elasticpath.provider.payment.service.processor.ChargeProcessor;
import com.elasticpath.provider.payment.service.processor.CreditProcessor;
import com.elasticpath.provider.payment.service.processor.ModifyReservationProcessor;
import com.elasticpath.provider.payment.service.processor.PaymentInstrumentCreationProcessor;
import com.elasticpath.provider.payment.service.processor.ReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

/**
 * Default implementation of {@link PaymentAPIWorkflow}.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.GodClass"})
public class PaymentAPIWorkflowImpl implements PaymentAPIWorkflow {

	private final BeanFactory beanFactory;
	private final PaymentProviderService paymentProviderService;
	private final PaymentProviderConfigurationService paymentProviderConfigurationService;

	/**
	 * Constructor.
	 *
	 * @param beanFactory                         EP bean factory
	 * @param paymentProviderService              payment provider service
	 * @param paymentProviderConfigurationService payment provider configuration service
	 */
	public PaymentAPIWorkflowImpl(final BeanFactory beanFactory,
								  final PaymentProviderService paymentProviderService,
								  final PaymentProviderConfigurationService paymentProviderConfigurationService) {
		this.beanFactory = beanFactory;
		this.paymentProviderService = paymentProviderService;
		this.paymentProviderConfigurationService = paymentProviderConfigurationService;
	}

	@Override
	public PICInstructionsFieldsDTO getPICInstructionFields(final String paymentProviderConfigurationGuid,
															final PICFieldsRequestContextDTO context) {
		return beanFactory.getSingletonBean(PIC_PROCESSOR, PaymentInstrumentCreationProcessor.class)
				.getPICInstructionFields(paymentProviderConfigurationGuid, context);
	}

	@Override
	public PICInstructionsDTO getPICInstructions(final String configurationGuid,
												 final Map<String, String> instructionsMap,
												 final PICRequestContextDTO context) {
		return beanFactory.getSingletonBean(PIC_PROCESSOR, PaymentInstrumentCreationProcessor.class)
				.getPICInstructions(configurationGuid, instructionsMap, context);
	}

	@Override
	public PaymentInstrumentCreationFieldsDTO getPICFields(final String paymentProviderConfigurationGuid,
														   final PICFieldsRequestContextDTO context) {
		return beanFactory.getSingletonBean(PIC_PROCESSOR, PaymentInstrumentCreationProcessor.class)
				.getPICFields(paymentProviderConfigurationGuid, context);
	}

	@Override
	public String createPI(final String configurationGuid,
						   final Map<String, String> instrumentMap,
						   final PICRequestContextDTO context) {
		return beanFactory.getSingletonBean(PIC_PROCESSOR, PaymentInstrumentCreationProcessor.class)
				.createPI(configurationGuid, instrumentMap, context);
	}

	@Override
	public PaymentAPIResponse reserve(final ReserveRequest reserveRequest) {
		return beanFactory.getSingletonBean(RESERVATION_PROCESSOR, ReservationProcessor.class).reserve(reserveRequest);
	}

	@Override
	public PaymentAPIResponse cancelReservation(final CancelReservationRequest cancelRequest) {
		return beanFactory.getSingletonBean(CANCEL_RESERVATION_PROCESSOR, CancelReservationProcessor.class).cancelReservation(cancelRequest);
	}

	@Override
	public PaymentAPIResponse cancelAllReservations(final CancelAllReservationsRequest cancelAllReservationsRequest) {
		return beanFactory.getSingletonBean(CANCEL_RESERVATION_PROCESSOR, CancelReservationProcessor.class)
				.cancelAllReservations(cancelAllReservationsRequest);
	}

	@Override
	public PaymentAPIResponse modifyReservation(final ModifyReservationRequest modifyRequest) {
		return beanFactory.getSingletonBean(MODIFY_RESERVATION_PROCESSOR, ModifyReservationProcessor.class).modifyReservation(modifyRequest);
	}

	@Override
	public PaymentAPIResponse chargePayment(final ChargeRequest chargeRequest) {
		return beanFactory.getSingletonBean(CHARGE_PROCESSOR, ChargeProcessor.class).chargePayment(chargeRequest);
	}

	@Override
	public PaymentAPIResponse credit(final CreditRequest creditRequest) {
		return beanFactory.getSingletonBean(CREDIT_PROCESSOR, CreditProcessor.class).credit(creditRequest);
	}

	@Override
	public PaymentAPIResponse manualCredit(final ManualCreditRequest creditRequest) {
		return beanFactory.getSingletonBean(CREDIT_PROCESSOR, CreditProcessor.class).manualCredit(creditRequest);
	}

	@Override
	public PaymentAPIResponse reverseCharge(final ReverseChargeRequest reverseChargeRequest) {
		return beanFactory.getSingletonBean(CREDIT_PROCESSOR, CreditProcessor.class).reverseCharge(reverseChargeRequest);
	}

	@Override
	public boolean requiresBillingAddress(final String configurationGuid) {
		return getPaymentProvider(configurationGuid).isBillingAddressRequired();
	}

	/**
	 * Associates {@link PaymentProviderConfiguration} with the plugin essentially creating a {@link PaymentProvider} instance.
	 *
	 * @param configurationGuid payment provider configuration id
	 * @return payment provider
	 */
	protected PaymentProvider getPaymentProvider(final String configurationGuid) {
		final PaymentProviderConfiguration paymentProviderConfiguration = paymentProviderConfigurationService.findByGuid(configurationGuid);
		if (paymentProviderConfiguration == null) {
			throw new IllegalStateException("Configuration with GUID " + configurationGuid + " is missing");
		}
		return paymentProviderService.createProvider(paymentProviderConfiguration);
	}

}
