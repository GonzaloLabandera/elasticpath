/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorChoiceResource;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.selector.Choice;

/**
 * When the Account Billing Address Selector Choice is selected, return data about the Account Billing Address.
 * Implements {@link AccountBillingAddressSelectorChoiceResource.Read}.
 */
public class AccountBillingAddressSelectorChoiceReadPrototype implements AccountBillingAddressSelectorChoiceResource.Read {

	private final AccountBillingAddressSelectorChoiceIdentifier accountBillingAddressSelectorChoiceIdentifier;
	private final SelectorRepository<AccountBillingAddressSelectorIdentifier, AccountBillingAddressSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Constructor.
	 *
	 * @param selectorRepository                            the selector Repository
	 * @param accountBillingAddressSelectorChoiceIdentifier Billing-Address Selector Choice Identifier
	 */
	@Inject
	public AccountBillingAddressSelectorChoiceReadPrototype(
			@ResourceRepository final SelectorRepository<AccountBillingAddressSelectorIdentifier,
					AccountBillingAddressSelectorChoiceIdentifier> selectorRepository,
			@RequestIdentifier final AccountBillingAddressSelectorChoiceIdentifier accountBillingAddressSelectorChoiceIdentifier) {
		this.accountBillingAddressSelectorChoiceIdentifier = accountBillingAddressSelectorChoiceIdentifier;
		this.selectorRepository = selectorRepository;
	}

	@Override
	public Single<Choice> onRead() {
		return selectorRepository.getChoice(accountBillingAddressSelectorChoiceIdentifier);
	}
}
