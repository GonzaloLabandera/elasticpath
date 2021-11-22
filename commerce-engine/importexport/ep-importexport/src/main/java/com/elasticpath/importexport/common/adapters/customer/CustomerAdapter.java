/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.importexport.common.adapters.customer;

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.importexport.common.adapters.DtoAssemblerDelegatingAdapter;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.validation.service.ConstraintViolationsSummariser;
import com.elasticpath.validation.service.CustomerConstraintValidationService;

/**
 * Adapter for converting between {@link Customer} and
 * {@link CustomerDTO} objects for importexport.
 */
public class CustomerAdapter extends DtoAssemblerDelegatingAdapter<Customer, CustomerDTO> {

	/** Preferred billing address type. */
	public static final int PREFERRED_BILLING_ADDRESS = 0;
	/** Preferred shipping address type. */
	public static final int PREFERRED_SHIPPING_ADDRESS = 1;
	/** Other address type. */
	public static final int OTHER_ADDRESS = 2;

	private ConstraintViolationsSummariser summariser;
	private CustomerConstraintValidationService customerConstraintValidationService;
	private final Multimap<String, Pair<Integer, CustomerAddress>> nonPersistedCustomerAddresses = HashMultimap.create();

	@Override
	public Customer buildDomain(final CustomerDTO source, final Customer target) {
		final Customer customer = super.buildDomain(source, target);

		//customer addresses are not maintained by OpenJPA due to perf issues;
		// they are separated here and validated and saved in CustomerImporter.postProcessingImportHandling
		separateAddresses(customer);

		validateSingleCustomer(customer);

		return customer;
	}

	/**
	 * Validate a list of customers.
	 *
	 * @param customers the list of customers.
	 */
	public void validateCustomers(final List<Customer> customers) {
		for (Customer customer : customers) {
			validateSingleCustomer(customer);
		}
	}

	private void validateSingleCustomer(final Customer customer) {
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
			return;
		}
		throw new PopulationRollbackException("IE-30851", customer.getSharedId(), getSummariser().summarise(violations));
	}

	private void separateAddresses(final Customer customer) {
		if (!customer.getTransientAddresses().isEmpty()) {
			CustomerAddress preferredBillingAddress = customer.getPreferredBillingAddress();
			if (preferredBillingAddress != null) {
				nonPersistedCustomerAddresses.put(customer.getGuid(), Pair.of(PREFERRED_BILLING_ADDRESS, preferredBillingAddress));
				customer.getTransientAddresses().remove(preferredBillingAddress);
				customer.setPreferredBillingAddress(null);
			}
			CustomerAddress preferredShippingAddress = customer.getPreferredShippingAddress();
			if (preferredShippingAddress != null) {
				nonPersistedCustomerAddresses.put(customer.getGuid(), Pair.of(PREFERRED_SHIPPING_ADDRESS, preferredShippingAddress));
				customer.getTransientAddresses().remove(preferredShippingAddress);
				customer.setPreferredShippingAddress(null);
			}

			customer.getTransientAddresses().forEach(otherAddress ->
				nonPersistedCustomerAddresses.put(customer.getGuid(), Pair.of(OTHER_ADDRESS, otherAddress))
			);
			customer.getTransientAddresses().clear();
		}
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

	public Multimap<String, Pair<Integer, CustomerAddress>> getNonPersistedCustomerAddresses() {
		return nonPersistedCustomerAddresses;
	}

}