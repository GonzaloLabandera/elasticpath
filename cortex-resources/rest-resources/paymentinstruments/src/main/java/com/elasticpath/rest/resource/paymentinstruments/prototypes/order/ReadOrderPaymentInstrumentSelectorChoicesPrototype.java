/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.paymentinstruments.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * READ operation on the {@link OrderPaymentInstrumentSelectorResource} creating the choices links.
 */
public class ReadOrderPaymentInstrumentSelectorChoicesPrototype implements OrderPaymentInstrumentSelectorResource.Choices {

	private final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier;
	private final SelectorRepository<
			OrderPaymentInstrumentSelectorIdentifier,
			OrderPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorIdentifier selector identifier
	 * @param selectorRepository selector repository
	 */
	@Inject
	public ReadOrderPaymentInstrumentSelectorChoicesPrototype(
			@RequestIdentifier final OrderPaymentInstrumentSelectorIdentifier selectorIdentifier,
			@ResourceRepository final SelectorRepository<
					OrderPaymentInstrumentSelectorIdentifier,
					OrderPaymentInstrumentSelectorChoiceIdentifier> selectorRepository) {
		this.selectorIdentifier = selectorIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return selectorRepository.getChoices(selectorIdentifier);
	}

}
