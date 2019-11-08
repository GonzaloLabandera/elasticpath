/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorChoiceResource;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.Choice;

/**
 * When the Shipping Address Selector Choice is selected, return data about the Shipping-Address.
 * Implements {@link ShippingAddressSelectorChoiceResource.Read}.
 */
public class ShippingAddressSelectorChoiceReadPrototype implements ShippingAddressSelectorChoiceResource.Read {

	private final ShippingAddressSelectorChoiceIdentifier shippingAddressSelectorChoiceIdentifier;
	private final SelectorRepository<ShippingAddressSelectorIdentifier, ShippingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository              the Address Repository
	 * @param shippingAddressSelectorChoiceIdentifier Shipping-Address Selector Choice Identifier
	 */
	@Inject
	public ShippingAddressSelectorChoiceReadPrototype(
			@ResourceRepository final SelectorRepository<ShippingAddressSelectorIdentifier,
					ShippingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final ShippingAddressSelectorChoiceIdentifier shippingAddressSelectorChoiceIdentifier) {
		this.shippingAddressSelectorChoiceIdentifier = shippingAddressSelectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<Choice> onRead() {
		return selectorRepository.getChoice(shippingAddressSelectorChoiceIdentifier);
	}

}
