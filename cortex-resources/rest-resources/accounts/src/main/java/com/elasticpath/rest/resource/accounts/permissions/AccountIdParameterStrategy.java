/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.permissions;

import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.reactivex.Observable;
import org.apache.shiro.subject.PrincipalCollection;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.authorization.parameter.AbstractCollectionValueStrategy;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.Identifier;
import com.elasticpath.rest.id.transform.IdentifierTransformer;
import com.elasticpath.rest.id.transform.IdentifierTransformerProvider;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.PrincipalsUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Strategy to look up permission for account resource.
 */
@Singleton
@Named("accountIdPermissionParameterStrategy")
public final class AccountIdParameterStrategy extends AbstractCollectionValueStrategy {

	@Inject
	@ResourceRepository
	private Provider<Repository<AccountEntity, AccountIdentifier>> repository;

	@Inject
	private IdentifierTransformerProvider identifierTransformerProvider;

	@Inject
	private Provider<CustomerRepository> customerRepository;

	@Override
	protected Collection<String> getParameterValues(final PrincipalCollection principals) {
		String scope = PrincipalsUtil.getScope(principals);
		final IdentifierTransformer<Identifier> identifierTransformer = identifierTransformerProvider.forUriPart(AccountIdentifier.ACCOUNT_ID);
		return Observable.merge(
				repository.get()
						.findAll(StringIdentifier.of(scope))
						.map(accountIdentifier -> identifierTransformer.identifierToUri(accountIdentifier.getAccountId())),
				//merge with direct children
				repository.get()
						.findAll(StringIdentifier.of(scope))
						.map(accountIdentifier -> customerRepository.get().findDescendants(accountIdentifier.getAccountId().getValue()))
						.flatMapIterable(guids -> guids)
						.map(StringIdentifier::of)
						.map(identifierTransformer::identifierToUri)
		)
				.toList()
				.blockingGet();
	}
}
