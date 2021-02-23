/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.prototypes;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressResource;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account address prototype for Update operation.
 */
public class UpdateAccountAddressPrototype implements AccountAddressResource.Update {

	private final AddressEntity addressEntityForm;

	private final AccountAddressIdentifier accountAddressIdentifier;

	private final Repository<AddressEntity, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param addressEntityForm        addressEntityForm
	 * @param accountAddressIdentifier accountAddressIdentifier
	 * @param repository               repository
	 */
	@Inject
	public UpdateAccountAddressPrototype(@RequestForm final AddressEntity addressEntityForm,
										 @RequestIdentifier final AccountAddressIdentifier accountAddressIdentifier,
										 @ResourceRepository final Repository<AddressEntity, AccountAddressIdentifier> repository) {
		this.addressEntityForm = addressEntityForm;
		this.accountAddressIdentifier = accountAddressIdentifier;
		this.repository = repository;
	}

	@Override
	public Completable onUpdate() {
		return repository.update(addressEntityForm, accountAddressIdentifier);
	}
}
