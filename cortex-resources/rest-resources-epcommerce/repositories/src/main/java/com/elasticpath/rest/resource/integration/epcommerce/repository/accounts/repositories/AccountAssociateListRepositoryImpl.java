/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.stream.Collectors;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Account associate to account associate list repository.
 *
 * @param <LI> extends AssociatesIdentifier
 * @param <CI> extends AssociateIdentifier
 */
@Component
public class AccountAssociateListRepositoryImpl<LI extends AssociatesIdentifier, CI extends AssociateIdentifier>
		implements LinksRepository<AssociatesIdentifier, AssociateIdentifier> {

	private UserAccountAssociationService userAccountAssociationService;

	@Override
	public Observable<AssociateIdentifier> getElements(final AssociatesIdentifier listIdentifier) {
		AccountIdentifier accountIdentifier = listIdentifier.getAccount();
		String accountId = accountIdentifier.getAccountId().getValue();

		return Observable.fromIterable(
				userAccountAssociationService.findAssociationsForAccount(accountId).stream()
						.map(associate -> getAssociateIdentifier(accountIdentifier, associate))
						.collect(Collectors.toList()));

	}

	private AssociateIdentifier getAssociateIdentifier(final AccountIdentifier accountIdentifier, final UserAccountAssociation associate) {
		AssociatesIdentifier associatesIdentifier = AssociatesIdentifier.builder()
				.withAccount(accountIdentifier)
				.build();

		return AssociateIdentifier.builder()
				.withAssociateId(StringIdentifier.of(associate.getUserGuid()))
				.withAssociates(associatesIdentifier)
				.build();
	}

	@Reference
	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}
}
