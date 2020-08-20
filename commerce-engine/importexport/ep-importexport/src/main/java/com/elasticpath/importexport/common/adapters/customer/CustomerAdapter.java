/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.importexport.common.adapters.customer;

import java.util.Set;
import javax.validation.ConstraintViolation;

import org.apache.commons.lang.StringUtils;

import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.importexport.common.adapters.DtoAssemblerDelegatingAdapter;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.validation.service.ConstraintViolationsSummariser;
import com.elasticpath.validation.service.CustomerConstraintValidationService;

/**
 * Adapter for converting between {@link com.elasticpath.domain.customer.Customer} and 
 * {@link com.elasticpath.common.dto.customer.CustomerDTO} objects for importexport. 
 */
public class CustomerAdapter extends DtoAssemblerDelegatingAdapter<Customer, CustomerDTO> {

	private ConstraintViolationsSummariser summariser;
	private CustomerConstraintValidationService customerConstraintValidationService;

	@Override
	public Customer buildDomain(final CustomerDTO source, final Customer target) {
		final Customer customer = super.buildDomain(source, target);

		// Username, password, and salt are only allowed to be specified if the CUSTOMER_TYPE = REGISTERED_CUSTOMER.
		if (CustomerType.REGISTERED_USER != customer.getCustomerType()
				&& (StringUtils.isNotEmpty(customer.getUsername())
				|| StringUtils.isNotEmpty(customer.getPassword())
				|| StringUtils.isNotEmpty(getCustomerAuthenticationSalt(customer)))
		) {
			throw new PopulationRollbackException("IE-30854", customer.getSharedId());
		}

		Set<ConstraintViolation<Customer>> violations = customerConstraintValidationService.validate(customer);

		if (violations.isEmpty()) {
			return customer;
		}
		throw new PopulationRollbackException("IE-30851", customer.getSharedId(), getSummariser().summarise(violations));
	}

	private String getCustomerAuthenticationSalt(final Customer customer) {
		return customer.getCustomerAuthentication() == null ? null : customer.getCustomerAuthentication().getSalt();
	}

	public void setCustomerConstraintValidationService(final CustomerConstraintValidationService customerConstraintValidationService) {
		this.customerConstraintValidationService = customerConstraintValidationService;
	}

	public void setSummariser(final ConstraintViolationsSummariser summariser) {
		this.summariser = summariser;
	}

	protected ConstraintViolationsSummariser getSummariser() {
		return summariser;
	}
	
}