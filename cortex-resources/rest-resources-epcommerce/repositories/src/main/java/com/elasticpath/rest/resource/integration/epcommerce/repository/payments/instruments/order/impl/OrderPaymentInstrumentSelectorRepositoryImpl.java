/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_PAYMENT_INSTRUMENT;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * Order Payment Instrument selector.
 *
 * @param <SI> extends OrderPaymentInstrumentSelectorIdentifier
 * @param <CI> extends OrderPaymentInstrumentSelectorChoiceIdentifier
 */
@Component
public class OrderPaymentInstrumentSelectorRepositoryImpl
		<SI extends OrderPaymentInstrumentSelectorIdentifier, CI extends OrderPaymentInstrumentSelectorChoiceIdentifier>
		implements SelectorRepository<OrderPaymentInstrumentSelectorIdentifier, OrderPaymentInstrumentSelectorChoiceIdentifier> {

	/**
	 * GUID key for card order payment instrument in composite identifier map.
	 */
	static final String CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY = "cart-order-instrument";
	/**
	 * GUID key for customer payment instrument in composite identifier map.
	 */
	static final String CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY = "customer-instrument";
	/**
	 * Flag for default customer payment instrument in composite identifier map.
	 */
	static final String IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT = "is-default";
	/**
	 * GUID key of payment instrument in composite identifier map, used to find distinct instruments.
	 */
	static final String PAYMENT_INSTRUMENT_GUID_KEY = "instrument";

	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;
	private CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;
	private CartOrderRepository cartOrderRepository;
	private ReactiveAdapter reactiveAdapter;
	private ResourceOperationContext resourceOperationContext;
	private CustomerRepository customerRepository;
	private BeanFactory beanFactory;
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	/**
	 * Order payment instrument selector comparator for {@link SelectorChoice} comparing payment instrument GUIDs.
	 */
	private static class SelectorChoiceComparatorForPaymentInstrumentGuid implements Comparator<SelectorChoice>, Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public int compare(final SelectorChoice selectorChoice1, final SelectorChoice selectorChoice2) {
			final OrderPaymentInstrumentSelectorChoiceIdentifier choiceId1 =
					(OrderPaymentInstrumentSelectorChoiceIdentifier) selectorChoice1.getChoice();
			final OrderPaymentInstrumentSelectorChoiceIdentifier choiceId2 =
					(OrderPaymentInstrumentSelectorChoiceIdentifier) selectorChoice2.getChoice();
			final String piGuid1 = choiceId1.getSelectablePaymentInstrumentId().getValue().get(PAYMENT_INSTRUMENT_GUID_KEY);
			final String piGuid2 = choiceId2.getSelectablePaymentInstrumentId().getValue().get(PAYMENT_INSTRUMENT_GUID_KEY);
			return piGuid1.compareTo(piGuid2);
		}

	}

	@Override
	public Observable<SelectorChoice> getChoices(final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier) {
		final String storeCode = selectorIdentifier.getOrder().getScope().getValue();
		final String orderId = selectorIdentifier.getOrder().getOrderId().getValue();

		return reactiveAdapter.fromService(() -> resourceOperationContext.getUserIdentifier())
				.flatMapSingle(customerId -> customerRepository.getCustomer(customerId))
				.flatMap(customer -> cartOrderRepository.findByGuid(storeCode, orderId)
						.flatMapObservable(cartOrder -> cartOrderPaymentInstrumentRepository.findByCartOrder(cartOrder))
						.map(instrument -> createChosenPaymentInstrumentSelectorChoice(selectorIdentifier, instrument))
						.switchIfEmpty(reactiveAdapter.fromServiceAsMaybe(() -> filteredPaymentInstrumentService
								.findDefaultPaymentInstrumentForCustomerAndStore(customer, storeCode))
								.map(instrument -> createChosenDefaultPaymentInstrumentSelectorChoice(selectorIdentifier, instrument))
								.toObservable()
						)
						.concatWith(Observable.just(filteredPaymentInstrumentService
								.findCustomerPaymentInstrumentsForCustomerAndStore(customer, storeCode))
								.flatMap(Observable::fromIterable)
								.map(instrument -> createPaymentInstrumentSelectorChoice(selectorIdentifier, instrument))
						)
						.distinct(selectorChoice -> ((OrderPaymentInstrumentSelectorChoiceIdentifier) selectorChoice.getChoice())
								.getSelectablePaymentInstrumentId().getValue().get(PAYMENT_INSTRUMENT_GUID_KEY))
						.sorted(new SelectorChoiceComparatorForPaymentInstrumentGuid())
				);
	}

	private SelectorChoice createChosenPaymentInstrumentSelectorChoice(final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier,
																	   final CartOrderPaymentInstrument instrument) {
		return createSelectorChoice(selectorIdentifier, CompositeIdentifier.of(ImmutableMap.of(
				CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY, instrument.getGuid(),
				PAYMENT_INSTRUMENT_GUID_KEY, instrument.getPaymentInstrumentGuid()
		)), ChoiceStatus.CHOSEN);
	}

	private SelectorChoice createPaymentInstrumentSelectorChoice(final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier,
																 final CustomerPaymentInstrument instrument) {
		return createSelectorChoice(selectorIdentifier, CompositeIdentifier.of(ImmutableMap.of(
				CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, instrument.getGuid(),
				PAYMENT_INSTRUMENT_GUID_KEY, instrument.getPaymentInstrumentGuid()
		)), ChoiceStatus.CHOOSABLE);
	}

	private SelectorChoice createChosenDefaultPaymentInstrumentSelectorChoice(final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier,
																			  final CustomerPaymentInstrument instrument) {
		return createSelectorChoice(selectorIdentifier, CompositeIdentifier.of(ImmutableMap.of(
				CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, instrument.getGuid(),
				PAYMENT_INSTRUMENT_GUID_KEY, instrument.getPaymentInstrumentGuid(),
				IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT, Boolean.TRUE.toString()
		)), ChoiceStatus.CHOSEN);
	}

	private SelectorChoice createSelectorChoice(final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier,
												final IdentifierPart selectablePaymentInstrumentId,
												final ChoiceStatus choiceStatus) {
		return SelectorChoice.builder()
				.withChoice(OrderPaymentInstrumentSelectorChoiceIdentifier.builder()
						.withOrderPaymentInstrumentSelector(selectorIdentifier)
						.withSelectablePaymentInstrumentId(selectablePaymentInstrumentId)
						.build())
				.withStatus(choiceStatus)
				.build();
	}

	@Override
	public Single<Choice> getChoice(final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		final OrderPaymentInstrumentSelectorIdentifier selector = selectorChoiceIdentifier.getOrderPaymentInstrumentSelector();
		final Map<String, String> selectablePaymentInstrumentId = selectorChoiceIdentifier.getSelectablePaymentInstrumentId().getValue();

		final String cartOrderPaymentInstrumentGuid = selectablePaymentInstrumentId.get(CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY);
		if (cartOrderPaymentInstrumentGuid == null) {
			final String customerPaymentInstrumentGuid = selectablePaymentInstrumentId.get(CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY);

			if (Boolean.parseBoolean(selectablePaymentInstrumentId.get(IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT))) {
				return Single.just(Choice.builder()
						.withDescription(PaymentInstrumentIdentifier.builder()
								.withPaymentInstruments(PaymentInstrumentsIdentifier.builder()
										.withScope(selector.getOrder().getScope())
										.build())
								.withPaymentInstrumentId(StringIdentifier.of(customerPaymentInstrumentGuid))
								.build())
						.withAction(selectorChoiceIdentifier)
						.withStatus(ChoiceStatus.CHOSEN)
						.build());
			}

			return Single.just(Choice.builder()
					.withDescription(PaymentInstrumentIdentifier.builder()
							.withPaymentInstruments(PaymentInstrumentsIdentifier.builder()
									.withScope(selector.getOrder().getScope())
									.build())
							.withPaymentInstrumentId(StringIdentifier.of(customerPaymentInstrumentGuid))
							.build())
					.withAction(selectorChoiceIdentifier)
					.withStatus(ChoiceStatus.CHOOSABLE)
					.build());
		} else {
			return Single.just(Choice.builder()
					.withDescription(OrderPaymentInstrumentIdentifier.builder()
							.withOrder(selector.getOrder())
							.withPaymentInstrumentId(StringIdentifier.of(cartOrderPaymentInstrumentGuid))
							.build())
					.withAction(selectorChoiceIdentifier)
					.withStatus(ChoiceStatus.CHOSEN)
					.build());
		}
	}

	@Override
	public Single<SelectResult<OrderPaymentInstrumentSelectorIdentifier>> selectChoice(
			final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier = selectorChoiceIdentifier.getOrderPaymentInstrumentSelector();
		final Map<String, String> selectablePaymentInstrumentId = selectorChoiceIdentifier.getSelectablePaymentInstrumentId().getValue();

		final String cartOrderPaymentInstrumentGuid = selectablePaymentInstrumentId.get(CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY);
		if (cartOrderPaymentInstrumentGuid == null) {
			if (Boolean.parseBoolean(selectablePaymentInstrumentId.get(IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT))) {
				return Single.just(SelectResult.<OrderPaymentInstrumentSelectorIdentifier>builder()
						.withIdentifier(selectorIdentifier)
						.withStatus(SelectStatus.EXISTING)
						.build());
			}

			final String customerPaymentInstrumentGuid = selectablePaymentInstrumentId.get(CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY);
			final String storeCode = selectorIdentifier.getOrder().getScope().getValue();
			final String orderId = selectorIdentifier.getOrder().getOrderId().getValue();

			return cartOrderRepository.findByGuid(storeCode, orderId)
					.flatMap(cartOrder -> savePaymentInstrumentSelection(cartOrder, customerPaymentInstrumentGuid))
					.map(cartOrderPaymentInstrument -> SelectResult.<OrderPaymentInstrumentSelectorIdentifier>builder()
							.withIdentifier(selectorIdentifier)
							.withStatus(SelectStatus.SELECTED)
							.build());
		} else {
			return cartOrderPaymentInstrumentRepository.findByGuid(cartOrderPaymentInstrumentGuid)
					.flatMapCompletable(cartOrderPaymentInstrument -> cartOrderPaymentInstrumentRepository.remove(cartOrderPaymentInstrument))
					.andThen(Single.just(SelectResult.<OrderPaymentInstrumentSelectorIdentifier>builder()
							.withIdentifier(selectorIdentifier)
							.withStatus(SelectStatus.EXISTING)
							.build()));
		}
	}

	private Single<CartOrderPaymentInstrument> savePaymentInstrumentSelection(final CartOrder cartOrder,
																			  final String customerPaymentInstrumentGuid) {
		//TODO [payments] when multi-selection enabled - remove initial completable section (first two lines)
		return cartOrderPaymentInstrumentRepository.findByCartOrder(cartOrder)
				.flatMapCompletable(cartOrderPaymentInstrumentRepository::remove)
				.toSingle(() -> {
                    CartOrderPaymentInstrument cartOrderPaymentInstrument = beanFactory.getPrototypeBean(
                            CART_ORDER_PAYMENT_INSTRUMENT, CartOrderPaymentInstrument.class);
                    cartOrderPaymentInstrument.setCartOrderUid(cartOrder.getUidPk());
                    return cartOrderPaymentInstrument;
                }).flatMap(cartOrderPaymentInstrument ->
						configureCartOrderPaymentInstrument(customerPaymentInstrumentGuid, cartOrderPaymentInstrument));
	}

	private Single<CartOrderPaymentInstrument> configureCartOrderPaymentInstrument(final String customerPaymentInstrumentGuid,
																				   final CartOrderPaymentInstrument cartOrderPaymentInstrument) {
		return customerPaymentInstrumentRepository.findByGuid(customerPaymentInstrumentGuid)
				.map(CustomerPaymentInstrument::getPaymentInstrumentGuid)
				.flatMapCompletable(paymentInstrumentGuid ->
						Completable.fromRunnable(() -> cartOrderPaymentInstrument.setPaymentInstrumentGuid(paymentInstrumentGuid)))
				.andThen(Completable.fromRunnable(() -> {
					if (cartOrderPaymentInstrument.getLimitAmount() == null) {
						cartOrderPaymentInstrument.setLimitAmount(BigDecimal.ZERO);
					}
					cartOrderPaymentInstrument.setCurrency(SubjectUtil.getCurrency(resourceOperationContext.getSubject()));
				})).andThen(cartOrderPaymentInstrumentRepository.saveOrUpdate(cartOrderPaymentInstrument));
	}

	@Reference
	public void setCustomerPaymentInstrumentRepository(final CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository) {
		this.customerPaymentInstrumentRepository = customerPaymentInstrumentRepository;
	}

	@Reference
	public void setCartOrderPaymentInstrumentRepository(final CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository) {
		this.cartOrderPaymentInstrumentRepository = cartOrderPaymentInstrumentRepository;
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Reference
	public void setFilteredPaymentInstrumentService(final FilteredPaymentInstrumentService filteredPaymentInstrumentService) {
		this.filteredPaymentInstrumentService = filteredPaymentInstrumentService;
	}
}
