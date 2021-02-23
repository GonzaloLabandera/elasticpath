/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesResource;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Account Billing Addresses prototype for Read operation.
 */
public class AccountReadBillingAddressesPrototype implements AccountBillingAddressesResource.Read {

	private final IdentifierPart<String> scope;

	private final Repository<AddressEntity, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param scope      scope
	 * @param repository repository
	 */
	@Inject
	public AccountReadBillingAddressesPrototype(@UriPart(AddressesIdentifier.SCOPE) final IdentifierPart<String> scope,
												@ResourceRepository final Repository<AddressEntity, AccountAddressIdentifier> repository) {
		this.scope = scope;
		this.repository = repository;
	}

	@Override
	public Observable<AccountAddressIdentifier> onRead() {
		return repository.findAll(scope);
	}
}
