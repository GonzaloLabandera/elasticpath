/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
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
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.processor.ModifyReservationProcessor;
import com.elasticpath.provider.payment.service.processor.ReservationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
@RunWith(MockitoJUnitRunner.class)
public class ModifyReservationProcessorImplTest extends AbstractProcessorImplTestBase {

	private static final BigDecimal THREE = BigDecimal.valueOf(3);
	private static final BigDecimal FIVE = BigDecimal.valueOf(5);
	private static final BigDecimal EIGHT = BigDecimal.valueOf(8);
	private static final BigDecimal FIFTEEN = BigDecimal.valueOf(15);
	private static final BigDecimal THIRTEEN = BigDecimal.valueOf(13);
	private static final BigDecimal EIGHTEEN = BigDecimal.valueOf(18);

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
	private ArgumentCaptor<ModifyCapabilityRequest> modifyCapabilityRequestArgumentCaptor;

	@Captor
	private ArgumentCaptor<CancelReservationRequest> cancelReservationRequestArgumentCaptor;

	@Captor
	private ArgumentCaptor<ReserveRequest> reserveRequestArgumentCaptor;

	private ModifyReservationProcessor testee;

	private final PaymentProvider capablePaymentProvider = mock(PaymentProvider.class);
	private final ModifyCapability modifyCapability = mock(ModifyCapability.class);
	private final ReserveCapability reserveCapability = mock(ReserveCapability.class);
	private final CancelCapability cancelCapability = mock(CancelCapability.class);
	private final PaymentProviderConfiguration paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
	private final PaymentCapabilityResponse paymentCapabilityResponse = mockPaymentCapabilityResponse(RESPONSE_DATA);
	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Before
	public void setUp() throws PaymentCapabilityRequestFailedException {
		super.setUp();

		when(paymentProviderConfigurationService.findByGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID)).thenReturn(paymentProviderConfiguration);
		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(capablePaymentProvider);
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.of(modifyCapability));
		lenient().when(capablePaymentProvider.getCapability(ReserveCapability.class)).thenReturn(Optional.of(reserveCapability));
		lenient().when(capablePaymentProvider.getCapability(CancelCapability.class)).thenReturn(Optional.of(cancelCapability));
		when(modifyCapability.modify(any())).thenReturn(paymentCapabilityResponse);

		when(paymentHistory.getChargedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());
		mockPaymentHistoryChargeablePaymentEvents(Collections.singletonList(
				createReservationEvent(createOrderPaymentInstrumentDTO(), RESERVED_AMOUNT)));
		when(paymentHistory.getAvailableReservedAmount(any())).thenAnswer((Answer<MoneyDTO>) invocation -> {
			final MoneyDTO reservedAmount = moneyDtoCalculator.zeroMoneyDto();
			final MoneyDTO modifiedAmount = moneyDtoCalculator.zeroMoneyDto();
			invocation.<List<PaymentEvent>>getArgument(0)
					.stream()
					.filter(paymentEvent -> paymentEvent.getPaymentType() == RESERVE)
					.filter(paymentEvent -> paymentEvent.getPaymentStatus() != FAILED)
					.forEach(paymentEvent -> moneyDtoCalculator.increase(reservedAmount, paymentEvent.getAmount()));
			invocation.<List<PaymentEvent>>getArgument(0)
					.stream()
					.filter(paymentEvent -> paymentEvent.getPaymentType() == CANCEL_RESERVE)
					.forEach(paymentEvent -> moneyDtoCalculator.decrease(reservedAmount, paymentEvent.getAmount()));
			invocation.<List<PaymentEvent>>getArgument(0)
					.stream()
					.filter(paymentEvent -> paymentEvent.getPaymentType() == MODIFY_RESERVE)
					.filter(paymentEvent -> paymentEvent.getPaymentStatus() != FAILED)
					.forEach(paymentEvent -> moneyDtoCalculator.increase(modifiedAmount, paymentEvent.getAmount()));
			if (moneyDtoCalculator.hasBalance(modifiedAmount)) {
				return modifiedAmount;
			}
			return moneyDtoCalculator.isPositive(reservedAmount) ? reservedAmount : moneyDtoCalculator.zeroMoneyDto();
		});

		testee = new ModifyReservationProcessorImpl(paymentProviderConfigurationService,
				paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, reservationProcessor, getBeanFactory());
	}

	private void mockPaymentHistoryChargeablePaymentEvents(final List<PaymentEvent> newLedger) {
		final Multimap<PaymentEvent, MoneyDTO> paymentEvents = ArrayListMultimap.create();
		for (PaymentEvent paymentEvent : newLedger) {
			paymentEvents.put(paymentEvent, paymentEvent.getAmount());
		}
		when(paymentHistory.getChargeablePaymentEvents(any())).thenReturn(paymentEvents);
	}

	private void mockReservationCapabilitySuccess() {
		when(paymentAPIWorkflow.reserve(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			ReserveRequest request = (ReserveRequest) invocation.getArguments()[0];
			final PaymentEvent reservationEvent = createReservationEvent(request.getSelectedOrderPaymentInstruments().get(0), request.getAmount());
			return new PaymentAPIResponse(Collections.singletonList(reservationEvent), true);
		});
	}

	private void mockReserveToSimulateModifySuccess() {
		when(reservationProcessor.reserveToSimulateModify(any(), any(), any(), any(), anyInt()))
				.thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
					final MoneyDTO amount = (MoneyDTO) invocation.getArguments()[0];
					final OrderPaymentInstrumentDTO instrument = (OrderPaymentInstrumentDTO) invocation.getArguments()[1];
					final PaymentEvent reservationEvent = createReservationEvent(instrument, amount);
					return new PaymentAPIResponse(Collections.singletonList(reservationEvent), true);
				});
	}

	private void mockReservationCapabilityFailure() {
		when(paymentAPIWorkflow.reserve(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			ReserveRequest request = (ReserveRequest) invocation.getArguments()[0];
			final PaymentEvent reservationEvent = createReservationEvent(request.getSelectedOrderPaymentInstruments().get(0), request.getAmount());
			reservationEvent.setPaymentStatus(FAILED);
			return new PaymentAPIResponse(Collections.singletonList(reservationEvent), false);
		});
	}

	private void mockReserveToSimulateModifyFailure() {
		when(reservationProcessor.reserveToSimulateModify(any(), any(), any(), any(), anyInt()))
				.thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
					final MoneyDTO amount = (MoneyDTO) invocation.getArguments()[0];
					final OrderPaymentInstrumentDTO instrument = (OrderPaymentInstrumentDTO) invocation.getArguments()[1];
					final PaymentEvent reservationEvent = createReservationEvent(instrument, amount);
					reservationEvent.setPaymentStatus(FAILED);
					return new PaymentAPIResponse(Collections.singletonList(reservationEvent), false);
				});
	}

	private void mockCancelCapabilitySuccess() {
		when(paymentAPIWorkflow.cancelReservation(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			CancelReservationRequest request = (CancelReservationRequest) invocation.getArguments()[0];
			return simulateCancel(request.getSelectedPaymentEventsToCancel());
		});
	}

	private void mockCancelCapabilityFailure() {
		when(paymentAPIWorkflow.cancelReservation(any())).thenAnswer((Answer<PaymentAPIResponse>) invocation -> {
			CancelReservationRequest request = (CancelReservationRequest) invocation.getArguments()[0];
			return simulateCancelFailure(request.getSelectedPaymentEventsToCancel());
		});
	}

	private ReserveRequest getCapturedReserveToSimulateModifyArguments() {
		final ArgumentCaptor<MoneyDTO> amountCaptor = ArgumentCaptor.forClass(MoneyDTO.class);
		final ArgumentCaptor<OrderPaymentInstrumentDTO> instrumentCaptor = ArgumentCaptor.forClass(OrderPaymentInstrumentDTO.class);
		@SuppressWarnings("unchecked") final ArgumentCaptor<Map<String, String>> customRequestDataCaptor = ArgumentCaptor.forClass(Map.class);
		final ArgumentCaptor<OrderContext> orderContextCaptor = ArgumentCaptor.forClass(OrderContext.class);
		verify(reservationProcessor).reserveToSimulateModify(amountCaptor.capture(), instrumentCaptor.capture(), customRequestDataCaptor.capture(),
				orderContextCaptor.capture(), anyInt());
		return ReserveRequestBuilder.builder()
				.withAmount(amountCaptor.getValue())
				.withSelectedOrderPaymentInstruments(Collections.singletonList(instrumentCaptor.getValue()))
				.withOrderContext(orderContextCaptor.getValue())
				.withCustomRequestData(customRequestDataCaptor.getValue())
				.build(getBeanFactory());
	}

	@Test
	public void decreaseWithSupportedModifyCapability() throws PaymentCapabilityRequestFailedException {
		final BigDecimal newAmount = THREE;

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		verify(modifyCapability).modify(modifyCapabilityRequestArgumentCaptor.capture());
		checkCapabilityRequest(modifyCapabilityRequestArgumentCaptor.getValue(), newAmount);
		checkModifyResponse(response, newAmount);
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void decreaseWithFailingModifyCapability() throws PaymentCapabilityRequestFailedException {
		final BigDecimal newAmount = THREE;
		when(modifyCapability.modify(any())).thenThrow(CAPABILITY_EXCEPTION);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		assertThat(response.isSuccess()).isEqualTo(true);
		checkModifyResponse(response, newAmount);
		checkSinglePaymentEventSkipped(response);
		assertThat(response.getEvents()).extracting(PaymentEvent::getInternalMessage).containsOnly(CAPABILITY_EXCEPTION.getInternalMessage());
		assertThat(response.getEvents()).extracting(PaymentEvent::getExternalMessage).containsOnly(CAPABILITY_EXCEPTION.getExternalMessage());
		assertThat(response.getEvents()).extracting(PaymentEvent::isTemporaryFailure).containsOnly(CAPABILITY_EXCEPTION.isTemporaryFailure());
	}

	@Test
	public void increaseWithSupportedModifyCapability() throws PaymentCapabilityRequestFailedException {
		final BigDecimal newAmount = FIFTEEN;

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		verify(modifyCapability).modify(modifyCapabilityRequestArgumentCaptor.capture());
		checkCapabilityRequest(modifyCapabilityRequestArgumentCaptor.getValue(), newAmount);
		checkModifyResponse(response, newAmount);
		checkSinglePaymentEventApproved(response);
	}

	@Test
	public void increaseWithFailingModifyCapability() throws PaymentCapabilityRequestFailedException {
		when(modifyCapability.modify(any())).thenThrow(CAPABILITY_EXCEPTION);

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(FIFTEEN));

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void decreaseToZeroWithoutModifyCapability() {
		lenient().when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockCancelCapabilitySuccess();
		final BigDecimal newAmount = BigDecimal.ZERO;

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		verify(paymentAPIWorkflow).cancelReservation(cancelReservationRequestArgumentCaptor.capture());
		checkRequest(cancelReservationRequestArgumentCaptor.getValue(), RESERVED_AMOUNT.getAmount());
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(RESERVED_AMOUNT.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void decreaseWithoutModifyCapability() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockCancelCapabilitySuccess();
		mockReserveToSimulateModifySuccess();
		final BigDecimal newAmount = THREE;

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		verify(paymentAPIWorkflow).cancelReservation(cancelReservationRequestArgumentCaptor.capture());
		checkRequest(cancelReservationRequestArgumentCaptor.getValue(), RESERVED_AMOUNT.getAmount());
		final ReserveRequest capturedRequest = getCapturedReserveToSimulateModifyArguments();
		checkReservationRequest(capturedRequest, newAmount);
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(newAmount, RESERVED_AMOUNT.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void decreaseWithoutModifyCapabilityUsingMultipleInstruments() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockCancelCapabilitySuccess();
		mockReserveToSimulateModifySuccess();
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(THIRTEEN);
		final List<PaymentEvent> newLedger = new ArrayList<>(modifyReservationRequest.getLedger());
		newLedger.add(createReservationEvent(createLimitedOrderPaymentInstrument(createMoney(FIVE)), createMoney(FIVE)));
		modifyReservationRequest.setLedger(newLedger);
		mockPaymentHistoryChargeablePaymentEvents(newLedger);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		verify(paymentAPIWorkflow).cancelReservation(cancelReservationRequestArgumentCaptor.capture());
		checkRequest(cancelReservationRequestArgumentCaptor.getValue(), RESERVED_AMOUNT.getAmount(), FIVE); // we are decreasing either 5 or 10
		final ReserveRequest capturedRequest = getCapturedReserveToSimulateModifyArguments();
		checkReservationRequest(capturedRequest, EIGHT, THREE);
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.filteredOn(paymentEvent -> paymentEvent.getPaymentType() == CANCEL_RESERVE)
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsAnyOf(RESERVED_AMOUNT.getAmount(), FIVE); // we are cancelling either 5 or 10 first
		assertThat(response.getEvents())
				.filteredOn(paymentEvent -> paymentEvent.getPaymentType() == RESERVE)
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsAnyOf(EIGHT, THREE); // then we are reserving either 8 or 3 depending on what we cancelled
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE, CANCEL_RESERVE); // anyways single reserve should happen first, followed by the single cancel
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED); // and both events should be approved
	}

	@Test
	public void decreaseWithoutModifyAndFailingCancelCapabilities() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockCancelCapabilityFailure();
		mockReserveToSimulateModifySuccess();
		final BigDecimal newAmount = THREE;

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(newAmount));

		verify(paymentAPIWorkflow).cancelReservation(cancelReservationRequestArgumentCaptor.capture());
		checkRequest(cancelReservationRequestArgumentCaptor.getValue(), RESERVED_AMOUNT.getAmount());
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(newAmount, RESERVED_AMOUNT.getAmount());
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE, CANCEL_RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, FAILED);
	}

	@Test
	public void decreaseWithoutModifyAndCancelCapabilities() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		when(capablePaymentProvider.getCapability(CancelCapability.class)).thenReturn(Optional.empty());
		mockReserveToSimulateModifySuccess();

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(THREE));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void increaseWithoutModifyCapability() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockReservationCapabilitySuccess();

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(FIFTEEN));

		verify(paymentAPIWorkflow).reserve(reserveRequestArgumentCaptor.capture());
		checkReservationRequest(reserveRequestArgumentCaptor.getValue(), FIVE);
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(FIVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void checkRereservationCount() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockCancelCapabilitySuccess();
		mockReserveToSimulateModifySuccess();

		testee.modifyReservation(createModifyReservationRequest(THREE));

		verify(reservationProcessor).reserveToSimulateModify(any(), any(), any(), any(), eq(1));
	}

	@Test
	public void increaseWithoutModifyCapabilityUsingMultipleInstruments() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockReservationCapabilitySuccess();
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(EIGHTEEN);
		final List<PaymentEvent> newLedger = new ArrayList<>(modifyReservationRequest.getLedger());
		newLedger.add(createReservationEvent(createLimitedOrderPaymentInstrument(createMoney(FIVE)), createMoney(FIVE)));
		modifyReservationRequest.setLedger(newLedger);
		mockPaymentHistoryChargeablePaymentEvents(newLedger);

		final PaymentAPIResponse response = testee.modifyReservation(modifyReservationRequest);

		verify(paymentAPIWorkflow).reserve(reserveRequestArgumentCaptor.capture());
		checkReservationRequest(reserveRequestArgumentCaptor.getValue(), THREE);
		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void increaseWithoutModifyAndFailingReserveCapability() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockReservationCapabilityFailure();

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(FIFTEEN));

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(FIVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED);
	}

	@Test
	public void decreaseWithoutModifyAndFailingReserveCapability() {
		when(capablePaymentProvider.getCapability(ModifyCapability.class)).thenReturn(Optional.empty());
		mockReserveToSimulateModifyFailure();

		final PaymentAPIResponse response = testee.modifyReservation(createModifyReservationRequest(THREE));

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(THREE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(RESERVE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED);
	}

	@Test
	public void modifyReservationShouldCallReserveWithAmountThreeWhenModifyReservationRequestWithThreeAndChargeablePaymentEventsIsEmpty() {
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(THREE);
		final Multimap<PaymentEvent, MoneyDTO> emptyChargeablePaymentEvents = HashMultimap.create();

		when(paymentHistory.getChargeablePaymentEvents(modifyReservationRequest.getLedger())).thenReturn(emptyChargeablePaymentEvents);
		when(paymentHistory.getAvailableReservedAmount(anyList())).thenReturn(createMoney(BigDecimal.ZERO));
		when(paymentHistory.getChargedAmount(anyList())).thenReturn(createMoney(BigDecimal.ZERO));

		testee.modifyReservation(modifyReservationRequest);

		verify(paymentAPIWorkflow).reserve(argThat(reserveRequest -> reserveRequest.getAmount().getAmount().equals(THREE)));
	}

	@Test
	public void paymentAPIResponseShouldBeEqualReservePaymentAPIResponseWhenCallModifyReservationAndChargeablePaymentEventsIsEmpty() {
		final ModifyReservationRequest modifyReservationRequest = createModifyReservationRequest(THREE);
		final Multimap<PaymentEvent, MoneyDTO> emptyChargeablePaymentEvents = HashMultimap.create();
		when(paymentHistory.getChargeablePaymentEvents(modifyReservationRequest.getLedger())).thenReturn(emptyChargeablePaymentEvents);

		final PaymentAPIResponse reservePaymentAPIResponse = new PaymentAPIResponse(Collections.emptyList(), true);
		when(paymentAPIWorkflow.reserve(any())).thenReturn(reservePaymentAPIResponse);

		final PaymentAPIResponse paymentAPIResponse = testee.modifyReservation(modifyReservationRequest);

		assertThat(paymentAPIResponse).isEqualTo(reservePaymentAPIResponse);
	}

}