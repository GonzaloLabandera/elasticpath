/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_CHAIN;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_RELATIONSHIP_REGISTRY;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang3.tuple.Pair;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.handler.PaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.PaymentGroupState;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.history.validator.PaymentEventValidator;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Represents an implementation of {@link PaymentHistory}.
 */
public class PaymentHistoryImpl implements PaymentHistory {

	private final Map<TransactionType, PaymentEventHandler> paymentEventHandlers;
	private final MoneyDtoCalculator moneyDtoCalculator;
	private final List<PaymentEventValidator> paymentEventValidators;
	private final BeanFactory beanFactory;

	/**
	 * Constructor.
	 *
	 * @param paymentEventHandlers   payment events handlers.
	 * @param moneyDtoCalculator     calculator for mathematical operations with money dto.
	 * @param paymentEventValidators validators for payment events.
	 * @param beanFactory            EP bean factory
	 */
	public PaymentHistoryImpl(final Map<TransactionType, PaymentEventHandler> paymentEventHandlers, final MoneyDtoCalculator moneyDtoCalculator,
							  final List<PaymentEventValidator> paymentEventValidators, final BeanFactory beanFactory) {
		this.paymentEventHandlers = paymentEventHandlers;
		this.moneyDtoCalculator = moneyDtoCalculator;
		this.paymentEventValidators = paymentEventValidators;
		this.beanFactory = beanFactory;
	}

	@Override
	public MoneyDTO getAvailableReservedAmount(final List<PaymentEvent> paymentEvents) {
		final List<PaymentEventChain> paymentEventsChains = buildPaymentEventChains(paymentEvents);
		final List<PaymentGroupState> paymentGroupsState = paymentEventsChains.stream()
				.map(PaymentEventChain::getPaymentEvents)
				.peek(this::validatePaymentEvents)
				.map(this::calculatePaymentGroupsState)
				.collect(Collectors.collectingAndThen(Collectors.toList(), this::combinePaymentGroupStates));

		return getAvailableReservedAmountFromPaymentGroupStates(paymentGroupsState);
	}

	@Override
	public MoneyDTO getChargedAmount(final List<PaymentEvent> paymentEvents) {
		final List<PaymentEventChain> paymentEventsChains = buildPaymentEventChains(paymentEvents);
		final List<PaymentGroupState> paymentGroupsState = paymentEventsChains.stream()
				.map(PaymentEventChain::getPaymentEvents)
				.peek(this::validatePaymentEvents)
				.map(this::calculatePaymentGroupsState)
				.collect(Collectors.collectingAndThen(Collectors.toList(), this::combinePaymentGroupStates));

		return getMoneyDtoCalculator().minus(getChargedAmountFromPaymentGroupStates(paymentGroupsState),
				getReverseChargedAmountFromPaymentGroupStates(paymentGroupsState));
	}

	@Override
	public MoneyDTO getRefundedAmount(final List<PaymentEvent> paymentEvents) {
		final List<PaymentEventChain> paymentEventsChains = buildPaymentEventChains(paymentEvents);
		final List<PaymentGroupState> paymentGroupsState = paymentEventsChains.stream()
				.map(PaymentEventChain::getPaymentEvents)
				.peek(this::validatePaymentEvents)
				.map(this::calculatePaymentGroupsState)
				.collect(Collectors.collectingAndThen(Collectors.toList(), this::combinePaymentGroupStates));

		return getRefundedAmountFromPaymentGroupStates(paymentGroupsState);
	}

	@Override
	public Multimap<PaymentEvent, MoneyDTO> getChargeablePaymentEvents(final List<PaymentEvent> paymentEvents) {
		final List<PaymentEventChain> paymentEventsChains = buildPaymentEventChains(paymentEvents);
		final List<PaymentGroupState> paymentGroupsState = paymentEventsChains.stream()
				.map(PaymentEventChain::getPaymentEvents)
				.peek(this::validatePaymentEvents)
				.map(this::calculatePaymentGroupsState)
				.collect(Collectors.collectingAndThen(Collectors.toList(), this::combinePaymentGroupStates));

		final Map<PaymentEvent, MoneyDTO> chargeablePaymentEvents = getChargeablePaymentEventsFromPaymentGroupStates(paymentGroupsState);
		return Multimaps.forMap(chargeablePaymentEvents);
	}

	@Override
	public Multimap<PaymentEvent, MoneyDTO> getRefundablePaymentEvents(final List<PaymentEvent> paymentEvents) {
		final List<PaymentEventChain> paymentEventsChains = buildPaymentEventChains(paymentEvents);
		final List<PaymentGroupState> paymentGroupsState = paymentEventsChains.stream()
				.map(PaymentEventChain::getPaymentEvents)
				.peek(this::validatePaymentEvents)
				.map(this::calculatePaymentGroupsState)
				.collect(Collectors.collectingAndThen(Collectors.toList(), this::combinePaymentGroupStates));

		final Map<PaymentEvent, MoneyDTO> refundablePaymentEvents = getRefundablePaymentEventsFromPaymentGroupStates(paymentGroupsState);
		return Multimaps.forMap(refundablePaymentEvents);
	}

	@Override
	public Map<OrderPaymentInstrumentDTO, MoneyDTO> getReservableOrderPaymentInstruments(
			final List<PaymentEvent> ledger,
			final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {

		final Map<OrderPaymentInstrumentDTO, MoneyDTO> reservableOrderPaymentInstruments = new HashMap<>();
		for (final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO : orderPaymentInstruments) {
			if (orderPaymentInstrumentDTO.getLimit().getAmount().compareTo(BigDecimal.ZERO) == 0) {
				reservableOrderPaymentInstruments.put(orderPaymentInstrumentDTO, moneyDtoCalculator.zeroMoneyDto());
			} else {
				final List<PaymentEvent> paymentEvents = filterLedgerByOrderPaymentInstrumentGuid(ledger, orderPaymentInstrumentDTO.getGUID());
				final MoneyDTO reservableAmount = calculateReservableAmountForSingleInstrument(paymentEvents, orderPaymentInstrumentDTO);
				if (moneyDtoCalculator.isPositive(reservableAmount)) {
					reservableOrderPaymentInstruments.put(orderPaymentInstrumentDTO, reservableAmount);
				}
			}
		}

		return reservableOrderPaymentInstruments;
	}

	/**
	 * Calculates amount available to reserve for given order payment instrument.
	 *
	 * @param paymentEvents             ledger sequence.
	 * @param orderPaymentInstrumentDTO order payment instrument DTO.
	 * @return amount available to reserve for given order payment instrument.
	 */
	protected MoneyDTO calculateReservableAmountForSingleInstrument(final List<PaymentEvent> paymentEvents,
																	final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO) {
		if (orderPaymentInstrumentDTO.getLimit().getAmount().compareTo(BigDecimal.ZERO) == 0) {
			return moneyDtoCalculator.zeroMoneyDto();
		}

		final MoneyDTO limitAmount = orderPaymentInstrumentDTO.getLimit();
		if (paymentEvents.isEmpty()) {
			return limitAmount;
		}

		final MoneyDTO availableReservedAmount = getAvailableReservedAmount(paymentEvents);
		final MoneyDTO chargedAmount = getChargedAmount(paymentEvents);
		final MoneyDTO refundedAmount = getRefundedAmount(paymentEvents);
		final MoneyDTO reservedAndChargedAmount = moneyDtoCalculator.plus(availableReservedAmount, chargedAmount);
		final MoneyDTO usedAmount = moneyDtoCalculator.minus(reservedAndChargedAmount, refundedAmount);

		return moneyDtoCalculator.minus(limitAmount, usedAmount);
	}

	/**
	 * Validates ledger sequence for correctness.
	 *
	 * @param paymentEvents ledger sequence
	 */
	protected void validatePaymentEvents(final List<PaymentEvent> paymentEvents) {
		paymentEventValidators.forEach(paymentEventValidator -> paymentEventValidator.validate(paymentEvents));
	}

	/**
	 * Calculates payment groups state map from the ledger.
	 *
	 * @param paymentEvents the ledger
	 * @return map of payment states grouped by originating reservation
	 */
	protected PaymentGroupState calculatePaymentGroupsState(final List<PaymentEvent> paymentEvents) {
		return paymentEvents.stream().collect(
				Collector.of(PaymentGroupState::new, this::accumulatePaymentEventInPaymentGroupState, (state1, state2) -> {
					throw new IllegalStateException("Combiner operation is not supported, calculation must happen in single thread");
				}));
	}

	/**
	 * Combines payment group states based on originating event.
	 *
	 * @param paymentGroupStates payment group states
	 * @return potentially combined payment group states
	 */
	protected List<PaymentGroupState> combinePaymentGroupStates(final List<PaymentGroupState> paymentGroupStates) {
		return paymentGroupStates.stream()
				.collect(Collectors.collectingAndThen(
						Collectors.toMap(state -> state.getPaymentEvent().getGuid(), Function.identity(), this::combinePaymentGroupStates),
						map -> new ArrayList<>(map.values()))
				);
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private PaymentGroupState combinePaymentGroupStates(final PaymentGroupState state, final PaymentGroupState anotherState) {
		return paymentEventHandlers.get(state.getPaymentEvent().getPaymentType()).combinePaymentGroupStates(state, anotherState);
	}

	/**
	 * Accumulates another payment event in payment group state.
	 *
	 * @param paymentGroupState payment group state
	 * @param paymentEvent      payment event to accumulate
	 */
	protected void accumulatePaymentEventInPaymentGroupState(final PaymentGroupState paymentGroupState, final PaymentEvent paymentEvent) {
		final PaymentEventHandler paymentEventHandler = paymentEventHandlers.get(paymentEvent.getPaymentType());
		paymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentGroupState, paymentEvent);
	}

	/**
	 * Calculates the total available reservation amount across all payment states.
	 *
	 * @param paymentGroupStates map of payment states grouped by originating reservation
	 * @return total available reservation amount
	 */
	protected MoneyDTO getAvailableReservedAmountFromPaymentGroupStates(final List<PaymentGroupState> paymentGroupStates) {
		return paymentGroupStates.stream()
				.map(PaymentGroupState::getAvailable)
				.collect(Collector.of(moneyDtoCalculator::zeroMoneyDto, moneyDtoCalculator::increase, moneyDtoCalculator::plus));
	}

	/**
	 * Calculates the total charged amount across all payment states.
	 *
	 * @param paymentGroupStates map of payment states grouped by originating reservation
	 * @return total charged amount
	 */
	protected MoneyDTO getChargedAmountFromPaymentGroupStates(final List<PaymentGroupState> paymentGroupStates) {
		return paymentGroupStates.stream()
				.map(PaymentGroupState::getCharged)
				.collect(Collector.of(moneyDtoCalculator::zeroMoneyDto, moneyDtoCalculator::increase, moneyDtoCalculator::plus));
	}

	/**
	 * Calculates the total refunded/credited amount across all payment states.
	 *
	 * @param paymentGroupStates map of payment states grouped by originating reservation
	 * @return total refunded/credited amount
	 */
	protected MoneyDTO getRefundedAmountFromPaymentGroupStates(final List<PaymentGroupState> paymentGroupStates) {
		return paymentGroupStates.stream()
				.map(PaymentGroupState::getRefunded)
				.collect(Collector.of(moneyDtoCalculator::zeroMoneyDto, moneyDtoCalculator::increase, moneyDtoCalculator::plus));
	}

	/**
	 * Calculates the total reverse charged amount across all payment states.
	 *
	 * @param paymentGroupStates map of payment states grouped by originating reservation
	 * @return total reverse charged amount
	 */
	protected MoneyDTO getReverseChargedAmountFromPaymentGroupStates(final List<PaymentGroupState> paymentGroupStates) {
		return paymentGroupStates.stream()
				.map(PaymentGroupState::getReverseCharged)
				.collect(Collector.of(moneyDtoCalculator::zeroMoneyDto, moneyDtoCalculator::increase, moneyDtoCalculator::plus));
	}

	/**
	 * Filters out the map of chargeable payment events.
	 *
	 * @param paymentGroupStates map of payment states grouped by originating reservation
	 * @return reservation events mapped to reserved amounts
	 */
	protected Map<PaymentEvent, MoneyDTO> getChargeablePaymentEventsFromPaymentGroupStates(final List<PaymentGroupState> paymentGroupStates) {
		return paymentGroupStates.stream()
				.filter(paymentGroupState -> moneyDtoCalculator.hasBalance(paymentGroupState.getAvailable()))
				.collect(Collectors.toMap(PaymentGroupState::getPaymentEvent, PaymentGroupState::getAvailable));
	}

	/**
	 * Filters out the map of refundable payment events.
	 *
	 * @param paymentGroupStates map of payment states grouped by originating reservation
	 * @return charge events mapped to charged amounts
	 */
	protected Map<PaymentEvent, MoneyDTO> getRefundablePaymentEventsFromPaymentGroupStates(final List<PaymentGroupState> paymentGroupStates) {
		if (moneyDtoCalculator.hasBalance(getReverseChargedAmountFromPaymentGroupStates(paymentGroupStates))) {
			return Collections.emptyMap();
		}
		return paymentGroupStates.stream()
				.map(paymentGroupState -> Pair.of(paymentGroupState.getPaymentEvent(),
						moneyDtoCalculator.minus(paymentGroupState.getCharged(), paymentGroupState.getRefunded())))
				.filter(pair -> moneyDtoCalculator.hasBalance(pair.getValue()))
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}

	protected Map<TransactionType, PaymentEventHandler> getPaymentEventHandlers() {
		return paymentEventHandlers;
	}

	protected MoneyDtoCalculator getMoneyDtoCalculator() {
		return moneyDtoCalculator;
	}

	protected List<PaymentEventValidator> getPaymentEventValidators() {
		return paymentEventValidators;
	}

	/**
	 * Builds list of payment event chains.
	 *
	 * @param paymentEvents source list of payment events.
	 * @return list of payment event chains.
	 */
	protected List<PaymentEventChain> buildPaymentEventChains(final List<PaymentEvent> paymentEvents) {
		final PaymentEventRelationshipRegistry paymentEventRelationshipRegistry = beanFactory
				.getPrototypeBean(PAYMENT_EVENT_RELATIONSHIP_REGISTRY, PaymentEventRelationshipRegistry.class);
		paymentEventRelationshipRegistry.addPaymentEvents(paymentEvents);

		final List<PaymentEvent> rootEvents = paymentEvents.stream()
				.filter(paymentEvent -> Objects.isNull(paymentEvent.getParentGuid()))
				.collect(Collectors.toList());

		final List<PaymentEventChain> paymentEventChains = rootEvents.stream()
				.map(this::createNewPaymentEventChainWithRootPaymentEvent)
				.collect(Collectors.toList());

		while (paymentEventChains.stream().map(PaymentEventChain::getLast).anyMatch(paymentEventRelationshipRegistry::hasChild)) {
			final List<PaymentEventChain> upstreamPaymentEventChainsWithChildren = paymentEventChains.stream()
					.filter(paymentEventChain -> paymentEventRelationshipRegistry.hasChild(paymentEventChain.getLast()))
					.collect(Collectors.toList());

			final List<PaymentEventChain> newPaymentEventChains = upstreamPaymentEventChainsWithChildren.stream()
					.peek(paymentEventChains::remove)
					.flatMap(paymentEventChain -> paymentEventRelationshipRegistry.getChildren(paymentEventChain.getLast())
							.stream().map(paymentEventChain::createDownstreamPaymentEventChain))
					.collect(Collectors.toList());

			paymentEventChains.addAll(newPaymentEventChains);
		}

		return paymentEventChains;
	}

	/**
	 * Creates new payment event chain with given root payment event.
	 *
	 * @param paymentEvent root payment event.
	 * @return new payment event chain with given root payment event.
	 */
	protected PaymentEventChain createNewPaymentEventChainWithRootPaymentEvent(final PaymentEvent paymentEvent) {
		final PaymentEventChain paymentEventChain = beanFactory.getPrototypeBean(PAYMENT_EVENT_CHAIN, PaymentEventChain.class);
		paymentEventChain.addPaymentEvent(paymentEvent);

		return paymentEventChain;
	}

	/**
	 * Filters ledger by guid of order payment instrument.
	 *
	 * @param ledger list of payment events to filter.
	 * @param guid   guid of order payment instrument.
	 * @return list of payment events filtered by guid of order payment instrument.
	 */
	protected List<PaymentEvent> filterLedgerByOrderPaymentInstrumentGuid(final List<PaymentEvent> ledger, final String guid) {
		return ledger.stream()
				.filter(paymentEvent -> paymentEvent.getOrderPaymentInstrumentDTO().getGUID().equals(guid))
				.collect(Collectors.toList());
	}

}
