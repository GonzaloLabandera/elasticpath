/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.accounts.ChildAccountsIdentifier;
import com.elasticpath.rest.definition.accounts.ChildAccountsResource;
import com.elasticpath.rest.definition.accounts.PaginatedChildAccountsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;

/**
 * Read Child Accounts Prototype.
 */
public class ReadChildAccountsPrototype implements ChildAccountsResource.Read {

	private static final Integer FIRST_PAGE = 1;
	private final ChildAccountsIdentifier identifier;

	/**
	 * Constructor.
	 *
	 * @param identifier child accounts identifier
	 */
	@Inject
	public ReadChildAccountsPrototype(@RequestIdentifier final ChildAccountsIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Single<PaginatedChildAccountsIdentifier> onRead() {
		return Single.just(PaginatedChildAccountsIdentifier.builder()
				.withChildAccounts(identifier)
				.withPageId(IntegerIdentifier.of(FIRST_PAGE))
				.build());
	}
}
