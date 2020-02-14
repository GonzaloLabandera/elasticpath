/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_PAYMENT_INSTRUMENT;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl.OrderPaymentInstrumentSelectorRepositoryImpl.CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl.OrderPaymentInstrumentSelectorRepositoryImpl.CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl.OrderPaymentInstrumentSelectorRepositoryImpl.IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl.OrderPaymentInstrumentSelectorRepositoryImpl.PAYMENT_INSTRUMENT_GUID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentInstrumentSelectorRepositoryTest {

	private static final String STORE_CODE = "STORE_CODE";
	private static final String CART_ORDER_GUID = "CART_ORDER_GUID";
	private static final String CUSTOMER_GUID = "CUSTOMER_GUID";
	private static final String CUSTOMER_PAYMENT_INSTRUMENT_GUID = "CUSTOMER_PAYMENT_INSTRUMENT_GUID";
	private static final String DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT_GUID = "DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT_GUID";
	private static final String CART_ORDER_PAYMENT_INSTRUMENT_GUID = "CART_ORDER_PAYMENT_INSTRUMENT_GUID";
	private static final String COPI_PAYMENT_INSTRUMENT_GUID = "A_COPI_PAYMENT_INSTRUMENT_GUID";
	private static final String CPI_PAYMENT_INSTRUMENT_GUID = "B_CPI_PAYMENT_INSTRUMENT_GUID";
	private static final String DEFAULT_CPI_PAYMENT_INSTRUMENT_GUID = "C_DEFAULT_CPI_PAYMENT_INSTRUMENT_GUID";
	private static final long ORDER_UID = 404L;

	@Mock
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;

	@Mock
	private CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@InjectMocks
	private OrderPaymentInstrumentSelectorRepositoryImpl<
			OrderPaymentInstrumentSelectorIdentifier,
			OrderPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	private CartOrder cartOrder;
	private Customer customer;
	private CustomerPaymentInstrument existingCustomerPaymentInstrument;
	private CustomerPaymentInstrument defaultCustomerPaymentInstrument;
	private CartOrderPaymentInstrument existingCartOrderPaymentInstrument;
	private CartOrderPaymentInstrument newCartOrderPaymentInstrument;

	@Before
	public void setUp() {
		cartOrder = mock(CartOrder.class);
		customer = mock(Customer.class);
		existingCartOrderPaymentInstrument = mock(CartOrderPaymentInstrument.class);
		newCartOrderPaymentInstrument = mock(CartOrderPaymentInstrument.class);
		existingCustomerPaymentInstrument = mock(CustomerPaymentInstrument.class);
		defaultCustomerPaymentInstrument = mock(CustomerPaymentInstrument.class);

		when(resourceOperationContext.getUserIdentifier()).thenReturn(CUSTOMER_GUID);

		final Subject subject = mock(Subject.class);
		when(resourceOperationContext.getSubject()).thenReturn(subject);

		when(cartOrderRepository.findByGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(Single.just(cartOrder));
		when(existingCartOrderPaymentInstrument.getGuid()).thenReturn(CART_ORDER_PAYMENT_INSTRUMENT_GUID);
		when(existingCartOrderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(COPI_PAYMENT_INSTRUMENT_GUID);
		when(cartOrderPaymentInstrumentRepository.findByCartOrder(cartOrder)).thenReturn(Observable.just(existingCartOrderPaymentInstrument));
		when(cartOrderPaymentInstrumentRepository.findByGuid(CART_ORDER_PAYMENT_INSTRUMENT_GUID))
				.thenReturn(Single.just(existingCartOrderPaymentInstrument));
		when(cartOrderPaymentInstrumentRepository.remove(existingCartOrderPaymentInstrument)).thenReturn(Completable.complete());

		when(beanFactory.getPrototypeBean(CART_ORDER_PAYMENT_INSTRUMENT, CartOrderPaymentInstrument.class)).thenReturn(newCartOrderPaymentInstrument);
		when(cartOrderPaymentInstrumentRepository.saveOrUpdate(newCartOrderPaymentInstrument)).thenReturn(Single.just(newCartOrderPaymentInstrument));

		when(customerRepository.getCustomer(CUSTOMER_GUID)).thenReturn(Single.just(customer));
		when(filteredPaymentInstrumentService.findCustomerPaymentInstrumentsForCustomerAndStore(customer, STORE_CODE))
				.thenReturn(ImmutableList.of(existingCustomerPaymentInstrument));

		when(existingCustomerPaymentInstrument.getGuid()).thenReturn(CUSTOMER_PAYMENT_INSTRUMENT_GUID);
		when(existingCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(CPI_PAYMENT_INSTRUMENT_GUID);
		when(customerPaymentInstrumentRepository.findByGuid(CUSTOMER_PAYMENT_INSTRUMENT_GUID))
				.thenReturn(Single.just(existingCustomerPaymentInstrument));

		when(defaultCustomerPaymentInstrument.getGuid()).thenReturn(DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT_GUID);
		when(defaultCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(DEFAULT_CPI_PAYMENT_INSTRUMENT_GUID);

		selectorRepository.setReactiveAdapter(new ReactiveAdapterImpl(exceptionTransformer));
	}

	@Test
	public void testGetChoicesReturnsCartOrderPIAsChosenAndCustomerPIAsChoosable() {
		selectorRepository.getChoices(createOrderPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, selectorChoiceOf(ImmutableMap.of(
						CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY, CART_ORDER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, COPI_PAYMENT_INSTRUMENT_GUID
				), ChoiceStatus.CHOSEN))
				.assertValueAt(1, selectorChoiceOf(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, CPI_PAYMENT_INSTRUMENT_GUID
				), ChoiceStatus.CHOOSABLE));
	}

	@Test
	public void testGetChoicesReturnsDistinctPI() {
		when(existingCartOrderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(CPI_PAYMENT_INSTRUMENT_GUID);

		selectorRepository.getChoices(createOrderPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValueAt(0, selectorChoiceOf(ImmutableMap.of(
						CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY, CART_ORDER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, CPI_PAYMENT_INSTRUMENT_GUID
				), ChoiceStatus.CHOSEN));
	}

	@Test
	public void testGetChoicesReturnsDefaultPIAsChosenWhenNoCartOrderPIs() {
		simulateDefaultCustomerPaymentInstrument();

		selectorRepository.getChoices(createOrderPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, selectorChoiceOf(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, CPI_PAYMENT_INSTRUMENT_GUID
				), ChoiceStatus.CHOOSABLE))
				.assertValueAt(1, selectorChoiceOf(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, DEFAULT_CPI_PAYMENT_INSTRUMENT_GUID,
						IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT, Boolean.TRUE.toString()
				), ChoiceStatus.CHOSEN));
	}

	@Test
	public void testGetChoicesReturnsDistinctDefaultPI() {
		simulateDefaultCustomerPaymentInstrument();
		when(existingCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(DEFAULT_CPI_PAYMENT_INSTRUMENT_GUID);

		selectorRepository.getChoices(createOrderPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValueAt(0, selectorChoiceOf(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, DEFAULT_CPI_PAYMENT_INSTRUMENT_GUID,
						IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT, Boolean.TRUE.toString()
				), ChoiceStatus.CHOSEN));
	}

	@Test
	public void testGetChoicesReturnsSortedResult() {
		when(existingCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn("A");
		when(existingCartOrderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn("B");

		selectorRepository.getChoices(createOrderPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueAt(0, selectorChoiceOf(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, "A"
				), ChoiceStatus.CHOOSABLE))
				.assertValueAt(1, selectorChoiceOf(ImmutableMap.of(
						CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY, CART_ORDER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, "B"
				), ChoiceStatus.CHOSEN));
	}

	@Test
	public void testGetChoicesReturnsSortedResultWithDefault() {
		simulateDefaultCustomerPaymentInstrument();

		when(defaultCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn("A");
		when(existingCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn("B");

		selectorRepository.getChoices(createOrderPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueAt(0, selectorChoiceOf(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, "A",
						IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT, Boolean.TRUE.toString()
				), ChoiceStatus.CHOSEN))
				.assertValueAt(1, selectorChoiceOf(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						PAYMENT_INSTRUMENT_GUID_KEY, "B"
				), ChoiceStatus.CHOOSABLE));
	}

	private void simulateDefaultCustomerPaymentInstrument() {
		when(cartOrderPaymentInstrumentRepository.findByCartOrder(cartOrder)).thenReturn(Observable.empty());
		when(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, STORE_CODE))
				.thenReturn(defaultCustomerPaymentInstrument);
		when(filteredPaymentInstrumentService.findCustomerPaymentInstrumentsForCustomerAndStore(customer, STORE_CODE))
				.thenReturn(ImmutableList.of(existingCustomerPaymentInstrument, defaultCustomerPaymentInstrument));
	}

	private Predicate<SelectorChoice> selectorChoiceOf(final Map<String, String> instrumentId, final ChoiceStatus choiceStatus) {
		return selectorChoice -> {
			final OrderPaymentInstrumentSelectorChoiceIdentifier choiceIdentifier =
					(OrderPaymentInstrumentSelectorChoiceIdentifier) selectorChoice.getChoice();
			assertThat(choiceIdentifier.getOrderPaymentInstrumentSelector()).isEqualTo(createOrderPaymentInstrumentSelectorIdentifier());
			assertThat(choiceIdentifier.getSelectablePaymentInstrumentId().getValue()).isEqualTo(instrumentId);
			assertThat(selectorChoice.getStatus()).isEqualTo(choiceStatus);
			return true;
		};
	}

	@Test
	public void testGetChoiceSelectCartOrderPIReturnsItChosen() {
		final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier =
				createOrderPaymentInstrumentSelectorChoiceIdentifier(ImmutableMap.of(
						CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY,
						CART_ORDER_PAYMENT_INSTRUMENT_GUID));

		selectorRepository.getChoice(selectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choiceOf(CART_ORDER_PAYMENT_INSTRUMENT_GUID, selectorChoiceIdentifier, ChoiceStatus.CHOSEN));
	}

	@Test
	public void testGetChoiceSelectCustomerPIReturnsItChoosable() {
		final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier =
				createOrderPaymentInstrumentSelectorChoiceIdentifier(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY,
						CUSTOMER_PAYMENT_INSTRUMENT_GUID));

		selectorRepository.getChoice(selectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choiceOf(CUSTOMER_PAYMENT_INSTRUMENT_GUID, selectorChoiceIdentifier, ChoiceStatus.CHOOSABLE));
	}

	@Test
	public void testGetChoiceSelectDefaultCustomerPIReturnsItChosen() {
		final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier =
				createOrderPaymentInstrumentSelectorChoiceIdentifier(ImmutableMap.of(
						CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, CUSTOMER_PAYMENT_INSTRUMENT_GUID,
						IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT, Boolean.TRUE.toString()));

		selectorRepository.getChoice(selectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choiceOf(CUSTOMER_PAYMENT_INSTRUMENT_GUID, selectorChoiceIdentifier, ChoiceStatus.CHOSEN));
	}

	private Predicate<Choice> choiceOf(final String instrumentGuid, final ResourceIdentifier action, final ChoiceStatus choiceStatus) {
		return choice -> {
			assertThat(choice.getAction()).isEqualTo(Optional.ofNullable(action));
			assertThat(choice.getStatus()).isEqualTo(choiceStatus);
			if (choice.getDescription() instanceof OrderPaymentInstrumentIdentifier) {
				final OrderPaymentInstrumentIdentifier orderPaymentInstrumentIdentifier = (OrderPaymentInstrumentIdentifier) choice.getDescription();
				assertThat(orderPaymentInstrumentIdentifier.getOrder()).isEqualTo(createOrderIdentifier());
				assertThat(orderPaymentInstrumentIdentifier.getPaymentInstrumentId().getValue()).isEqualTo(instrumentGuid);
			} else {
				final PaymentInstrumentIdentifier paymentInstrumentIdentifier = (PaymentInstrumentIdentifier) choice.getDescription();
				assertThat(paymentInstrumentIdentifier.getPaymentInstruments()).isEqualTo(PaymentInstrumentsIdentifier.builder()
						.withScope(StringIdentifier.of(STORE_CODE))
						.build());
				assertThat(paymentInstrumentIdentifier.getPaymentInstrumentId().getValue()).isEqualTo(instrumentGuid);
			}
			return true;
		};
	}

	@Test
	public void testSelectChoiceCartOrderPITogglesIt() {
		selectorRepository.selectChoice(createOrderPaymentInstrumentSelectorChoiceIdentifier(ImmutableMap.of(
				CART_ORDER_PAYMENT_INSTRUMENT_GUID_KEY, CART_ORDER_PAYMENT_INSTRUMENT_GUID)))

				.test()
				.assertNoErrors()
				.assertValue(result -> result.getStatus() == SelectStatus.EXISTING)
				.assertValue(result -> result.getIdentifier().getOrder().getOrderId().getValue().equals(CART_ORDER_GUID));

		verify(cartOrderPaymentInstrumentRepository).remove(existingCartOrderPaymentInstrument);
		verify(cartOrderPaymentInstrumentRepository, never()).saveOrUpdate(any());
	}

	@Test
	public void testSelectChoiceCustomerPIRemovesOldCartOrderPIAndCreatesNewOne() {
		when(cartOrder.getUidPk()).thenReturn(ORDER_UID);

        selectorRepository.selectChoice(createOrderPaymentInstrumentSelectorChoiceIdentifier(ImmutableMap.of(
                CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, CUSTOMER_PAYMENT_INSTRUMENT_GUID)))

                .test()
                .assertNoErrors()
                .assertValue(result -> result.getStatus() == SelectStatus.SELECTED)
                .assertValue(result -> result.getIdentifier().getOrder().getOrderId().getValue().equals(CART_ORDER_GUID));

        verify(cartOrderPaymentInstrumentRepository).remove(existingCartOrderPaymentInstrument);

		verify(newCartOrderPaymentInstrument).setPaymentInstrumentGuid(CPI_PAYMENT_INSTRUMENT_GUID);
		verify(newCartOrderPaymentInstrument).setCartOrderUid(ORDER_UID);

        verify(cartOrderPaymentInstrumentRepository).saveOrUpdate(newCartOrderPaymentInstrument);
    }

	@Test
	public void testSelectChoiceDefaultCustomerPIDoesNothing() {
		selectorRepository.selectChoice(createOrderPaymentInstrumentSelectorChoiceIdentifier(ImmutableMap.of(
				CUSTOMER_PAYMENT_INSTRUMENT_GUID_KEY, CUSTOMER_PAYMENT_INSTRUMENT_GUID,
				IS_DEFAULT_CUSTOMER_PAYMENT_INSTRUMENT, Boolean.TRUE.toString()
		)))
				.test()
				.assertNoErrors()
				.assertValue(result -> result.getStatus() == SelectStatus.EXISTING)
				.assertValue(result -> result.getIdentifier().getOrder().getOrderId().getValue().equals(CART_ORDER_GUID));

		verifyZeroInteractions(cartOrderPaymentInstrumentRepository);
	}

	private static OrderPaymentInstrumentSelectorChoiceIdentifier createOrderPaymentInstrumentSelectorChoiceIdentifier(
			final Map<String, String> selectablePaymentInstrumentId) {
		return OrderPaymentInstrumentSelectorChoiceIdentifier.builder()
				.withOrderPaymentInstrumentSelector(createOrderPaymentInstrumentSelectorIdentifier())
				.withSelectablePaymentInstrumentId(CompositeIdentifier.of(selectablePaymentInstrumentId))
				.build();
	}

	private static OrderPaymentInstrumentSelectorIdentifier createOrderPaymentInstrumentSelectorIdentifier() {
		return OrderPaymentInstrumentSelectorIdentifier.builder()
				.withOrder(createOrderIdentifier())
				.build();
	}

	private static OrderIdentifier createOrderIdentifier() {
		return OrderIdentifier.builder()
				.withScope(StringIdentifier.of(STORE_CODE))
				.withOrderId(StringIdentifier.of(CART_ORDER_GUID))
				.build();
	}
}
