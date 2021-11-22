/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *  
 */
package com.elasticpath.service.customer.impl;

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.collect.Iterables;
import org.apache.openjpa.enhance.PersistenceCapable;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.service.customer.AddressService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.validation.ConstraintViolationTransformer;

/**
 * The default implementation of <code>AddressService</code>.
 */
public class AddressServiceImpl extends AbstractEpPersistenceServiceImpl implements AddressService {
	private Validator validator;
	private ConstraintViolationTransformer constraintViolationTransformer;

	@Override
	public List<CustomerAddress> findByCustomer(final long customerUid) {
		return getPersistenceEngine().retrieveByNamedQuery("ADDRESSES_BY_CUSTOMER_UID", customerUid);
	}

	@Override
	public List<CustomerAddress> findByCustomerCountryAndSubCountry(final long customerUId, final String country, final String subCountry) {
		return getPersistenceEngine()
				.retrieveByNamedQuery("ADDRESSES_BY_CUSTOMER_UID_COUNTRY_AND_SUB_COUNTRY", customerUId, country, subCountry);
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return null;
	}

	@Override
	public CustomerAddress findByAddress(final long customerUid, final CustomerAddress addressToMatch) {
		return getPersistenceEngine().<CustomerAddress>retrieveByNamedQuery("ADDRESSES_BY_CUSTOMER_UID", customerUid).stream()
				.filter(address -> address.equals(addressToMatch))
				.findFirst().orElse(null);
	}

	@Override
	public CustomerAddress findByCustomerAndAddressGuid(final long customerUid, final String addressGuid) {
		return Iterables
				.getFirst(
						getPersistenceEngine().retrieveByNamedQuery("ADDRESS_BY_CUSTOMER_UID_AND_ADDRESS_GUID", customerUid, addressGuid),
						null);
	}

	@Override
	public CustomerAddress findByCustomerAndAddressUid(final long customerUid, final long addressUid) {
		return Iterables
				.getFirst(
						getPersistenceEngine().retrieveByNamedQuery("ADDRESS_BY_CUSTOMER_UID_AND_ADDRESS_UID", customerUid, addressUid),
						null);
	}

	@Override
	public Customer removeAllByCustomer(final Customer customer) {
		customer.setPreferredShippingAddress(null);
		customer.setPreferredBillingAddress(null);
		customer.getTransientAddresses().clear();

		getPersistenceEngine().executeNamedQuery("REMOVE_ALL_CUSTOMER_ADDRESSES", customer.getUidPk());
		return customer;
	}

	@Override
	public Customer remove(final Customer customer, final CustomerAddress address) {
		customer.removeAddress(address);

		getPersistenceEngine().executeNamedQuery("REMOVE_ADDRESS_BY_UID", address.getUidPk());
		getPersistenceEngine().update(customer);

		return customer;
	}

	@Override
	public void save(final CustomerAddress... addresses) {
		for (CustomerAddress address : addresses) {
			if (shouldPersist(address)) {
				validateCustomerAddress(address);
				getPersistenceEngine().saveOrUpdate(address);
			}
		}
	}

	private boolean shouldPersist(final CustomerAddress address) {
		return !address.isPersisted() || ((PersistenceCapable) address).pcIsDirty();
	}

	/**
	 * Validates the CustomerAddress data.
	 *
	 * @param address the customer address.
	 */
	protected void validateCustomerAddress(final CustomerAddress address) {
		Set<ConstraintViolation<CustomerAddress>> addressViolations = validator.validate(address);
		if (!addressViolations.isEmpty()) {
			List<StructuredErrorMessage> structuredErrorMessageList = constraintViolationTransformer.transform(addressViolations);
			throw new EpValidationException("Address validation failure.", structuredErrorMessageList);
		}
	}

	public void setValidator(final Validator validator) {
		this.validator = validator;
	}

	protected Validator getValidator() {
		return validator;
	}

	public void setConstraintViolationTransformer(final ConstraintViolationTransformer constraintViolationTransformer) {
		this.constraintViolationTransformer = constraintViolationTransformer;
	}
}
