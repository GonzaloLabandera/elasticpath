/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Collection;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerIdentifierService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerIdentifierStrategy;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Implementation of CustomerIdentifierStrategyUtil.
 */
@Component
public class CustomerIdentifierServiceImpl implements CustomerIdentifierService {

	/**
	 * Setting column value for customer identifier key.
	 */
	public static final String CUSTOMER_IDENTIFIER_FIELD_SETTING_PATH = "COMMERCE/SYSTEM/CUSTOMER/identifier";

	@Reference
	private SettingsReader settingsReader;


	@Reference(
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY,
			fieldOption = FieldOption.UPDATE)
	private Collection<CustomerIdentifierStrategy> customerIdentifierStrategyList;


	@Override
	public ExecutionResult<Void> isCustomerExists(final String userId, final String storeCode, final String issuer) {
		ExecutionResult<CustomerIdentifierStrategy> customerIdentifierStrategy = getCustomerIdentifierStrategy(issuer);
		if (customerIdentifierStrategy.isFailure()) {
			return ExecutionResultFactory.createNotFound();
		}

		String customerIdentifierKey = getCustomerIdentifierKey(issuer);
		return customerIdentifierStrategy.getData().isCustomerExists(userId, storeCode, customerIdentifierKey);
	}

	@Override
	public ExecutionResult<String> deriveCustomerGuid(final String userId, final String storeCode, final String issuer) {
		ExecutionResult<CustomerIdentifierStrategy> customerIdentifierStrategy = getCustomerIdentifierStrategy(issuer);
		if (customerIdentifierStrategy.isFailure()) {
			return ExecutionResultFactory.createNotFound();
		}

		String customerIdentifierKey = getCustomerIdentifierKey(issuer);
		ExecutionResult<String> customerExecutionResult =
				customerIdentifierStrategy.getData().deriveCustomerGuid(userId, storeCode, customerIdentifierKey);

		if (customerExecutionResult.isSuccessful()) {
			return ExecutionResultFactory.createReadOK(customerExecutionResult.getData());
		}

		return ExecutionResultFactory.createNotFound();
	}

	/**
	 * Derives the customer identifier strategy for given issuer info.
	 *
	 * @param issuer issuer
	 *
	 * @return strategy
	 */
	protected ExecutionResult<CustomerIdentifierStrategy> getCustomerIdentifierStrategy(final String issuer) {
		String customerIdentifierKeyText = getCustomerIdentifierKey(issuer);

		// Consider only key part of identifierKey if its of format Key:Value.
		String customerIdentifierKey =
				customerIdentifierKeyText.contains(":") ? customerIdentifierKeyText.split(":")[0] : customerIdentifierKeyText;

		Optional<CustomerIdentifierStrategy> identifierStrategyOptional =  customerIdentifierStrategyList.stream()
				.filter(identifierStrategy ->
						identifierStrategy.getCustomerIdentificationKeyField().equalsIgnoreCase(customerIdentifierKey)).findFirst();

		return identifierStrategyOptional.map(ExecutionResultFactory::createReadOK)
				.orElseGet(ExecutionResultFactory::createNotFound);
	}

	/**
	 * Derives the customer identifier key for given issuer using corresponding setting value.
	 *
	 * @param issuer issuer
	 *
	 * @return customerIdentifierKey
	 */
	protected String getCustomerIdentifierKey(final String issuer) {
		SettingValue customerIdentifierKeyField = settingsReader.getSettingValue(CUSTOMER_IDENTIFIER_FIELD_SETTING_PATH, issuer);
		return customerIdentifierKeyField.getValue();
	}
}
