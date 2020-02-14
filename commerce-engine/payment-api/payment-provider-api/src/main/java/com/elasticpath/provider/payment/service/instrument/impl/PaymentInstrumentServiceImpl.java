/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instrument.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.impl.PaymentInstrumentImpl;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;

/**
 * Perform CRUD operations with {@link PaymentInstrument} entity.
 */
public class PaymentInstrumentServiceImpl implements PaymentInstrumentService {

	private final PersistenceEngine persistenceEngine;

	/**
	 * Constructor.
	 *
	 * @param persistenceEngine persistence engine
	 */
	@Autowired
	public PaymentInstrumentServiceImpl(final PersistenceEngine persistenceEngine) {
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

	@Override
	public PaymentInstrument get(final long uid) throws EpServiceException {
		return persistenceEngine.load(PaymentInstrumentImpl.class, uid);
	}

	@Override
	public PaymentInstrument findByGuid(final String guid) {
		return (PaymentInstrument) persistenceEngine.retrieveByNamedQuery("PAYMENT_INSTRUMENT_BY_GUID", guid)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public void remove(final PaymentInstrument paymentInstrument) {
		persistenceEngine.delete(paymentInstrument);
	}

	@Override
	public PaymentInstrument saveOrUpdate(final PaymentInstrument paymentInstrument) {
		return persistenceEngine.saveOrUpdate(paymentInstrument);
	}

}
