/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions.impl;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_INSTRUMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.impl.OrderPaymentInstrumentImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContextImpl;

@RunWith(MockitoJUnitRunner.class)
public class CreateOrderPaymentInstrumentsCheckoutActionTest {

	private static final String PAYMENT_INSTRUMENT_GUID = "PAYMENT_INSTRUMENT_GUID";
	private static final String STORE_CODE = "mobee";

	@InjectMocks
	private CreateOrderPaymentInstrumentsCheckoutAction testee;

	@Mock
	private OrderPaymentInstrumentService orderPaymentInstrumentService;
	@Mock
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;
	@Mock
	private OrderPaymentApiCleanupService orderPaymentApiCleanupService;
	@Mock
	private BeanFactory beanFactory;
	@Mock
	private CartOrder cartOrder;
	@Mock
	private Customer customer;
	@Mock
	private Order order;

	private PreCaptureCheckoutActionContext context;

	@Before
	public void setUp() throws Exception {
		final ShoppingCart shoppingCart = mock(ShoppingCart.class);
		final Shopper shopper = mock(Shopper.class);
		when(shoppingCart.getShopper()).thenReturn(shopper);

		final ShoppingCartTaxSnapshot shoppingCartTaxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		final CustomerSession customerSession = mock(CustomerSession.class);
		final OrderReturn orderReturn = mock(OrderReturn.class);

		context = new PreCaptureCheckoutActionContextImpl(
				shoppingCart,
				shoppingCartTaxSnapshot,
				customerSession,
				false,
				false,
				orderReturn,
				(customer, order) -> cartOrder);

		context.setOrder(order);

		when(beanFactory.getPrototypeBean(ORDER_PAYMENT_INSTRUMENT, OrderPaymentInstrument.class)).thenReturn(new OrderPaymentInstrumentImpl());
		when(order.getOrderNumber()).thenReturn("20000");
		when(order.getStoreCode()).thenReturn(STORE_CODE);
		when(order.getTotal()).thenReturn(BigDecimal.TEN);
		when(order.getAccount()).thenReturn(customer);

		final CartOrderPaymentInstrument cartOrderPaymentInstrument = mock(CartOrderPaymentInstrument.class);
		when(cartOrderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_GUID);
		when(cartOrderPaymentInstrument.getLimitAmount()).thenReturn(BigDecimal.ONE);
		when(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderAndStore(cartOrder, context.getOrder().getStoreCode()))
				.thenReturn(Collections.singletonList(cartOrderPaymentInstrument));
	}

	@Test
	public void executeCopiesPaymentInstrumentsToOrder() {
		testee.execute(context);

		final ArgumentCaptor<OrderPaymentInstrument> instrumentArgumentCaptor = ArgumentCaptor.forClass(OrderPaymentInstrument.class);
		verify(orderPaymentInstrumentService).saveOrUpdate(instrumentArgumentCaptor.capture());
		final OrderPaymentInstrument orderPaymentInstrument = instrumentArgumentCaptor.getValue();
		assertThat(orderPaymentInstrument.getOrderNumber()).isEqualTo(context.getOrder().getOrderNumber());
		assertThat(orderPaymentInstrument.getPaymentInstrumentGuid()).isEqualTo(PAYMENT_INSTRUMENT_GUID);
		assertThat(orderPaymentInstrument.getLimitAmount()).isEqualTo(BigDecimal.ONE);
	}

	@Test
	public void executeUsingDefaultAccountPaymentInstrument() {
		when(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderAndStore(cartOrder, context.getOrder().getStoreCode()))
				.thenReturn(Collections.emptyList());

		testee.execute(context);

		verify(filteredPaymentInstrumentService).findDefaultPaymentInstrumentForCustomerAndStore(customer, STORE_CODE);
	}

	@Test
	public void rollbackRemovesOrderPaymentInstruments() {
		testee.rollback(context);

		verify(orderPaymentApiCleanupService).removeByOrder(context.getOrder());
	}
}
