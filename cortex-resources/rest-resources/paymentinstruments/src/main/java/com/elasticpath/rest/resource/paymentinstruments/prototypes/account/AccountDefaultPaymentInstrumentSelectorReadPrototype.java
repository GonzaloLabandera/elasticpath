/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.account;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Implement a Selector for the Account Default Payment Instrument.
 * Implements {@link AccountDefaultPaymentInstrumentSelectorResource.Select}.
 */
public class AccountDefaultPaymentInstrumentSelectorReadPrototype implements AccountDefaultPaymentInstrumentSelectorResource.Select {
	private static final String SELECTOR_NAME = "default-payment-instrument-selector";
	private static final String SELECTION_RULE = "1";

	@Override
	public Single<SelectorEntity> onRead() {
		return Single.just(
				SelectorEntity
						.builder()
						.withName(SELECTOR_NAME)
						.withSelectionRule(SELECTION_RULE)
						.build());
	}
}
