/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi.management.impl;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginsDTO;
import com.elasticpath.provider.payment.workflow.PaymentProviderWorkflow;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderManagementService;

/**
 * Service to interact with payment provider config workflow.
 */
public class PaymentProviderManagementServiceImpl implements PaymentProviderManagementService {

	private final PaymentProviderWorkflow paymentProviderWorkflow;

	/**
	 * Constructor.
	 *
	 * @param paymentProviderWorkflow payment provider workflow service
	 */
	public PaymentProviderManagementServiceImpl(final PaymentProviderWorkflow paymentProviderWorkflow) {
		this.paymentProviderWorkflow = paymentProviderWorkflow;
	}

	@Override
	public Map<String, PaymentProviderPluginDTO> findAll() {
		PaymentProviderPluginsDTO paymentProviderPluginsDTO = paymentProviderWorkflow.findAll();
		Map<String, PaymentProviderPluginDTO> paymentProviderPluginDTOMap =
				paymentProviderPluginsDTO.getPaymentProviderPluginDTOs().stream()
						.collect(Collectors.toMap(PaymentProviderPluginDTO::getPluginBeanName, Function.identity()));
		return Collections.unmodifiableMap(paymentProviderPluginDTOMap);
	}

	protected PaymentProviderWorkflow getPaymentProviderWorkflow() {
		return paymentProviderWorkflow;
	}
}
