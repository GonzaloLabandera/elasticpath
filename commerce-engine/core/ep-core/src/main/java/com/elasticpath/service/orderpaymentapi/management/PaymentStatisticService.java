/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi.management;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Service that generates statistic for payment events.
 */
public interface PaymentStatisticService {

	/**
	 * Finds all payment events for a particular order by specific filter criteria.
	 *
	 * @param order       the order
	 * @param instruments payment instruments associated with the order, see {@link PaymentInstrumentManagementService#findOrderInstruments(Order)}
	 * @param filter      {@link OrderPayment} filter
	 * @return list of payment transactions
	 */
	List<PaymentStatistic> findPayments(Order order, List<PaymentInstrumentDTO> instruments, Predicate<? super OrderPayment> filter);

	/**
	 * Accumulates payment events by the instrument summing up their amounts.
	 *
	 * @param payments payment events
	 * @return grouped payment event statistic
	 */
	Collection<PaymentStatistic> accumulateByInstrument(Collection<PaymentStatistic> payments);

}
