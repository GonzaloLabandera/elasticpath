/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.shipping.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement the handling of the selector for the shipping address.
 * Implements {@link AccountShippingAddressSelectorResource.Choices}.
 */
public class AccountShippingAddressSelectorChoicesPrototype implements AccountShippingAddressSelectorResource.Choices {

	private final AccountShippingAddressSelectorIdentifier accountShippingAddressSelectorIdentifier;
	private final SelectorRepository<AccountShippingAddressSelectorIdentifier, AccountShippingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository                       the Account-Shipping-Address Selector repository
	 * @param accountShippingAddressSelectorIdentifier The Account-Shipping-Address Choices Identifier.
	 */
	@Inject
	public AccountShippingAddressSelectorChoicesPrototype(
			@ResourceRepository final SelectorRepository<AccountShippingAddressSelectorIdentifier,
					AccountShippingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final AccountShippingAddressSelectorIdentifier accountShippingAddressSelectorIdentifier) {
		this.accountShippingAddressSelectorIdentifier = accountShippingAddressSelectorIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return selectorRepository.getChoices(accountShippingAddressSelectorIdentifier);
	}

}
