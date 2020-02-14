/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi.impl;

import java.util.Collection;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;

/**
 * Default implementation of {@link CartOrderPaymentInstrumentService}.
 */
public class CartOrderPaymentInstrumentServiceImpl extends AbstractEpPersistenceServiceImpl implements CartOrderPaymentInstrumentService {

	@Override
	public CartOrderPaymentInstrument saveOrUpdate(final CartOrderPaymentInstrument cartOrderPaymentInstrument) {
		sanityCheck();

		return getPersistenceEngine().saveOrUpdate(cartOrderPaymentInstrument);
	}

	@Override
	public void remove(final CartOrderPaymentInstrument cartOrderPaymentInstrument) {
		sanityCheck();

		getPersistenceEngine().delete(cartOrderPaymentInstrument);
	}

	@Override
	public CartOrderPaymentInstrument findByGuid(final String guid) {
		sanityCheck();

		return (CartOrderPaymentInstrument) getPersistenceEngine().retrieveByNamedQuery("FIND_CART_ORDER_PAYMENT_INSTRUMENT_BY_GUID", guid)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public Collection<CartOrderPaymentInstrument> findByCartOrder(final CartOrder cartOrder) {
		sanityCheck();

		return getPersistenceEngine()
				.retrieveByNamedQuery("FIND_CART_ORDER_PAYMENT_INSTRUMENTS_BY_CART_ORDER_ALL", cartOrder.getUidPk());
	}

	@Override
	public boolean hasPaymentInstruments(final CartOrder cartOrder) {
		return !findByCartOrder(cartOrder).isEmpty();
	}

	@Override
	public Object getObject(final long uid) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQuery("FIND_CART_ORDER_PAYMENT_INSTRUMENT_BY_UID", uid).stream()
				.findFirst()
				.orElse(null);
	}
}
