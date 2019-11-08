/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement the handling of the selector for the billing address.
 * Implements {@link BillingAddressSelectorResource.Choices}.
 */
public class BillingAddressSelectorChoicesPrototype implements BillingAddressSelectorResource.Choices {

	private final BillingAddressSelectorIdentifier billingAddressSelectorIdentifier;
	private final SelectorRepository<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository the Billing-Address Selector repository
	 * @param billingAddressSelectorIdentifier The Billing-Address Choices Identifier.
	 */
	@Inject
	public BillingAddressSelectorChoicesPrototype(
			@ResourceRepository final SelectorRepository<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final BillingAddressSelectorIdentifier billingAddressSelectorIdentifier) {
		this.billingAddressSelectorIdentifier = billingAddressSelectorIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return selectorRepository.getChoices(billingAddressSelectorIdentifier);
	}
}
