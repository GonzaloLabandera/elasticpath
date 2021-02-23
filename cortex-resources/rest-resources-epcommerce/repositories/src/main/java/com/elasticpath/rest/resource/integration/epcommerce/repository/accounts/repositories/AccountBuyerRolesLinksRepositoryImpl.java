/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */


package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;

/**
 * Account buyer roles links repository.
 * @param <LI> extends AccountsIdentifier
 * @param <CI> extends AccountBuyerRolesIdentifier
 */
@Component
public class AccountBuyerRolesLinksRepositoryImpl<LI extends AccountsIdentifier, CI extends AccountBuyerRolesIdentifier> implements
		LinksRepository<AccountsIdentifier, AccountBuyerRolesIdentifier> {

	@Override
	public Observable<AccountBuyerRolesIdentifier> getElements(final AccountsIdentifier identifier) {
		return Observable.just(AccountBuyerRolesIdentifier.builder().withAccounts(identifier).build());
	}
}
