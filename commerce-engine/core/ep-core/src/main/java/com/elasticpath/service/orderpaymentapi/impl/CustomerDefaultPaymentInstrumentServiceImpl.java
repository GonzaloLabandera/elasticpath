/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.orderpaymentapi.impl;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerDefaultPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.impl.CustomerDefaultPaymentInstrumentImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.orderpaymentapi.CustomerDefaultPaymentInstrumentService;

/**
 * Default implementation of {@link CustomerDefaultPaymentInstrumentService}.
 */
public class CustomerDefaultPaymentInstrumentServiceImpl implements CustomerDefaultPaymentInstrumentService {

	private PersistenceEngine persistenceEngine;

	@Override
	public void saveAsDefault(final CustomerPaymentInstrument customerPaymentInstrument) {
		sanityCheck();

		CustomerDefaultPaymentInstrument customerDefaultPaymentInstrument = (CustomerDefaultPaymentInstrument) getPersistenceEngine()
                .retrieveByNamedQuery("FIND_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT_BY_CUSTOMER", customerPaymentInstrument.getCustomerUid())
				.stream()
				.findFirst()
				.orElse(null);

        if (customerDefaultPaymentInstrument == null) {
            customerDefaultPaymentInstrument = new CustomerDefaultPaymentInstrumentImpl(customerPaymentInstrument);
        } else {
            customerDefaultPaymentInstrument.setCustomerPaymentInstrument(customerPaymentInstrument);
        }
        getPersistenceEngine().saveOrUpdate(customerDefaultPaymentInstrument);
    }

    @Override
    public CustomerPaymentInstrument getDefaultForCustomer(final Customer customer) {
        return getDefaultForCustomerUid(customer.getUidPk());
    }

    private CustomerPaymentInstrument getDefaultForCustomerUid(final long customerUid) {
        sanityCheck();

        return getPersistenceEngine().retrieveByNamedQuery(
                "FIND_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT_BY_CUSTOMER", customerUid)
                .stream()
                .map(association -> ((CustomerDefaultPaymentInstrument) association).getCustomerPaymentInstrument())
                .findFirst()
                .orElse(null);
    }

    @Override
	public boolean hasDefaultPaymentInstrument(final Customer customer) {
		return getDefaultForCustomer(customer) != null;
	}

	@Override
	public boolean isDefault(final CustomerPaymentInstrument customerPaymentInstrument) {
        if (customerPaymentInstrument == null) {
            return false;
        }
        final CustomerPaymentInstrument defaultCustomerPaymentInstrument = getDefaultForCustomerUid(customerPaymentInstrument.getCustomerUid());
        return customerPaymentInstrument.equals(defaultCustomerPaymentInstrument);
    }

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * Get persistence engine.
	 *
	 * @return persistence engine
	 */
	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	/**
	 * Sanity check of this service instance.
	 *
	 * @throws EpServiceException - if something goes wrong.
	 */
	protected void sanityCheck() throws EpServiceException {
		if (getPersistenceEngine() == null) {
			throw new EpServiceException("The persistence engine is not correctly initialized.");
		}
	}

}