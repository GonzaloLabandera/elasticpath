/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.addresses.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesResource;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;

/**
 * Account Addresses Prototype for Read operation.
 */
public class ReadAccountAddressesPrototype implements AccountAddressesResource.Read {

	private final IdentifierPart<String> scope;

	private final Repository<AddressEntity, AccountAddressIdentifier> repository;

	/*
	This unused field is required to access the accountEntityPurchaseRepository in AccountIdParameterStrategy.
	Note: Injecting an OSGi service in non-prototype classes (e.g. a PermissionParameterStrategy) with @ResourceRepository or @ResourceService
	will not work unless the services are already injected in a prototype class.  See "Data Injectors" in the cortex documentation.
*/
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final Repository<AccountEntity, AccountIdentifier> accountEntityRepository;

	@SuppressWarnings("PMD.UnusedPrivateField")
	private final AddressRepository addressRepository;

	/**
	 * Constructor.
	 *
	 * @param scope                   scope
	 * @param repository              repository
	 * @param accountEntityRepository the account entity purchase repository
	 * @param addressRepository       the address repository
	 */
	@Inject
	public ReadAccountAddressesPrototype(@UriPart(AddressesIdentifier.SCOPE) final IdentifierPart<String> scope,
										 @ResourceRepository final Repository<AddressEntity, AccountAddressIdentifier> repository,
										 @ResourceRepository final Repository<AccountEntity, AccountIdentifier> accountEntityRepository,
										 @ResourceService final AddressRepository addressRepository) {
		this.scope = scope;
		this.repository = repository;
		this.accountEntityRepository = accountEntityRepository;
		this.addressRepository = addressRepository;
	}

	@Override
	public Observable<AccountAddressIdentifier> onRead() {
		return repository.findAll(scope);
	}

}
