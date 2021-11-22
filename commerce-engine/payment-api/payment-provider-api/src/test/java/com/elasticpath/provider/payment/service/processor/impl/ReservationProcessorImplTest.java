/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.processor.ReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
@RunWith(MockitoJUnitRunner.class)
public class ReservationProcessorImplTest extends AbstractProcessorImplTestBase {

	private static final PaymentCapabilityRequestFailedException PAYMENT_CAPABILITY_REQUEST_FAILED_EXCEPTION =
			new PaymentCapabilityRequestFailedException("InternalMessage", "ExternalMessage", true);

	private final PaymentProvider capablePaymentProvider = mock(PaymentProvider.class);
	private final ReserveCapability reserveCapability = mock(ReserveCapability.class);
	private final PaymentProviderConfiguration paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
	private final PaymentCapabilityResponse paymentCapabilityResponse = mockPaymentCapabilityResponse(RESERVATION_EVENT_DATA);
	@Mock
	private PaymentProviderConfigurationService paymentProviderConfigurationService;
	@Mock
	private PaymentProviderService paymentProviderService;
	@Mock
	private PaymentHistory paymentHistory;
	@Mock
	private PaymentAPIWorkflow paymentAPIWorkflow;
	private ReservationProcessor testee;

	@Override
	@Before
	public void setUp() throws PaymentCapabilityRequestFailedException {
		super.setUp();

		when(paymentProviderConfigurationService.findByGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID)).thenReturn(paymentProviderConfiguration);
		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(capablePaymentProvider);
		when(capablePaymentProvider.getCapability(ReserveCapability.class)).thenReturn(Optional.of(reserveCapability));
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(RESERVED_AMOUNT);

		testee = new ReservationProcessorImpl(paymentProviderConfigurationService,
				paymentProviderService, paymentHistory, new MoneyDtoCalculator(), paymentAPIWorkflow, getBeanFactory());
	}

	@Test
	public void reserveShouldRespondWithAcceptedEventAndInformationSuppliedByCapability() throws PaymentCapabilityRequestFailedException {
		final ReserveRequest reserveRequest = createReservationRequest();
		when(reserveCapability.reserve(any())).thenReturn(paymentCapabilityResponse);
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(reserveRequest.getAmount());

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		checkReservationResponse(response);
	}

	@Test
	public void reserveShouldRespondWithSkippedEventWhenCapabilityIsUnavailable() throws PaymentCapabilityRequestFailedException {
		final ReserveRequest reserveRequest = createReservationRequest();
		when(capablePaymentProvider.getCapability(ReserveCapability.class)).thenReturn(Optional.empty());
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(reserveRequest.getAmount());

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		assertThat(response.getEvents().size()).isEqualTo(1);
		assertThat(response.isSuccess()).isTrue();
		final PaymentEvent paymentEvent = response.getEvents().get(0);
		assertThat(paymentEvent.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent.getPaymentStatus()).isEqualTo(SKIPPED);
		assertThat(paymentEvent.getAmount().getAmount()).isEqualTo(RESERVED_AMOUNT.getAmount());
		verify(reserveCapability, never()).reserve(any());
	}

	@Test
	public void reserveShouldRespondWithFailedEventWhenCapabilityThrowsException() throws PaymentCapabilityRequestFailedException {
		final ReserveRequest reserveRequest = createReservationRequest();
		when(reserveCapability.reserve(any())).thenThrow(PAYMENT_CAPABILITY_REQUEST_FAILED_EXCEPTION);
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(createMoney(BigDecimal.ZERO));

		final PaymentAPIResponse response = testee.reserve(reserveRequest);

		assertThat(response.getEvents().size()).isEqualTo(1);
		assertThat(response.isSuccess()).isFalse();
		final PaymentEvent paymentEvent = response.getEvents().get(0);
		assertThat(paymentEvent.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent.getPaymentStatus()).isEqualTo(FAILED);
		assertThat(paymentEvent.getAmount().getAmount()).isEqualTo(RESERVED_AMOUNT.getAmount());
		assertThat(paymentEvent.getExternalMessage()).isEqualTo(PAYMENT_CAPABILITY_REQUEST_FAILED_EXCEPTION.getExternalMessage());
		assertThat(paymentEvent.getInternalMessage()).isEqualTo(PAYMENT_CAPABILITY_REQUEST_FAILED_EXCEPTION.getInternalMessage());
	}

	@Test
	public void reserveToSimulateModifyShouldRespondWithAcceptedEventAndInformationSuppliedByCapability()
			throws PaymentCapabilityRequestFailedException {
		final ReserveRequest reserveRequest = createReservationRequest();
		when(reserveCapability.reserve(any())).thenReturn(paymentCapabilityResponse);
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(reserveRequest.getAmount());

		final PaymentAPIResponse response = testee.reserveToSimulateModify(reserveRequest.getAmount(),
				reserveRequest.getSelectedOrderPaymentInstruments().get(0), reserveRequest.getCustomRequestData(), reserveRequest.getOrderContext(),
				0);

		checkReservationResponse(response);
	}

	@Test
	public void reserveToSimulateModifyShouldRespondWithSkippedEventWhenCapabilityIsUnavailable() throws PaymentCapabilityRequestFailedException {
		final ReserveRequest reserveRequest = createReservationRequest();
		when(capablePaymentProvider.getCapability(ReserveCapability.class)).thenReturn(Optional.empty());
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(reserveRequest.getAmount());

		final PaymentAPIResponse response = testee.reserveToSimulateModify(reserveRequest.getAmount(),
				reserveRequest.getSelectedOrderPaymentInstruments().get(0), reserveRequest.getCustomRequestData(), reserveRequest.getOrderContext(),
				0);

		assertThat(response.getEvents().size()).isEqualTo(1);
		assertThat(response.isSuccess()).isTrue();
		final PaymentEvent paymentEvent = response.getEvents().get(0);
		assertThat(paymentEvent.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent.getPaymentStatus()).isEqualTo(SKIPPED);
		assertThat(paymentEvent.getAmount().getAmount()).isEqualTo(RESERVED_AMOUNT.getAmount());
		verify(reserveCapability, never()).reserve(any());
	}

	@Test
	public void reserveToSimulateModifyShouldRespondWithFailedEventWhenCapabilityThrowsException() throws PaymentCapabilityRequestFailedException {
		final ReserveRequest reserveRequest = createReservationRequest();
		when(reserveCapability.reserve(any())).thenThrow(PAYMENT_CAPABILITY_REQUEST_FAILED_EXCEPTION);
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(createMoney(BigDecimal.ZERO));

		final PaymentAPIResponse response = testee.reserveToSimulateModify(reserveRequest.getAmount(),
				reserveRequest.getSelectedOrderPaymentInstruments().get(0), reserveRequest.getCustomRequestData(), reserveRequest.getOrderContext(),
				0);

		assertThat(response.getEvents().size()).isEqualTo(1);
		assertThat(response.isSuccess()).isFalse();
		final PaymentEvent paymentEvent = response.getEvents().get(0);
		assertThat(paymentEvent.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent.getPaymentStatus()).isEqualTo(FAILED);
		assertThat(paymentEvent.getAmount().getAmount()).isEqualTo(RESERVED_AMOUNT.getAmount());
		assertThat(paymentEvent.getExternalMessage()).isEqualTo(PAYMENT_CAPABILITY_REQUEST_FAILED_EXCEPTION.getExternalMessage());
		assertThat(paymentEvent.getInternalMessage()).isEqualTo(PAYMENT_CAPABILITY_REQUEST_FAILED_EXCEPTION.getInternalMessage());
	}
}
