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
import com.elasticpath.rest.selector.SelectResult;

/**
 * Address when the user chooses an item.
 * Implements {@link BillingAddressSelectorChoiceResource.SelectWithResult}.
 */
public class BillingAddressSelectorChoiceSelectPrototype implements BillingAddressSelectorChoiceResource.SelectWithResult {

	private final BillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier;

	private final SelectorRepository<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository           the selector repository
	 * @param billingAddressSelectorChoiceIdentifier Billing-Address Selector Choice Identifier
	 */
	@Inject
	public BillingAddressSelectorChoiceSelectPrototype(
			@ResourceRepository final SelectorRepository<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final BillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier) {

		this.billingAddressSelectorChoiceIdentifier = billingAddressSelectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<SelectResult<BillingAddressSelectorIdentifier>> onSelectWithResult() {
		return selectorRepository.selectChoice(billingAddressSelectorChoiceIdentifier);
	}

}
