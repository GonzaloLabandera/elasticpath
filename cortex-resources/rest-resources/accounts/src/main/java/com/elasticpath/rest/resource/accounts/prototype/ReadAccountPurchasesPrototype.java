/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.accounts.AccountPurchasesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountPurchasesResource;
import com.elasticpath.rest.definition.accounts.PaginatedAccountPurchasesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;

/**
 * Account purchases prototype for Read operation.
 */
public class ReadAccountPurchasesPrototype implements AccountPurchasesResource.Read {

	private static final int FIRST_PAGE = 1;

	private final AccountPurchasesIdentifier accountPurchasesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountPurchasesIdentifier accountPurchasesIdentifier
	 */
	@Inject
	public ReadAccountPurchasesPrototype(
			@RequestIdentifier final AccountPurchasesIdentifier accountPurchasesIdentifier) {
		this.accountPurchasesIdentifier = accountPurchasesIdentifier;
	}

	@Override
	public Single<PaginatedAccountPurchasesIdentifier> onRead() {
		return Single.just(PaginatedAccountPurchasesIdentifier.builder()
				.withAccountPurchases(accountPurchasesIdentifier)
				.withPageId(IntegerIdentifier.of(FIRST_PAGE))
				.build());
	}
}
