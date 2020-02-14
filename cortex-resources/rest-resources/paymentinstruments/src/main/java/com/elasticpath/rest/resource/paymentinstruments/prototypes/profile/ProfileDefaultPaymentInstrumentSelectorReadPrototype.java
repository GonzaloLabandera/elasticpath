/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.profile;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * Implement a Selector for the Profile Default Payment Instrument.
 * Implements {@link ProfileDefaultPaymentInstrumentSelectorResource.Select}.
 */
public class ProfileDefaultPaymentInstrumentSelectorReadPrototype implements ProfileDefaultPaymentInstrumentSelectorResource.Select {
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
