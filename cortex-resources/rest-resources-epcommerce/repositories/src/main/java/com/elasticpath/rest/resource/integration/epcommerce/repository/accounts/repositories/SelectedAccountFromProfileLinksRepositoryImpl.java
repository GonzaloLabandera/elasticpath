/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.Optional;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.service.customer.CustomerService;

/**
 * Repository for account.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class SelectedAccountFromProfileLinksRepositoryImpl<E extends AccountEntity, I extends AccountIdentifier>
		implements LinksRepository<ProfileIdentifier, AccountIdentifier> {

	private ResourceOperationContext resourceOperationContext;

	private CustomerService customerService;

	@Override
	public Observable<AccountIdentifier> getElements(final ProfileIdentifier profileIdentifier) {
		final IdentifierPart<String> scope = profileIdentifier.getScope();
		final Optional<String> accountSharedId = Optional.ofNullable(
				SubjectUtil.getAccountSharedId(resourceOperationContext.getSubject()));
		return accountSharedId.map(sharedId -> Observable.just(AccountIdentifier.builder()
				.withAccountId(StringIdentifier.of(customerService.findCustomerGuidBySharedId(sharedId)))
				.withAccounts(AccountsIdentifier.builder().withScope(scope).build())
				.build())).orElseGet(Observable::empty);
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}
}
