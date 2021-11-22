/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi.impl;

import java.util.List;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;

/**
 * Default implementation for {@link CustomerPaymentInstrumentService}.
 */
public class CustomerPaymentInstrumentServiceImpl extends AbstractEpPersistenceServiceImpl implements CustomerPaymentInstrumentService {

	@Override
	public CustomerPaymentInstrument saveOrUpdate(final CustomerPaymentInstrument customerPaymentInstrument) {
		sanityCheck();

		return getPersistenceEngine().saveOrUpdate(customerPaymentInstrument);
	}

	@Override
	public void remove(final CustomerPaymentInstrument customerPaymentInstrument) {
		sanityCheck();

		getPersistenceEngine().executeNamedQuery(
				"DELETE_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT_BY_CUSTOMER_PAYMENT_INSTRUMENT",
				customerPaymentInstrument.getUidPk());
		getPersistenceEngine().delete(customerPaymentInstrument);
		getPersistenceEngine().flush();
	}

	@Override
	public CustomerPaymentInstrument findByGuid(final String guid) {
		sanityCheck();

		return (CustomerPaymentInstrument) getPersistenceEngine().retrieveByNamedQuery("FIND_CUSTOMER_PAYMENT_INSTRUMENT_BY_GUID", guid)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<CustomerPaymentInstrument> findByCustomer(final Customer customer) {
		sanityCheck();

		return getPersistenceEngine()
				.retrieveByNamedQuery("FIND_CUSTOMER_PAYMENT_INSTRUMENTS_BY_CUSTOMER_ALL", customer.getUidPk());
	}

	@Override
	public Object getObject(final long uid) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQuery("FIND_CUSTOMER_PAYMENT_INSTRUMENT_BY_UID", uid).stream()
				.findFirst()
				.orElse(null);
	}

}
