/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.accounts.AddAssociateFormIdentifier;

/**
 * Account buyer roles for associate form repository.
 *
 * @param <LI> extends AddAssociateFormIdentifier
 * @param <CI> extends AccountBuyerRolesIdentifier
 */
@Component
public class AccountBuyerRolesForAssociateFormRepositoryImpl<LI extends AddAssociateFormIdentifier, CI extends AccountBuyerRolesIdentifier>
		implements LinksRepository<AddAssociateFormIdentifier, AccountBuyerRolesIdentifier> {

	@Override
	public Observable<AccountBuyerRolesIdentifier> getElements(final AddAssociateFormIdentifier identifier) {
		return Observable.just(AccountBuyerRolesIdentifier.builder().withAccounts(identifier.getAssociates().getAccount().getAccounts()).build());
	}
}
