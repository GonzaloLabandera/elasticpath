/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.processor.CancelReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
@RunWith(MockitoJUnitRunner.class)
public class CancelReservationProcessorImplTest extends AbstractProcessorImplTestBase {

	@Mock
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Mock
	private PaymentProviderService paymentProviderService;

	@Mock
	private PaymentHistory paymentHistory;

	@Mock
	private PaymentAPIWorkflow paymentAPIWorkflow;

	@Captor
	private ArgumentCaptor<CancelCapabilityRequest> cancelArgumentCaptor;

	private CancelReservationProcessor testee;

	private final PaymentProvider capablePaymentProvider = mock(PaymentProvider.class);
	private final CancelCapability cancelCapability = mock(CancelCapability.class);
	private final PaymentProviderConfiguration paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
	private final PaymentCapabilityResponse paymentCapabilityResponse = mockPaymentCapabilityResponse(RESPONSE_DATA);
	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Before
	public void setUp() throws PaymentCapabilityRequestFailedException {
		super.setUp();

		when(paymentProviderConfigurationService.findByGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID)).thenReturn(paymentProviderConfiguration);
		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(capablePaymentProvider);
		when(capablePaymentProvider.getCapability(CancelCapability.class)).thenReturn(Optional.of(cancelCapability));
		when(cancelCapability.cancel(any())).thenReturn(paymentCapabilityResponse);

		when(paymentHistory.getAvailableReservedAmount(any())).thenAnswer((Answer<MoneyDTO>) invocation -> {
			final MoneyDTO reservedAmount = moneyDtoCalculator.zeroMoneyDto();
			invocation.<List<PaymentEvent>>getArgument(0)
					.stream()
					.filter(paymentEvent -> paymentEvent.getPaymentType() == RESERVE)
					.filter(paymentEvent -> paymentEvent.getPaymentStatus() == APPROVED)
					.forEach(paymentEvent -> moneyDtoCalculator.increase(reservedAmount, paymentEvent.getAmount()));
			invocation.<List<PaymentEvent>>getArgument(0)
					.stream()
					.filter(paymentEvent -> paymentEvent.getPaymentType() == CANCEL_RESERVE)
					.filter(paymentEvent -> paymentEvent.getPaymentStatus() == APPROVED)
					.forEach(paymentEvent -> moneyDtoCalculator.decrease(reservedAmount, paymentEvent.getAmount()));
			return moneyDtoCalculator.isPositive(reservedAmount) ? reservedAmount : moneyDtoCalculator.zeroMoneyDto();
		});

		testee = new CancelReservationProcessorImpl(paymentProviderConfigurationService,
				paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, getBeanFactory());
	}

	@Test
	public void cancelReservationGeneratesApprovedEventWhenCapabilityIsSupported() throws PaymentCapabilityRequestFailedException {
		final CancelReservationRequest cancelReservationRequest = createCancelReservationRequest();
		cancelReservationRequest.setSelectedPaymentEventsToCancel(cancelReservationRequest.getLedger());
		cancelReservationRequest.setAmount(RESERVED_AMOUNT);

		final PaymentAPIResponse response = testee.cancelReservation(cancelReservationRequest);

		checkSinglePaymentEventApproved(response);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(CANCEL_RESERVE);
		verify(cancelCapability).cancel(cancelArgumentCaptor.capture());
		checkCapabilityRequest(cancelArgumentCaptor.getValue(), RESERVED_AMOUNT.getAmount());
	}

	@Test
	public void cancelReservationGeneratesFailedEventWhenCapabilityThrowsException() throws PaymentCapabilityRequestFailedException {
		when(cancelCapability.cancel(any())).thenThrow(CAPABILITY_EXCEPTION);
		final CancelReservationRequest cancelReservationRequest = createCancelReservationRequest();
		cancelReservationRequest.setSelectedPaymentEventsToCancel(cancelReservationRequest.getLedger());
		cancelReservationRequest.setAmount(RESERVED_AMOUNT);

		final PaymentAPIResponse response = testee.cancelReservation(cancelReservationRequest);

		checkSinglePaymentEventFailed(response);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(CANCEL_RESERVE);
		verify(cancelCapability).cancel(cancelArgumentCaptor.capture());
		checkCapabilityRequest(cancelArgumentCaptor.getValue(), RESERVED_AMOUNT.getAmount());
	}

	@Test
	public void cancelReservationGeneratesSkippedEventWhenCapabilityIsUnsupported() throws PaymentCapabilityRequestFailedException {
		when(capablePaymentProvider.getCapability(CancelCapability.class)).thenReturn(Optional.empty());
		final CancelReservationRequest cancelReservationRequest = createCancelReservationRequest();
		cancelReservationRequest.setSelectedPaymentEventsToCancel(cancelReservationRequest.getLedger());
		cancelReservationRequest.setAmount(RESERVED_AMOUNT);

		final PaymentAPIResponse response = testee.cancelReservation(cancelReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(false);
		checkSinglePaymentEventSkipped(response);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(CANCEL_RESERVE);
		verify(cancelCapability, never()).cancel(any());
	}

	@Test
	public void cancelAllReservationGeneratesApprovedEventWhenCancelCapabilityIsSupported() {
		final CancelAllReservationsRequest cancelReservationRequest = createCancelAllReservationsRequest();
		mockPaymentHistoryChargeablePaymentEvents(Arrays.asList(createReservationEvent(createOrderPaymentInstrumentDTO(),
				RESERVED_AMOUNT), createReservationEvent(createOrderPaymentInstrumentDTO(),
				RESERVED_AMOUNT)));
		mockCancelCapabilitySuccess();
		final PaymentAPIResponse response = testee.cancelAllReservations(cancelReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(RESERVED_AMOUNT.getAmount(), RESERVED_AMOUNT.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsOnly(CANCEL_RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void cancelAllReservationGeneratesFailedEventWhenCancelCapabilityThrowsException() {
		final CancelAllReservationsRequest cancelReservationRequest = createCancelAllReservationsRequest();
		mockPaymentHistoryChargeablePaymentEvents(Arrays.asList(createReservationEvent(createOrderPaymentInstrumentDTO(),
				RESERVED_AMOUNT), createReservationEvent(createOrderPaymentInstrumentDTO(),
				RESERVED_AMOUNT)));
		mockCancelCapabilityFailure();
		final PaymentAPIResponse response = testee.cancelAllReservations(cancelReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(RESERVED_AMOUNT.getAmount(), RESERVED_AMOUNT.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsOnly(CANCEL_RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED, FAILED);
	}

	@Test
	public void cancelAllReservationGeneratesSkippedEventWhenCancelCapabilityIsUnsupported() {
		final CancelAllReservationsRequest cancelReservationRequest = createCancelAllReservationsRequest();
		mockPaymentHistoryChargeablePaymentEvents(Arrays.asList(createReservationEvent(createOrderPaymentInstrumentDTO(),
				RESERVED_AMOUNT), createReservationEvent(createOrderPaymentInstrumentDTO(),
				RESERVED_AMOUNT)));
		mockCancelCapabilitySkip();
		final PaymentAPIResponse response = testee.cancelAllReservations(cancelReservationRequest);

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(RESERVED_AMOUNT.getAmount(), RESERVED_AMOUNT.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsOnly(CANCEL_RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(SKIPPED, SKIPPED);
	}

	private void mockCancelCapabilitySuccess() {
		when(paymentAPIWorkflow.cancelReservation(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			CancelReservationRequest request = (CancelReservationRequest) invocation.getArguments()[0];
			return simulateCancel(request.getSelectedPaymentEventsToCancel());
		});
	}

	private void mockCancelCapabilitySkip() {
		when(paymentAPIWorkflow.cancelReservation(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			CancelReservationRequest request = (CancelReservationRequest) invocation.getArguments()[0];
			return simulateCancelSkip(request.getSelectedPaymentEventsToCancel());
		});
	}

	private void mockCancelCapabilityFailure() {
		when(paymentAPIWorkflow.cancelReservation(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			CancelReservationRequest request = (CancelReservationRequest) invocation.getArguments()[0];
			return simulateCancelFailure(request.getSelectedPaymentEventsToCancel());
		});
	}

	private void mockPaymentHistoryChargeablePaymentEvents(final List<PaymentEvent> newLedger) {
		final Multimap<PaymentEvent, MoneyDTO> paymentEvents = ArrayListMultimap.create();
		for (PaymentEvent paymentEvent : newLedger) {
			paymentEvents.put(paymentEvent, paymentEvent.getAmount());
		}
		when(paymentHistory.getChargeablePaymentEvents(any())).thenReturn(paymentEvents);
	}

}