/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.AliasRepository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountDefaultBillingAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountDefaultBillingAddressResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account Default Billing Address prototype for Read operation.
 */
public class AccountReadDefaultBillingAddressPrototype implements AccountDefaultBillingAddressResource.Read {

	private final AccountDefaultBillingAddressIdentifier accountDefaultBillingAddressIdentifier;

	private final AliasRepository<AccountDefaultBillingAddressIdentifier, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountDefaultBillingAddressIdentifier defaultBillingAddressIdentifier
	 * @param repository                             repository
	 */
	@Inject
	public AccountReadDefaultBillingAddressPrototype(@RequestIdentifier final AccountDefaultBillingAddressIdentifier
																 accountDefaultBillingAddressIdentifier,
													 @ResourceRepository final AliasRepository<AccountDefaultBillingAddressIdentifier,
															 AccountAddressIdentifier> repository) {
		this.accountDefaultBillingAddressIdentifier = accountDefaultBillingAddressIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<AccountAddressIdentifier> onRead() {
		return repository.resolve(accountDefaultBillingAddressIdentifier);
	}
}
