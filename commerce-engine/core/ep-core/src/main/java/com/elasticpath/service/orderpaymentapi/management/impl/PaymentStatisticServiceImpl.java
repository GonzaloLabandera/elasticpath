/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.management.impl;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatisticService;

/**
 * {@link PaymentStatisticService} default implementation.
 */
public class PaymentStatisticServiceImpl implements PaymentStatisticService {

	private final OrderPaymentService orderPaymentService;
	private final BeanFactory beanFactory;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentService order payment service
	 * @param beanFactory         EP bean factory
	 */
	public PaymentStatisticServiceImpl(final OrderPaymentService orderPaymentService, final BeanFactory beanFactory) {
		this.orderPaymentService = orderPaymentService;
		this.beanFactory = beanFactory;
	}

	@Override
	public List<PaymentStatistic> findPayments(final Order order,
											   final List<PaymentInstrumentDTO> instruments,
											   final Predicate<? super OrderPayment> filter) {
		return orderPaymentService.findByOrder(order)
                .stream()
                .filter(filter)
                .map(orderPayment -> createPaymentStatistic(order, orderPayment, instruments))
				.collect(Collectors.toList());
	}

    /**
     * Creates payment statistic pairing initial order payment with its instrument.
     *
     * @param order        order
     * @param orderPayment initial order payment
     * @param instruments  payment instruments associated with the order, see
     *                     {@link com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService#findOrderInstruments(Order)}
     * @return payment statistic
     */
    protected PaymentStatistic createPaymentStatistic(final Order order,
                                                      final OrderPayment orderPayment,
                                                      final List<PaymentInstrumentDTO> instruments) {
        final PaymentStatistic paymentStatistic = beanFactory.getPrototypeBean(ContextIdNames.PAYMENT_STATISTIC, PaymentStatistic.class);
        paymentStatistic.initOrderPayment(order, orderPayment, instruments);
        return paymentStatistic;
    }

	@Override
	public Collection<PaymentStatistic> accumulateByInstrument(final Collection<PaymentStatistic> payments) {
		return payments
				.stream()
				.collect(Collectors.groupingBy(
						paymentTransactionInfo -> paymentTransactionInfo.getInstrument().getGUID(),
						Collectors.reducing(null, Function.identity(), this::accumulate)))
				.values();
	}

	/**
	 * Accumulates payment transactions together.
	 *
	 * @param aggregate          payment statistic aggregate
	 * @param paymentTransaction another transaction in the same group
	 * @return payment statistic
	 */
	protected PaymentStatistic accumulate(final PaymentStatistic aggregate, final PaymentStatistic paymentTransaction) {
		if (aggregate == null) {
			return paymentTransaction;
		}
		aggregate.accumulate(paymentTransaction);
		return aggregate;
	}

	protected OrderPaymentService getOrderPaymentService() {
		return orderPaymentService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
