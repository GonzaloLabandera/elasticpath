/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.billing.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.repository.AliasRepository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountDefaultBillingAddressIdentifier;
import com.elasticpath.rest.definition.addresses.DefaultAccountBillingAddressFromAccountBillingAddressesRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account billing addresses to default billing address link.
 */
public class AccountDefaultBillingAddressFromAccountBillingAddressesRelationshipImpl
		implements DefaultAccountBillingAddressFromAccountBillingAddressesRelationship.LinkTo {

	private static final Logger LOG = LoggerFactory.getLogger(AccountDefaultBillingAddressFromAccountBillingAddressesRelationshipImpl.class);
	private final AccountBillingAddressesIdentifier accountBillingAddressesIdentifier;
	private final AliasRepository<AccountDefaultBillingAddressIdentifier, AccountAddressIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountBillingAddressesIdentifier accountBillingAddressesIdentifier
	 * @param repository                        repository
	 */
	@Inject
	public AccountDefaultBillingAddressFromAccountBillingAddressesRelationshipImpl(
			@RequestIdentifier final AccountBillingAddressesIdentifier accountBillingAddressesIdentifier,
			@ResourceRepository final AliasRepository<AccountDefaultBillingAddressIdentifier, AccountAddressIdentifier> repository) {
		this.accountBillingAddressesIdentifier = accountBillingAddressesIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountDefaultBillingAddressIdentifier> onLinkTo() {
		AccountDefaultBillingAddressIdentifier accountDefaultBillingAddressIdentifier = AccountDefaultBillingAddressIdentifier.builder()
				.withAccountBillingAddresses(accountBillingAddressesIdentifier)
				.build();

		return repository.resolve(accountDefaultBillingAddressIdentifier)
				.toObservable()
				.map(addressIdentifier -> accountDefaultBillingAddressIdentifier)
				.doOnError(throwable -> LOG.info("Could not find a default billing address."))
				.onErrorResumeNext(Observable.empty());
	}
}
