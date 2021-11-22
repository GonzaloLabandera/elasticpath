/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerIdentifierStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Implementation of CustomerIdentifierStrategy which uses AttributeValueKey.
 */
@Component(property = "customerIdentificationKeyField=ATTRIBUTE_VALUE")
public class CustomerIdentifierByAttributeValueKeyStrategyImpl implements CustomerIdentifierStrategy {

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
	public ExecutionResult<Boolean> isCustomerExists(final String userId, final CustomerType customerType, final String customerIdentifierKey) {
		String keyFieldString = getKeyFieldStringFromCustomerIdentifierKey(customerIdentifierKey);

		ExecutionResult<Long> customerExecutionResult =
				customerRepository.getCustomerCountByProfileAttributeKeyAndValue(keyFieldString, userId);
		if (customerExecutionResult.isSuccessful()) {
			if (customerExecutionResult.getData().equals(1L)) {
				return ExecutionResultFactory.createReadOK(Boolean.TRUE);
			} else if (customerExecutionResult.getData() > 1L) {
				return ExecutionResultFactory.createBadRequestBody(prepareMultipleCustomersFoundError(keyFieldString, userId));
			}
		}
		return ExecutionResultFactory.createBadRequestBody(prepareNoCustomerFoundError(keyFieldString, userId));
	}

	@Override
	public ExecutionResult<String> deriveCustomerGuid(final String userId, final CustomerType customerType, final String customerIdentifierKey) {
		String keyFieldString = getKeyFieldStringFromCustomerIdentifierKey(customerIdentifierKey);
		ExecutionResult<String> executionResult =
				customerRepository.findCustomerGuidByProfileAttributeKeyAndValue(keyFieldString, userId);
		if (executionResult.isFailure()) {
			return ExecutionResultFactory.createBadRequestBody(prepareNoCustomerFoundError(keyFieldString, userId));
		}
		return executionResult;
	}

	/**
	 * Get attribute key from customer identifier key string.
	 *
	 * @param customerIdentifierKey customer identifier key string.
	 *
 	 * @return attribute key
	 */
	public static String getKeyFieldStringFromCustomerIdentifierKey(final String customerIdentifierKey) {
		return customerIdentifierKey.split(":")[1];
	}

	/**
	 * Prepare error message for no customer found.
	 *
	 * @param keyFieldString attribute key
	 * @param userId attribute value
	 *
	 * @return error message
	 */
	public static String prepareNoCustomerFoundError(final String keyFieldString, final String userId) {
		return String.format("No customer with %s set to %s found.", keyFieldString, userId);
	}

	/**
	 * Prepare error message for multiple customers found.
	 *
	 * @param keyFieldString attribute key
	 * @param userId attribute value
	 * @return error message
	 */
	public static String prepareMultipleCustomersFoundError(final String keyFieldString, final String userId) {
		return String.format("Multiple customers with %s set to %s were found.", keyFieldString, userId);
	}

	@Override
	public String getCustomerIdentificationKeyField() {
		return customerIdentificationKeyField;
	}

	@Override
	public ExecutionResult<String> deriveUserIdFromCustomer(final Customer customer) {
		return ExecutionResultFactory.createNotFound();
	}
}
