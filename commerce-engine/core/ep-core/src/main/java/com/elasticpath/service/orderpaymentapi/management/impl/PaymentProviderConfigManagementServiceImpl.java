/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi.management.impl;

import java.util.List;

import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.workflow.PaymentProviderConfigWorkflow;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Service to interact with payment provider config workflow.
 */
public class PaymentProviderConfigManagementServiceImpl implements PaymentProviderConfigManagementService {

	private final PaymentProviderConfigWorkflow paymentProviderConfigWorkflow;

	/**
	 * Constructor.
	 *
	 * @param paymentProviderConfigWorkflow payment provider configuration workflow service
	 */
	public PaymentProviderConfigManagementServiceImpl(final PaymentProviderConfigWorkflow paymentProviderConfigWorkflow) {
		this.paymentProviderConfigWorkflow = paymentProviderConfigWorkflow;
	}

	@Override
	public List<PaymentProviderConfigDTO> findAll() {
		return paymentProviderConfigWorkflow.findAll();
	}

	@Override
	public List<PaymentProviderConfigDTO> findAllActive() {
		return paymentProviderConfigWorkflow.findByStatus(PaymentProviderConfigurationStatus.ACTIVE);
	}

	@Override
	public PaymentProviderConfigDTO findByGuid(final String guid) {
		return paymentProviderConfigWorkflow.findByGuid(guid);
	}

	@Override
	public List<PaymentProviderConfigDTO> findByGuids(final List<String> guids) {
		return paymentProviderConfigWorkflow.findByGuids(guids);
	}

	@Override
	public PaymentProviderConfigDTO saveOrUpdate(final PaymentProviderConfigDTO paymentProviderConfiguration) {
		return paymentProviderConfigWorkflow.saveOrUpdate(paymentProviderConfiguration);
	}

	protected PaymentProviderConfigWorkflow getPaymentProviderConfigWorkflow() {
		return paymentProviderConfigWorkflow;
	}
}
