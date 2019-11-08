/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorChoiceResource;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.Choice;

/**
 * When the Billing Address Selector Choice is selected, return data about the Billing-Address.
 * Implements {@link BillingAddressSelectorChoiceResource.Read}.
 */
public class BillingAddressSelectorChoiceReadPrototype implements BillingAddressSelectorChoiceResource.Read {

	private final BillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier;
	private final SelectorRepository<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository              the selector Repository
	 * @param billingAddressSelectorChoiceIdentifier Billing-Address Selector Choice Identifier
	 */
	@Inject
	public BillingAddressSelectorChoiceReadPrototype(
			@ResourceRepository final SelectorRepository<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final BillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier) {
		this.billingAddressSelectorChoiceIdentifier = billingAddressSelectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<Choice> onRead() {
		return selectorRepository.getChoice(billingAddressSelectorChoiceIdentifier);
	}
}
