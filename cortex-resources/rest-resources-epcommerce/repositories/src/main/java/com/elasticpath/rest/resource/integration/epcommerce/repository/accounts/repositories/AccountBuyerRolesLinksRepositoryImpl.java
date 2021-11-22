/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */


package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.Optional;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.references.ReferencesIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.ScopePrincipal;
import com.elasticpath.rest.identity.type.BasicPrincipal;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;

/**
 * Account buyer roles links repository.
 *
 * @param <LI> extends ReferencesIdentifier
 * @param <CI> extends AccountBuyerRolesIdentifier
 */
@Component
public class AccountBuyerRolesLinksRepositoryImpl<LI extends ReferencesIdentifier, CI extends AccountBuyerRolesIdentifier> implements
		LinksRepository<ReferencesIdentifier, AccountBuyerRolesIdentifier> {

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<AccountBuyerRolesIdentifier> getElements(final ReferencesIdentifier identifier) {
		return Observable.just(
				AccountBuyerRolesIdentifier.builder()
						.withAccounts(AccountsIdentifier.builder().withScope(getScope()).build())
						.build());
	}

	private StringIdentifier getScope() {
		final ScopePrincipal principal = SubjectUtil.getOnlyPrincipal(resourceOperationContext.getSubject(), ScopePrincipal.class);

		return Optional.ofNullable(principal)
				.map(BasicPrincipal::getValue)
				.map(StringIdentifier::of)
				.orElse(null);
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
