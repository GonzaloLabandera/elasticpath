/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.order.impl;

import static com.elasticpath.commons.constants.ContextIdNames.EVENT_ORIGINATOR_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_EVENT_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_RETURN_SERVICE;
import static com.elasticpath.service.order.ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.cmuser.impl.CmUserImpl;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnSku;
import com.elasticpath.domain.order.OrderReturnStatus;
import com.elasticpath.domain.order.OrderReturnType;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.OrderReturnImpl;
import com.elasticpath.domain.order.impl.OrderReturnSkuImpl;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.tax.ReturnTaxOperationService;

@RunWith(MockitoJUnitRunner.class)
public class ReturnAndExchangeServiceImplTest {

	@InjectMocks
	private ReturnAndExchangeServiceImpl returnAndExchangeService;

	@Mock
	private ReturnTaxOperationService mockReturnTaxOperationService;

	@Mock
	private EventOriginatorHelper mockEventOriginatorHelper;
	@Mock
	private EventOriginator mockEventOriginator;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private TimeService mockTimeService;

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private OrderEventHelper orderEventHelper;

	@Mock
	private OrderService orderService;

	private OrderReturnImpl orderReturn;

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private final ElasticPathImpl elasticPath = (ElasticPathImpl) ElasticPathImpl.getInstance();

	/**
	 * Prepares for tests.
	 */
	@Before
	public void setUp() throws Exception {
		elasticPath.setBeanFactory(beanFactory);
		orderReturn = new OrderReturnImpl();
		final CmUser user = new CmUserImpl();
		orderReturn.setCreatedByCmUser(user);
		when(beanFactory.getSingletonBean(EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class)).thenReturn(mockEventOriginatorHelper);
		when(mockTimeService.getCurrentTime()).thenReturn(new Date());
		when(persistenceEngine.merge(orderReturn)).thenReturn(orderReturn);
		when(mockEventOriginatorHelper.getCmUserOriginator(user)).thenReturn(mockEventOriginator);
		when(beanFactory.getSingletonBean(ORDER_EVENT_HELPER, OrderEventHelper.class)).thenReturn(orderEventHelper);
		doNothing().when(mockReturnTaxOperationService).reverseTaxes(orderReturn, null);
		doNothing().when(orderEventHelper).logOrderExchangeCanceled(null, orderReturn);
		when(beanFactory.getSingletonBean(ORDER_RETURN_SERVICE, ReturnAndExchangeService.class)).thenReturn(returnAndExchangeService);
	}

	@Test
	public void cancelExchangeShouldCancelExchangeOrder() {
		final Order orderExchange = new OrderImpl();
		orderReturn.setExchangeOrder(orderExchange);
		orderReturn.setReturnStatus(OrderReturnStatus.AWAITING_STOCK_RETURN);
		orderReturn.setReturnType(OrderReturnType.EXCHANGE);

		when(orderService.cancelOrder(orderExchange)).thenReturn(cancelOrder(orderExchange));
		final OrderReturn result = returnAndExchangeService.cancelReturnExchange(orderReturn);

		assertThat(result.getExchangeOrder().getStatus()).isEqualTo(OrderStatus.CANCELLED);
	}

	@Test
	public void completeExchangePossibleWhenExchangeOrderCancelled() {
		final Order orderExchange = new OrderImpl();
		cancelOrder(orderExchange);
		final OrderReturnSku orderReturnSku = new OrderReturnSkuImpl();
		final OrderSku orderSku = new OrderSkuImpl();
		final OrderShipment orderShipment = new PhysicalOrderShipmentImpl();
		orderSku.setShipment(orderShipment);
		orderReturnSku.setOrderSku(orderSku);
		orderReturn.setReturnStatus(OrderReturnStatus.AWAITING_STOCK_RETURN);
		orderReturn.setReturnType(OrderReturnType.EXCHANGE);
		orderReturn.setExchangeOrder(orderExchange);
		orderReturn.setOrderReturnSkus(ImmutableSet.of(orderReturnSku));
		orderReturn.setReturnTotal(BigDecimal.ZERO);

		final OrderReturn result = returnAndExchangeService.completeExchange(orderReturn, REFUND_TO_ORIGINAL);
		assertThat(result.getExchangeOrder().getStatus()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(result.getReturnStatus()).isEqualTo(OrderReturnStatus.COMPLETED);
	}

	private Order cancelOrder(final Order order) {
		order.cancelOrder();
		return order;
	}
}
