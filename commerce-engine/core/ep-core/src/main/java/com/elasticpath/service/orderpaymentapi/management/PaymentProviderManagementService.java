/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi.management;

import java.util.Map;

import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;

/**
 * Service to interact with payment provider workflow.
 */
public interface PaymentProviderManagementService {

	/**
	 * Find all {@link PaymentProviderPluginDTO}s mapped to their providerPluginId.
	 * @return all payment provider plugins.
	 */
	Map<String, PaymentProviderPluginDTO> findAll();

}
