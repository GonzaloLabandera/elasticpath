/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerIdentifierStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Implementation of CustomerIdentifierStrategy which uses guid.
 */
@Component(property = "customerIdentificationKeyField=guid")
public class CustomerIdentifierByGuidStrategyImpl implements CustomerIdentifierStrategy {

	@Reference
	private CustomerRepository customerRepository;

	private String customerIdentificationKeyField;

	/**
	 * Called by OSGi Declarative Services on activation.
	 *
	 * @param properties properties specified on the component annotation.
	 */
	@Activate
	public void activate(final Map<String, String> properties) {
		customerIdentificationKeyField = properties.get("customerIdentificationKeyField");
	}

	@Override
	public ExecutionResult<Void> isCustomerExists(final String userId, final String storeCode, final String customerIdentifierKey) {
		return customerRepository.isCustomerGuidExists(userId);
	}

	@Override
	public ExecutionResult<String> deriveCustomerGuid(final String userId, final String storeCode, final String customerIdentifierKey) {
		ExecutionResult<Void> executionResult = customerRepository.isCustomerGuidExists(userId);
		if (executionResult.isFailure()) {
			return ExecutionResultFactory.createNotFound();
		}
		return ExecutionResultFactory.createReadOK(userId);
	}

	@Override
	public String getCustomerIdentificationKeyField() {
		return customerIdentificationKeyField;
	}
}
