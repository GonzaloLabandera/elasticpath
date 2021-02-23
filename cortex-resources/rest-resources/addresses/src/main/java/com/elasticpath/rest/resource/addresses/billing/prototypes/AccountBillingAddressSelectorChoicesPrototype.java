/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement the handling of the selector for the account billing address.
 * Implements {@link AccountBillingAddressSelectorResource.Choices}.
 */
public class AccountBillingAddressSelectorChoicesPrototype implements AccountBillingAddressSelectorResource.Choices {

	private final AccountBillingAddressSelectorIdentifier accountBillingAddressSelectorIdentifier;
	private final SelectorRepository<AccountBillingAddressSelectorIdentifier, AccountBillingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository                      the Billing-Address Selector repository
	 * @param accountBillingAddressSelectorIdentifier The Billing-Address Choices Identifier
	 */
	@Inject
	public AccountBillingAddressSelectorChoicesPrototype(
			@ResourceRepository final SelectorRepository<AccountBillingAddressSelectorIdentifier,
					AccountBillingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final AccountBillingAddressSelectorIdentifier accountBillingAddressSelectorIdentifier) {
		this.accountBillingAddressSelectorIdentifier = accountBillingAddressSelectorIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Observable<SelectorChoice> onChoices() {
		return selectorRepository.getChoices(accountBillingAddressSelectorIdentifier);
	}
}
