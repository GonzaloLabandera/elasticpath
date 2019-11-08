/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement the handling of the selector for the shipping address.
 * Implements {@link ShippingAddressSelectorResource.Choices}.
 */
public class ShippingAddressSelectorChoicesPrototype implements ShippingAddressSelectorResource.Choices {

	private final ShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier;
	private final SelectorRepository<ShippingAddressSelectorIdentifier, ShippingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository the Shipping-Address Selector repository
	 * @param shippingAddressSelectorIdentifier The Shipping-Address Choices Identifier.
	 */
	@Inject
	public ShippingAddressSelectorChoicesPrototype(
			@ResourceRepository final SelectorRepository<ShippingAddressSelectorIdentifier,
					ShippingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final ShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier) {
		this.shippingAddressSelectorIdentifier = shippingAddressSelectorIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return selectorRepository.getChoices(shippingAddressSelectorIdentifier);
	}

}
