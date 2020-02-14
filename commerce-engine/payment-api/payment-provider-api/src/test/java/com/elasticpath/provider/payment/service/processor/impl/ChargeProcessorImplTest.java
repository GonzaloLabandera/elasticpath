/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.processor.ChargeProcessor;
import com.elasticpath.provider.payment.service.processor.ReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

@RunWith(MockitoJUnitRunner.class)
public class ChargeProcessorImplTest extends AbstractProcessorImplTestBase {

	private static final MoneyDTO ZERO = createMoney(BigDecimal.ZERO);
	private static final MoneyDTO TEN = createMoney(BigDecimal.TEN);
	private static final MoneyDTO TWENTY = createMoney(BigDecimal.valueOf(20));
	private static final MoneyDTO THIRTY = createMoney(BigDecimal.valueOf(30));
	private static final MoneyDTO FIFTY = createMoney(BigDecimal.valueOf(50));
	private static final MoneyDTO SEVENTY = createMoney(BigDecimal.valueOf(70));
	private static final MoneyDTO ONE_HUNDRED = createMoney(BigDecimal.valueOf(100));

	@Mock
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Mock
	private PaymentProviderService paymentProviderService;

	@Mock
	private PaymentHistory paymentHistory;

	@Mock
	private PaymentAPIWorkflow paymentAPIWorkflow;

	@Mock
	private ReservationProcessor reservationProcessor;

	@Captor
	private ArgumentCaptor<ChargeCapabilityRequest> chargeArgumentCaptor;

	@Captor
	private ArgumentCaptor<CancelReservationRequest> cancelReservationRequestArgumentCaptor;

	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	private ChargeProcessor testee;

	private final PaymentProvider capablePaymentProvider = mock(PaymentProvider.class);
	private final ChargeCapability chargeCapability = mock(ChargeCapability.class);
	private final PaymentProviderConfiguration paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
	private final PaymentCapabilityResponse paymentCapabilityResponse = mockPaymentCapabilityResponse(RESPONSE_DATA);

	@Override
	@Before
	public void setUp() throws PaymentCapabilityRequestFailedException {
		super.setUp();

		when(paymentProviderConfigurationService.findByGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID)).thenReturn(paymentProviderConfiguration);
		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(capablePaymentProvider);

		when(capablePaymentProvider.getCapability(ChargeCapability.class)).thenReturn(Optional.of(chargeCapability));

		when(chargeCapability.charge(any())).thenReturn(paymentCapabilityResponse);

		when(paymentAPIWorkflow.cancelReservation(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			CancelReservationRequest request = (CancelReservationRequest) invocation.getArguments()[0];
			return simulateCancel(request.getSelectedPaymentEventsToCancel());
		});
		when(paymentAPIWorkflow.reserve(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			ReserveRequest request = (ReserveRequest) invocation.getArguments()[0];
			final PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), request.getAmount());
			return new PaymentAPIResponse(Collections.singletonList(reservationEvent), true);
		});
		when(reservationProcessor.reserveToSimulateModify(any(MoneyDTO.class), any(OrderPaymentInstrumentDTO.class),
				anyMap(), any(OrderContext.class))).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
					final MoneyDTO amount = (MoneyDTO) invocation.getArguments()[0];
					final OrderPaymentInstrumentDTO instrument = (OrderPaymentInstrumentDTO) invocation.getArguments()[1];
					final PaymentEvent reservationEvent = createReservationEvent(instrument, amount);
					return new PaymentAPIResponse(Collections.singletonList(reservationEvent), true);
				}
		);

		testee = new ChargeProcessorImpl(paymentProviderConfigurationService,
				paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, reservationProcessor, getBeanFactory());
	}

	@Test
	public void testChargePaymentSendsDownPaymentEventData() throws PaymentCapabilityRequestFailedException {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(TWENTY);
		simulateChargeSuccessInPaymentHistory(chargeRequest, TWENTY);

		final PaymentAPIResponse response = testee.chargePayment(chargeRequest);

		assertThat(response.getEvents().size()).isEqualTo(1);
		assertThat(response.isSuccess()).isTrue();
		verify(chargeCapability).charge(chargeArgumentCaptor.capture());
		ChargeCapabilityRequest chargeCapabilityRequest = chargeArgumentCaptor.getValue();
		assertThat(chargeCapabilityRequest.getReservationData()).isEqualTo(RESERVATION_EVENT_DATA);
	}

	@Test
	public void shouldThrowExceptionThenDoChargeWhenNotContainEnoughReservedAmount() {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(ONE_HUNDRED);
		simulateChargeSuccessInPaymentHistory(chargeRequest, ONE_HUNDRED);
		when(paymentHistory.getChargedAmount(any())).thenReturn(ZERO);
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(ZERO);
		when(paymentAPIWorkflow.modifyReservation(any())).thenReturn(new PaymentAPIResponse(Collections.emptyList(), true));

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);
		assertThat(paymentAPIResponse.isSuccess()).isFalse();
	}

	@Test
	public void shouldApproveCharge100WhenThereIsSuccessfulReservation100() {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(ONE_HUNDRED);
		simulateChargeSuccessInPaymentHistory(chargeRequest, ONE_HUNDRED);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		final List<PaymentEvent> resultingPaymentEvents = paymentAPIResponse.getEvents();
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentType).containsExactly(CHARGE);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount).containsExactly(ONE_HUNDRED.getAmount());
	}

	@Test
	public void shouldApproveCharge30AndReReserve70WhenThereIsSuccessfulReservation100() {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(ONE_HUNDRED);
		simulateChargeSuccessInPaymentHistory(chargeRequest, THIRTY);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		final List<PaymentEvent> resultingPaymentEvents = paymentAPIResponse.getEvents();
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentType).containsExactly(CHARGE, RESERVE);
		assertThat(resultingPaymentEvents).filteredOn(paymentEvent -> Objects.equals(CHARGE, paymentEvent.getPaymentType()))
				.extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsExactly(THIRTY.getAmount());
		assertThat(resultingPaymentEvents).filteredOn(paymentEvent -> Objects.equals(RESERVE, paymentEvent.getPaymentType()))
				.extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsExactly(SEVENTY.getAmount());
	}

	@Test
	public void shouldApproveCharge30AndReReserve70WhenThereIsSuccessfulReservation100AndThisIsLastCharge() {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(ONE_HUNDRED);
		simulateChargeSuccessInPaymentHistory(chargeRequest, THIRTY);
		chargeRequest.setFinalPayment(true);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		final List<PaymentEvent> resultingPaymentEvents = paymentAPIResponse.getEvents();
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentType).containsExactly(CHARGE);
		assertThat(resultingPaymentEvents).filteredOn(paymentEvent -> Objects.equals(CHARGE, paymentEvent.getPaymentType()))
				.extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsExactly(THIRTY.getAmount());
	}

	@Test
	public void shouldApproveCharge100WhenThereAreTwoSuccessfulReservations50() {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(FIFTY, FIFTY);
		simulateChargeSuccessInPaymentHistory(chargeRequest, ONE_HUNDRED);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		final List<PaymentEvent> resultingPaymentEvents = paymentAPIResponse.getEvents();
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentType).containsExactly(CHARGE, CHARGE);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount).containsExactly(FIFTY.getAmount(), FIFTY.getAmount());
	}

	@Test
	public void shouldApproveCharge50WhenThereAreTwoSuccessfulReservations50() throws PaymentCapabilityRequestFailedException {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(FIFTY, FIFTY);
		simulateChargeSuccessInPaymentHistory(chargeRequest, FIFTY);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		final List<PaymentEvent> resultingPaymentEvents = paymentAPIResponse.getEvents();
		assertThat(resultingPaymentEvents).hasSize(1);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentStatus).containsExactly(APPROVED);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentType).containsExactly(CHARGE);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getAmount).extracting(MoneyDTO::getAmount).containsExactly(FIFTY.getAmount());

		verify(chargeCapability, times(1)).charge(chargeArgumentCaptor.capture());
		final ChargeCapabilityRequest chargeCapabilityRequest = chargeArgumentCaptor.getValue();
		assertThat(chargeCapabilityRequest.getAmount()).extracting(MoneyDTO::getAmount).isEqualTo(FIFTY.getAmount());
		verifyZeroInteractions(paymentAPIWorkflow);
	}

	@Test
	public void shouldApproveCharge50WhenThereAreTwoSuccessfulReservations30Charging30And20AndReserving10() {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(THIRTY, THIRTY);
		simulateChargeSuccessInPaymentHistory(chargeRequest, FIFTY);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
		final List<PaymentEvent> resultingPaymentEvents = paymentAPIResponse.getEvents();
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentStatus).containsOnly(APPROVED);
		assertThat(resultingPaymentEvents).extracting(PaymentEvent::getPaymentType).containsExactly(CHARGE, CHARGE, RESERVE);
		assertThat(resultingPaymentEvents)
				.filteredOn(paymentEvent -> Objects.equals(CHARGE, paymentEvent.getPaymentType()))
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THIRTY.getAmount(), TWENTY.getAmount());
		assertThat(resultingPaymentEvents)
				.filteredOn(paymentEvent -> Objects.equals(RESERVE, paymentEvent.getPaymentType()))
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN.getAmount());
	}

	@Test
	public void amountInCancelReservationRequestIsEqualToAmountInChargeRequest() throws PaymentCapabilityRequestFailedException {
		final ChargeRequest chargeRequest = createChargeRequestSupportedByReservationEventsInPaymentHistory(ONE_HUNDRED);
		simulateChargeSuccessInPaymentHistory(chargeRequest, FIFTY);
		when(chargeCapability.charge(any())).thenThrow(PaymentCapabilityRequestFailedException.class);

		testee.chargePayment(chargeRequest);

		verify(paymentAPIWorkflow).cancelReservation(cancelReservationRequestArgumentCaptor.capture());
		final CancelReservationRequest cancelRequest = cancelReservationRequestArgumentCaptor.getValue();
		assertThat(cancelRequest.getAmount().getAmount()).isEqualTo(ONE_HUNDRED.getAmount());
		assertThat(cancelRequest.getSelectedPaymentEventsToCancel().get(0).getAmount().getAmount()).isEqualTo(ONE_HUNDRED.getAmount());
	}

	@Test
	public void paymentAPIResponseShouldBeTrueWhenChargeRequestForFiftyAndChargedAmountIsOneHundredAndReverseChargedAmountIsFifty() {
		final PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), FIFTY);
		final PaymentEvent chargeEvent = createChargeEvent(firstReservationEvent, FIFTY);
		final PaymentEvent reverseChargeEvent = createReverseChargeEvent(chargeEvent);
		final PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), FIFTY);

		when(paymentHistory.getAvailableReservedAmount(anyList())).thenReturn(FIFTY);

		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = Multimaps.forMap(Collections.singletonMap(secondReservationEvent, FIFTY));
		when(paymentHistory.getChargeablePaymentEvents(Arrays.asList(firstReservationEvent, chargeEvent, reverseChargeEvent,
				secondReservationEvent)))
				.thenReturn(chargeablePaymentEvents);

		final ChargeRequest chargeRequest =
				createChargeRequest(Arrays.asList(firstReservationEvent, chargeEvent, reverseChargeEvent, secondReservationEvent), FIFTY);
		when(paymentHistory.getChargedAmount(anyList())).thenReturn(ZERO).thenReturn(FIFTY);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
	}

	@Test
	public void paymentAPIResponseShouldBeTrueWhenChargeRequestForFiftyAndChargedAmountIsFiftyAndReverseChargedAmountIsZero() {
		final PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), FIFTY);

		when(paymentHistory.getAvailableReservedAmount(anyList())).thenReturn(FIFTY);

		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = Multimaps.forMap(Collections.singletonMap(reservationEvent, FIFTY));
		when(paymentHistory.getChargeablePaymentEvents(Collections.singletonList(reservationEvent))).thenReturn(chargeablePaymentEvents);

		final ChargeRequest chargeRequest = createChargeRequest(Collections.singletonList(reservationEvent), FIFTY);
		when(paymentHistory.getChargedAmount(anyList())).thenReturn(ZERO).thenReturn(FIFTY);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isTrue();
	}

	@Test
	public void paymentAPIResponseShouldBeFalseWhenChargeRequestForFiftyAndChargedAmountIsZeroAndReverseChargedAmountIsZero() {
		final PaymentEvent reservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), FIFTY);

		when(paymentHistory.getAvailableReservedAmount(anyList())).thenReturn(FIFTY);

		final Multimap<PaymentEvent, MoneyDTO> chargeablePaymentEvents = Multimaps.forMap(Collections.singletonMap(reservationEvent, FIFTY));
		when(paymentHistory.getChargeablePaymentEvents(Collections.singletonList(reservationEvent))).thenReturn(chargeablePaymentEvents);

		final ChargeRequest chargeRequest = createChargeRequest(Collections.singletonList(reservationEvent), FIFTY);
		when(paymentHistory.getChargedAmount(anyList())).thenReturn(ZERO);

		final PaymentAPIResponse paymentAPIResponse = testee.chargePayment(chargeRequest);

		assertThat(paymentAPIResponse.isSuccess()).isFalse();
	}

	private ChargeRequest createChargeRequestSupportedByReservationEventsInPaymentHistory(final MoneyDTO... amounts) {
		final MoneyDTO orderTotal = moneyDtoCalculator.zeroMoneyDto();
		final Multimap<PaymentEvent, MoneyDTO> eventsMultimap = ArrayListMultimap.create();
		for (MoneyDTO amount : amounts) {
			eventsMultimap.put(createReservationEvent(createOrderPaymentInstrumentDTO(), amount), amount);
			moneyDtoCalculator.increase(orderTotal, amount);
		}

		when(paymentHistory.getChargeablePaymentEvents(any())).thenReturn(eventsMultimap);
		when(paymentHistory.getAvailableReservedAmount(any())).thenReturn(orderTotal);

		final OrderContext orderContext = new OrderContext();
		orderContext.setOrderTotal(orderTotal);
		orderContext.setOrderNumber("12345");

		final ChargeRequest chargeRequest = new ChargeRequest();
		chargeRequest.setLedger(new ArrayList<>(eventsMultimap.keys()));
		chargeRequest.setOrderContext(orderContext);
		chargeRequest.setCustomRequestData(Collections.emptyMap());
		chargeRequest.setFinalPayment(false);
		chargeRequest.setOrderPaymentInstruments(Collections.emptyList());
		return chargeRequest;
	}

	private void simulateChargeSuccessInPaymentHistory(final ChargeRequest chargeRequest, final MoneyDTO amount) {
		chargeRequest.setTotalChargeableAmount(amount);
		when(paymentHistory.getChargedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto()).thenReturn(amount);
		lenient().when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());
	}

}