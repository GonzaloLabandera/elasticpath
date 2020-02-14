/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.prototypes.order;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorResource;
import com.elasticpath.rest.selector.SelectorEntity;

/**
 * READ operation on the {@link OrderPaymentInstrumentSelectorResource} for configuring selector.
 */
public class ReadOrderPaymentInstrumentSelectorResourcePrototype implements OrderPaymentInstrumentSelectorResource.Select {

	private static final String SELECTOR_NAME = "order-payment-instrument-selector";
	private static final String SELECTION_RULE = "1";

	@Override
	public Single<SelectorEntity> onRead() {
		return Single.just(SelectorEntity.builder()
				.withName(SELECTOR_NAME)
				.withSelectionRule(SELECTION_RULE)
				.build());
	}

}
