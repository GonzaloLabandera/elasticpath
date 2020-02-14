/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequestBuilder;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.PaymentsExceptionMessageId;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.processor.CreditProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
@RunWith(MockitoJUnitRunner.class)
public class CreditProcessorImplTest extends AbstractProcessorImplTestBase {

	@Mock
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Mock
	private PaymentProviderService paymentProviderService;

	@Mock
	private PaymentHistory paymentHistory;

	@Mock
	private PaymentAPIWorkflow paymentAPIWorkflow;

	@Captor
	private ArgumentCaptor<CreditCapabilityRequest> creditArgumentCaptor;

	@Captor
	private ArgumentCaptor<ReverseChargeCapabilityRequest> reverseChargeArgumentCaptor;

	private CreditProcessor testee;

	private static final MoneyDTO ONE = createMoney(BigDecimal.ONE);
	private static final MoneyDTO TEN = createMoney(BigDecimal.TEN);
	private static final MoneyDTO ELEVEN = createMoney(BigDecimal.valueOf(11));
	private static final MoneyDTO TWENTY = createMoney(BigDecimal.valueOf(20));
	private static final MoneyDTO THIRTY = createMoney(BigDecimal.valueOf(30));
	private static final MoneyDTO ZERO = createMoney(BigDecimal.ZERO);

	private static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "PAYMENT_PROVIDER_CONFIGURATION_GUID";
	private static final Map<String, String> CUSTOM_REQUEST_DATA = ImmutableMap.of("custom-request-data-key", "custom-request-data-value");

	private final PaymentProvider capablePaymentProvider = mock(PaymentProvider.class);
	private final CreditCapability creditCapability = mock(CreditCapability.class);
	private final ReverseChargeCapability reverseChargeCapability = mock(ReverseChargeCapability.class);
	private final PaymentProviderConfiguration paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
	private final PaymentCapabilityResponse paymentCapabilityResponse = mockPaymentCapabilityResponse(CHARGE_EVENT_DATA);
	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Before
	public void setUp() throws PaymentCapabilityRequestFailedException {
		super.setUp();

		when(paymentProviderConfigurationService.findByGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID)).thenReturn(paymentProviderConfiguration);
		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(capablePaymentProvider);
		when(creditCapability.credit(any())).thenReturn(paymentCapabilityResponse);
		when(reverseChargeCapability.reverseCharge(any())).thenReturn(paymentCapabilityResponse);
		lenient().when(capablePaymentProvider.getCapability(CreditCapability.class)).thenReturn(Optional.of(creditCapability));
		lenient().when(capablePaymentProvider.getCapability(ReverseChargeCapability.class)).thenReturn(Optional.of(reverseChargeCapability));
		testee = new CreditProcessorImpl(paymentProviderConfigurationService,
				paymentProviderService, paymentHistory, moneyDtoCalculator, paymentAPIWorkflow, getBeanFactory());
		mockPaymentHistoryRefundablePaymentEvents(getPaymentEvents());
	}

	@Test
	public void creditGeneratesApprovedEventWhenAmountToRefundLowerThanOrderTotalAndCapabilityIsSupported()
			throws PaymentCapabilityRequestFailedException {
		final CreditRequest creditRequest = createCreditRequest(getPaymentEvents(), TEN);
		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto()).thenReturn(TEN);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(CREDIT);
		verify(creditCapability).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getValue(), TEN.getAmount());
	}

	@Test
	public void creditGeneratesTwoApprovedEventWhenAmountToRefundLowerThanOrderTotalAndCapabilityIsSupported()
			throws PaymentCapabilityRequestFailedException {
		final CreditRequest creditRequest = createCreditRequest(getPaymentEvents(), ELEVEN);
		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto()).thenReturn(ELEVEN);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsOnly(CREDIT, CREDIT);
		verify(creditCapability, times(2)).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(0), TEN.getAmount());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(1), ONE.getAmount());
	}

	@Test
	public void creditGeneratesTwoApprovedEventWhenAmountToRefundEqualThanOrderTotalAndCapabilityIsSupported()
			throws PaymentCapabilityRequestFailedException {
		final CreditRequest creditRequest = createCreditRequest(getPaymentEvents(), TWENTY);
		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto()).thenReturn(TWENTY);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsOnly(CREDIT, CREDIT);
		verify(creditCapability, times(2)).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(0), TEN.getAmount());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(1), TEN.getAmount());
	}

	@Test
	public void creditThrownExceptionWhenAmountToRefundBiggerThanOrderTotalAndCapabilityIsSupported() {
		final CreditRequest creditRequest = createCreditRequest(getPaymentEvents(), THIRTY);
		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());

		assertThatThrownBy(() -> testee.credit(creditRequest))
				.isInstanceOf(PaymentsException.class)
				.extracting(exception -> ((PaymentsException) exception).getMessageId())
				.isEqualTo(PaymentsExceptionMessageId.PAYMENT_INSUFFICIENT_FUNDS);
	}

	@Test
	public void creditThrowsExceptionWhenTryingToRefundFreeOrder() {
		when(paymentHistory.getChargedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());
		final CreditRequest creditRequest = createCreditRequest(ImmutableList.of(), TEN);

		assertThatThrownBy(() -> testee.credit(creditRequest)).isInstanceOf(PaymentsException.class);
	}

	@Test
	public void creditApprovedWhenAmountToRefundEqualThanOrderTotalMinusExistingCredit() throws PaymentCapabilityRequestFailedException {
		final PaymentEvent firstReserveEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), ELEVEN);
		final PaymentEvent firstChargeEvent = createChargeEvent(firstReserveEvent, ELEVEN);
		final PaymentEvent firstCreditEvent = createCreditEvent(firstReserveEvent, ONE);
		when(paymentHistory.getChargedAmount(any())).thenReturn(ELEVEN);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto()).thenReturn(ONE);
		final CreditRequest creditRequest = createCreditRequest(ImmutableList.of(firstReserveEvent, firstChargeEvent, firstCreditEvent), TEN);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(CREDIT);
		verify(creditCapability).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getValue(), TEN.getAmount());
	}

	@Test
	public void creditThrowsExceptionWhenAmountToRefundBiggerThanOrderTotalMinusExistingCredit() {
		final PaymentEvent firstReserveEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), ELEVEN);
		final PaymentEvent firstChargeEvent = createChargeEvent(firstReserveEvent, ELEVEN);
		final PaymentEvent firstCreditEvent = createCreditEvent(firstReserveEvent, ONE);
		final CreditRequest creditRequest = createCreditRequest(ImmutableList.of(firstReserveEvent, firstChargeEvent, firstCreditEvent), ELEVEN);
		when(paymentHistory.getChargedAmount(any())).thenReturn(TEN);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());

		assertThatThrownBy(() -> testee.credit(creditRequest)).isInstanceOf(PaymentsException.class);
	}

	@Test
	public void creditGeneratesFailedEventWhenCapabilityThrowsException() throws PaymentCapabilityRequestFailedException {
		final CreditRequest creditRequest = createCreditRequest(getPaymentEvents(), ONE);
		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());
		when(creditCapability.credit(any())).thenThrow(CAPABILITY_EXCEPTION);

		final PaymentAPIResponse response = testee.credit(creditRequest);

		checkSinglePaymentEventFailed(response);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(CREDIT);
		verify(creditCapability).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getValue(), ONE.getAmount());
	}

	@Test
	public void creditThrowsExceptionWhenCapabilityIsUnsupported() {
		final CreditRequest creditRequest = createCreditRequest(getPaymentEvents(), ONE);
		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());
		when(capablePaymentProvider.getCapability(CreditCapability.class)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> testee.credit(creditRequest)).isInstanceOf(PaymentsException.class);
	}

	@Test
	public void reverseChargeGeneratesApprovedWhenReverseChargeThrowsExceptionAndCreditIsSupported()
			throws PaymentCapabilityRequestFailedException {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		List<PaymentEvent> selectedPaymentEvents = filterApprovedChargeEvents(paymentEventList);
		final ReverseChargeRequest reverseChargeRequest = createReverseCharge(paymentEventList, selectedPaymentEvents);

		when(paymentHistory.getChargedAmount(any())).thenReturn(ZERO);
		when(reverseChargeCapability.reverseCharge(any())).thenThrow(CAPABILITY_EXCEPTION);

		final PaymentAPIResponse response = testee.reverseCharge(reverseChargeRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsExactly(APPROVED, APPROVED);
		assertThat(response.isSuccess()).isTrue();
		verify(reverseChargeCapability, times(2)).reverseCharge(reverseChargeArgumentCaptor.capture());
		verify(creditCapability, times(2)).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(0), TEN.getAmount());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(1), TEN.getAmount());
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(0));
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(1));
	}

	@Test
	public void reverseChargeGeneratesApprovedWhenReverseChargeCapabilityIsSupportedAndCreditCapabilityIsSupported()
			throws PaymentCapabilityRequestFailedException {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		List<PaymentEvent> selectedPaymentEvents = filterApprovedChargeEvents(paymentEventList);
		final ReverseChargeRequest reverseChargeRequest = createReverseCharge(paymentEventList, selectedPaymentEvents);

		when(paymentHistory.getChargedAmount(any())).thenReturn(ZERO);

		final PaymentAPIResponse response = testee.reverseCharge(reverseChargeRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsExactly(APPROVED, APPROVED);
		assertThat(response.isSuccess()).isTrue();
		verify(reverseChargeCapability, times(2)).reverseCharge(reverseChargeArgumentCaptor.capture());
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(0));
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(1));
	}

	@Test
	public void reverseChargeGeneratesApprovedWhenReverseChargeCapabilityIsUnsupportedAndCreditCapabilityIsSupported()
			throws PaymentCapabilityRequestFailedException {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		List<PaymentEvent> selectedPaymentEvents = filterApprovedChargeEvents(paymentEventList);
		final ReverseChargeRequest reverseChargeRequest = createReverseCharge(paymentEventList, selectedPaymentEvents);

		when(paymentHistory.getChargedAmount(any())).thenReturn(ZERO);
		when(capablePaymentProvider.getCapability(ReverseChargeCapability.class)).thenReturn(Optional.empty());

		final PaymentAPIResponse response = testee.reverseCharge(reverseChargeRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsExactly(APPROVED, APPROVED);
		assertThat(response.isSuccess()).isTrue();
		verify(creditCapability, times(2)).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(0), TEN.getAmount());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(1), TEN.getAmount());
	}

	@Test
	public void reverseChargeGeneratesFailedWhenReverseChargeThrowsExceptionAndCreditIsUnsupported()
			throws PaymentCapabilityRequestFailedException {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		List<PaymentEvent> selectedPaymentEvents = filterApprovedChargeEvents(paymentEventList);
		final ReverseChargeRequest reverseChargeRequest = createReverseCharge(paymentEventList, selectedPaymentEvents);

		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(reverseChargeCapability.reverseCharge(any())).thenThrow(CAPABILITY_EXCEPTION);
		when(capablePaymentProvider.getCapability(CreditCapability.class)).thenReturn(Optional.empty());

		final PaymentAPIResponse response = testee.reverseCharge(reverseChargeRequest);

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getEvents()).isEmpty();
		verify(reverseChargeCapability, times(2)).reverseCharge(reverseChargeArgumentCaptor.capture());
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(0));
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(1));
	}

	@Test
	public void reverseChargeGeneratesFailedWhenReverseChargeThrowsExceptionAndCreditThrowsException()
			throws PaymentCapabilityRequestFailedException {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		List<PaymentEvent> selectedPaymentEvents = filterApprovedChargeEvents(paymentEventList);
		final ReverseChargeRequest reverseChargeRequest = createReverseCharge(paymentEventList, selectedPaymentEvents);

		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(reverseChargeCapability.reverseCharge(any())).thenThrow(CAPABILITY_EXCEPTION);
		when(creditCapability.credit(any())).thenThrow(CAPABILITY_EXCEPTION);

		final PaymentAPIResponse response = testee.reverseCharge(reverseChargeRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsExactly(FAILED, FAILED);
		assertThat(response.isSuccess()).isFalse();
		verify(reverseChargeCapability, times(2)).reverseCharge(reverseChargeArgumentCaptor.capture());
		verify(creditCapability, times(2)).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(0), TEN.getAmount());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(1), TEN.getAmount());
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(0));
		checkCapabilityRequest(reverseChargeArgumentCaptor.getAllValues().get(1));
	}

	@Test
	public void reverseChargeGeneratesFailedWhenReverseChargeIsUnsupportedAndCreditIsUnsupported() {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		List<PaymentEvent> selectedPaymentEvents = filterApprovedChargeEvents(paymentEventList);
		final ReverseChargeRequest reverseChargeRequest = createReverseCharge(paymentEventList, selectedPaymentEvents);

		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(capablePaymentProvider.getCapability(CreditCapability.class)).thenReturn(Optional.empty());
		when(capablePaymentProvider.getCapability(ReverseChargeCapability.class)).thenReturn(Optional.empty());

		final PaymentAPIResponse response = testee.reverseCharge(reverseChargeRequest);

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void reverseChargeGeneratesFailedWhenReverseChargeIsUnsupportedCreditThrowsException()
			throws PaymentCapabilityRequestFailedException {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		List<PaymentEvent> selectedPaymentEvents = filterApprovedChargeEvents(paymentEventList);
		final ReverseChargeRequest reverseChargeRequest = createReverseCharge(paymentEventList, selectedPaymentEvents);

		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(capablePaymentProvider.getCapability(ReverseChargeCapability.class)).thenReturn(Optional.empty());
		when(creditCapability.credit(any())).thenThrow(CAPABILITY_EXCEPTION);

		final PaymentAPIResponse response = testee.reverseCharge(reverseChargeRequest);

		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentType).containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents()).extracting(PaymentEvent::getPaymentStatus).containsExactly(FAILED, FAILED);
		assertThat(response.isSuccess()).isFalse();
		verify(creditCapability, times(2)).credit(creditArgumentCaptor.capture());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(0), TEN.getAmount());
		checkCapabilityRequest(creditArgumentCaptor.getAllValues().get(1), TEN.getAmount());
	}

	@Test
	public void reverseChargeThrownExceptionWhenAmountToRefundBiggerThanOrderTotalAndCapabilityIsSupported() {
		List<PaymentEvent> paymentEventList = getPaymentEvents();
		final CreditRequest creditRequest = createCreditRequest(paymentEventList, THIRTY);

		when(paymentHistory.getChargedAmount(any())).thenReturn(TWENTY);
		when(paymentHistory.getRefundedAmount(any())).thenReturn(moneyDtoCalculator.zeroMoneyDto());

		assertThatThrownBy(() -> testee.credit(creditRequest))
				.isInstanceOf(PaymentsException.class)
				.extracting(exception -> ((PaymentsException) exception).getMessageId())
				.isEqualTo(PaymentsExceptionMessageId.PAYMENT_INSUFFICIENT_FUNDS);
	}

	private List<PaymentEvent> getPaymentEvents() {
		final PaymentEvent firstReserveEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), TEN);
		final PaymentEvent firstChargeEvent = createChargeEvent(firstReserveEvent, TEN);
		final PaymentEvent secondReserveEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), TEN);
		final PaymentEvent secondChargeEvent = createChargeEvent(secondReserveEvent, TEN);
		return Arrays.asList(firstReserveEvent, firstChargeEvent, secondReserveEvent, secondChargeEvent);
	}

	private List<PaymentEvent> filterApprovedChargeEvents(final List<PaymentEvent> paymentEventList) {
		return paymentEventList.stream()
				.filter(paymentEvent -> paymentEvent.getPaymentStatus() == APPROVED && paymentEvent.getPaymentType() == CHARGE)
				.collect(Collectors.toList());
	}

	private void mockPaymentHistoryRefundablePaymentEvents(final List<PaymentEvent> newLedger) {
		final Multimap<PaymentEvent, MoneyDTO> paymentEvents = ArrayListMultimap.create();
		for (PaymentEvent paymentEvent : newLedger) {
			if (paymentEvent.getPaymentType().equals(CHARGE)) {
				paymentEvents.put(paymentEvent, paymentEvent.getAmount());
			}
		}
		when(paymentHistory.getRefundablePaymentEvents(any())).thenReturn(paymentEvents);
	}

	private ReverseChargeRequest createReverseCharge(final List<PaymentEvent> paymentEvents,
													 final List<PaymentEvent> selectedPaymentEvents) {
		return ReverseChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(Collections.singletonList(paymentEvents.get(0).getOrderPaymentInstrumentDTO()))
				.withLedger(paymentEvents)
				.withCustomRequestData(CUSTOM_REQUEST_DATA)
				.withSelectedPaymentEvents(selectedPaymentEvents)
				.withOrderContext(createOrderContext())
				.build(getBeanFactory());
	}
}

