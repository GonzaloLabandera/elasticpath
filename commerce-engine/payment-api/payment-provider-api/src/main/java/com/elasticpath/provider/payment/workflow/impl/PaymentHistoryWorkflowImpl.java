/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow.impl;

import java.util.List;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequest;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponse;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponseBuilder;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.workflow.PaymentHistoryWorkflow;

/**
 * The Payment api workflow implementation.
 */
public class PaymentHistoryWorkflowImpl implements PaymentHistoryWorkflow {

	private final BeanFactory beanFactory;
	private final PaymentHistory paymentHistory;

	/**
	 * Constructor.
	 *
	 * @param beanFactory                         EP bean factory
	 * @param paymentHistory              payment provider service
	 */
	public PaymentHistoryWorkflowImpl(final BeanFactory beanFactory,
									  final PaymentHistory paymentHistory) {
		this.beanFactory = beanFactory;
		this.paymentHistory = paymentHistory;
	}

	@Override
	public PaymentEventHistoryResponse getPaymentEventHistoryAmounts(final PaymentEventHistoryRequest paymentEventHistoryRequest) {
		final List<PaymentEvent> ledger = paymentEventHistoryRequest.getLedger();
		return PaymentEventHistoryResponseBuilder.builder()
				.withAmountCharged(paymentHistory.getChargedAmount(ledger))
				.withAmountRefunded(paymentHistory.getRefundedAmount(ledger))
				.build(beanFactory);
	}
}
