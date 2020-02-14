/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow;

import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginsDTO;

/**
 * Payment provider workflow facade for Order Payment API (ep-core) interaction.
 */
public interface PaymentProviderWorkflow {

	/**
	 * Find all {@link PaymentProviderPluginsDTO}s.
	 * @return all payment provider plugins.
	 */
	PaymentProviderPluginsDTO findAll();
}