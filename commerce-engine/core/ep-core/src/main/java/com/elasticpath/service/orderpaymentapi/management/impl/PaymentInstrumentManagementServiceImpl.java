/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.orderpaymentapi.management.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.workflow.PaymentInstrumentWorkflow;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;

/**
 * Order Payment API instrument management service proxying Payment API workflow.
 */
public class PaymentInstrumentManagementServiceImpl implements PaymentInstrumentManagementService {

	private final PaymentInstrumentWorkflow paymentInstrumentWorkflow;
	private final OrderPaymentInstrumentService orderPaymentInstrumentService;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentWorkflow     payment instrument workflow service
	 * @param orderPaymentInstrumentService order payment instrument service
	 */
	@Inject
	public PaymentInstrumentManagementServiceImpl(final PaymentInstrumentWorkflow paymentInstrumentWorkflow,
												  final OrderPaymentInstrumentService orderPaymentInstrumentService) {
		this.paymentInstrumentWorkflow = paymentInstrumentWorkflow;
		this.orderPaymentInstrumentService = orderPaymentInstrumentService;
	}

	@Override
	public PaymentInstrumentDTO getPaymentInstrument(final String paymentInstrumentGuid) {
		return paymentInstrumentWorkflow.findByGuid(paymentInstrumentGuid);
	}

	@Override
	public List<PaymentInstrumentDTO> findOrderInstruments(final Order order) {
		return orderPaymentInstrumentService.findByOrder(order)
				.stream()
				.map(OrderPaymentInstrument::getPaymentInstrumentGuid)
				.map(this::getPaymentInstrument)
				.collect(Collectors.toList());
	}

	@Override
	public List<PaymentInstrumentDTO> findUnlimitedOrderInstruments(final Order order) {
		return orderPaymentInstrumentService.findByOrder(order)
				.stream()
				.filter(instrument -> instrument.getLimitAmount().compareTo(BigDecimal.ZERO) == 0)
				.map(OrderPaymentInstrument::getPaymentInstrumentGuid)
				.map(this::getPaymentInstrument)
				.limit(1)
				.collect(Collectors.toList());
	}

	@Override
	public PaymentInstrumentDTO findByOrderPaymentInstrumentGuid(final String orderPaymentInstrumentGuid) {
		final String paymentInstrumentGuid = getPaymentInstrumentGuid(orderPaymentInstrumentGuid);

		return getPaymentInstrument(paymentInstrumentGuid);

	}

	private String getPaymentInstrumentGuid(final String orderPaymentInstrumentGuid) {
		return orderPaymentInstrumentService.findByGuid(orderPaymentInstrumentGuid).getPaymentInstrumentGuid();
	}

	protected PaymentInstrumentWorkflow getPaymentInstrumentWorkflow() {
		return paymentInstrumentWorkflow;
	}

	protected OrderPaymentInstrumentService getOrderPaymentInstrumentService() {
		return orderPaymentInstrumentService;
	}
}
