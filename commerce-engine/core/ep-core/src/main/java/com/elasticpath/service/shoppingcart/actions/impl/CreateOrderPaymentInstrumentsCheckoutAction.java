/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions.impl;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_INSTRUMENT;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * CheckoutAction to create OrderPaymentInstruments from CartOrderPaymentInstruments.
 */
public class CreateOrderPaymentInstrumentsCheckoutAction implements ReversibleCheckoutAction {

	private static final Logger LOG = Logger.getLogger(CreateOrderPaymentInstrumentsCheckoutAction.class);

	private OrderPaymentInstrumentService orderPaymentInstrumentService;
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;
	private OrderPaymentApiCleanupService orderPaymentApiCleanupService;
	private BeanFactory beanFactory;

	@Override
	public void execute(final CheckoutActionContext context) throws EpSystemException {
		final CartOrder cartOrder = context.getCartOrder();
		if (cartOrder != null) {
			final Collection<CartOrderPaymentInstrument> instruments = filteredPaymentInstrumentService
					.findCartOrderPaymentInstrumentsForCartOrderAndStore(cartOrder, context.getOrder().getStoreCode());
			if (instruments.isEmpty()) {
				useDefaultCustomerPaymentInstrumentForOrder(context.getOrder());
			} else {
				instruments.forEach(instrument -> copyToOrder(instrument, context.getOrder()));
			}
		}
	}

	private void copyToOrder(final CartOrderPaymentInstrument sourceInstrument, final Order order) {
		final OrderPaymentInstrument destinationInstrument = beanFactory.getPrototypeBean(
				ORDER_PAYMENT_INSTRUMENT, OrderPaymentInstrument.class);
		destinationInstrument.setOrderNumber(order.getOrderNumber());

		destinationInstrument.setPaymentInstrumentGuid(sourceInstrument.getPaymentInstrumentGuid());
		destinationInstrument.setLimitAmount(sourceInstrument.getLimitAmount());
		destinationInstrument.setCurrency(sourceInstrument.getCurrency());

		orderPaymentInstrumentService.saveOrUpdate(destinationInstrument);
	}

	private void useDefaultCustomerPaymentInstrumentForOrder(final Order order) {
		final CustomerPaymentInstrument defaultCustomerPaymentInstrument =
				filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(order.getCustomer(), order.getStoreCode());
		if (defaultCustomerPaymentInstrument == null) {
			if (order.getTotal().compareTo(BigDecimal.ZERO) > 0) {
				LOG.warn("Attempting to purchase non-free product without payment instrument");
			}
			return;
		}

		final OrderPaymentInstrument destinationInstrument = beanFactory.getPrototypeBean(
				ORDER_PAYMENT_INSTRUMENT, OrderPaymentInstrument.class);
		destinationInstrument.setOrderNumber(order.getOrderNumber());
		destinationInstrument.setLimitAmount(BigDecimal.ZERO);
		destinationInstrument.setCurrency(order.getCurrency());
		destinationInstrument.setPaymentInstrumentGuid(defaultCustomerPaymentInstrument.getPaymentInstrumentGuid());
		orderPaymentInstrumentService.saveOrUpdate(destinationInstrument);
	}

	@Override
	public void rollback(final CheckoutActionContext context) throws EpSystemException {
		final Order order = context.getOrder();
		if (order == null) {
			LOG.error("Order not found in checkout action context");
		} else {
			orderPaymentApiCleanupService.removeByOrder(order);
		}
	}

	public void setOrderPaymentInstrumentService(final OrderPaymentInstrumentService orderPaymentInstrumentService) {
		this.orderPaymentInstrumentService = orderPaymentInstrumentService;
	}

	public void setOrderPaymentApiCleanupService(final OrderPaymentApiCleanupService orderPaymentApiCleanupService) {
		this.orderPaymentApiCleanupService = orderPaymentApiCleanupService;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setFilteredPaymentInstrumentService(final FilteredPaymentInstrumentService filteredPaymentInstrumentService) {
		this.filteredPaymentInstrumentService = filteredPaymentInstrumentService;
	}
}
