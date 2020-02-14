/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config;

import io.reactivex.Single;

import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;

/**
 * Repository for operations with payment provider configurations.
 */
public interface PaymentProviderConfigManagementRepository {

	/**
	 * Find {@link PaymentProviderConfigDTO} by guid.
	 *
	 * @param guid given guid.
	 * @return instance of {@link PaymentProviderConfigDTO}
	 */
	Single<PaymentProviderConfigDTO> findByGuid(String guid);
}
