/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorChoiceResource;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectResult;

/**
 * CREATE operation on the {@link OrderPaymentInstrumentSelectorChoiceResource} for selecting a choice and promoting it to be chosen.
 */
public class SelectOrderPaymentInstrumentSelectorChoicePrototype implements OrderPaymentInstrumentSelectorChoiceResource.SelectWithResult {

	private final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier;
	private final SelectorRepository<
			OrderPaymentInstrumentSelectorIdentifier,
			OrderPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorChoiceIdentifier selector choice identifier
	 * @param selectorRepository       selector repository
	 */
	@Inject
	public SelectOrderPaymentInstrumentSelectorChoicePrototype(
			@RequestIdentifier final OrderPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier,
			@ResourceRepository final SelectorRepository<
					OrderPaymentInstrumentSelectorIdentifier,
					OrderPaymentInstrumentSelectorChoiceIdentifier> selectorRepository) {
		this.selectorChoiceIdentifier = selectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<SelectResult<OrderPaymentInstrumentSelectorIdentifier>> onSelectWithResult() {
		return selectorRepository.selectChoice(selectorChoiceIdentifier);
	}

}
