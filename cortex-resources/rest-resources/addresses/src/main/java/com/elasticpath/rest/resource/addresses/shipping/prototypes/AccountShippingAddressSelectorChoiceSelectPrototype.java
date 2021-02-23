/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorChoiceResource;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectResult;

/**
 * Address when the user chooses an item.
 * Implements {@link AccountShippingAddressSelectorChoiceResource.SelectWithResult}.
 */
public class AccountShippingAddressSelectorChoiceSelectPrototype implements AccountShippingAddressSelectorChoiceResource.SelectWithResult {

	private final AccountShippingAddressSelectorChoiceIdentifier accountShippingAddressSelectorChoiceIdentifier;

	private final SelectorRepository<AccountShippingAddressSelectorIdentifier, AccountShippingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository                             the selector repository
	 * @param accountShippingAddressSelectorChoiceIdentifier Account Shipping Address Selector Choice Identifier
	 */
	@Inject
	public AccountShippingAddressSelectorChoiceSelectPrototype(
			@ResourceRepository final SelectorRepository<AccountShippingAddressSelectorIdentifier,
					AccountShippingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final AccountShippingAddressSelectorChoiceIdentifier accountShippingAddressSelectorChoiceIdentifier) {
		this.accountShippingAddressSelectorChoiceIdentifier = accountShippingAddressSelectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<SelectResult<AccountShippingAddressSelectorIdentifier>> onSelectWithResult() {
		return selectorRepository.selectChoice(accountShippingAddressSelectorChoiceIdentifier);
	}
}
