/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.permissions;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.reactivex.Observable;
import org.apache.shiro.subject.PrincipalCollection;

import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.authorization.parameter.AbstractCollectionValueStrategy;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.addresses.ContextAwareAddressIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.id.Identifier;
import com.elasticpath.rest.id.transform.IdentifierTransformer;
import com.elasticpath.rest.id.transform.IdentifierTransformerProvider;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.PrincipalsUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;

/**
 * Strategy to look up permission for Address address resource.
 */
@Singleton
@Named("addressIdPermissionParameterStrategy")
public final class AddressIdParameterStrategy extends AbstractCollectionValueStrategy {

	@Inject
	@ResourceRepository
	private Provider<Repository<AddressEntity, AddressIdentifier>> addressEntityRepository;

	@Inject
	@ResourceRepository
	private Provider<Repository<AddressEntity, ContextAwareAddressIdentifier>> contexawareRepository;

	@Inject
	@ResourceRepository
	private Provider<Repository<AccountEntity, AccountIdentifier>> accountRepository;

	@Inject
	@ResourceService
	private Provider<AddressRepository> addressRepository;

	@Inject
	private IdentifierTransformerProvider identifierTransformerProvider;

	@Override
	protected Collection<String> getParameterValues(final PrincipalCollection principals) {
		String scope = PrincipalsUtil.getScope(principals);
		final IdentifierTransformer<Identifier> identifierTransformer = identifierTransformerProvider.forUriPart(AddressIdentifier.ADDRESS_ID);
		List<String> addressIds = Observable.merge(addressEntityRepository.get().findAll(StringIdentifier.of(scope)),
				contexawareRepository.get().findAll(StringIdentifier.of(scope)).map(ContextAwareAddressIdentifier::getAddressIdentifier))
				.map(addressIdentifier -> identifierTransformer.identifierToUri(addressIdentifier.getAddressId()))
				.toList()
				.blockingGet();

		List<String> accountIds = accountRepository.get().findAll(StringIdentifier.of(scope))
				.map(accountIdentifier -> accountIdentifier.getAccountId().getValue()).toList().blockingGet();

		List<String> accountAddressIds = accountIds.stream()
				.map(accountId -> addressRepository.get().getAccountAddresses(accountId).stream()
						.map(address ->
								identifierTransformer.identifierToUri(
										buildAccountAddressIdentifer(address, scope, accountId).getAccountAddressId()))
						.collect(Collectors.toList()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		addressIds.addAll(accountAddressIds);

		return addressIds;
	}

	private AccountAddressIdentifier buildAccountAddressIdentifer(final CustomerAddress address, final String scope, final String accountId) {

		return AccountAddressIdentifier.builder()
				.withAccountAddressId(StringIdentifier.of(address.getGuid()))
				.withAccountAddresses(
						AccountAddressesIdentifier.builder().withAccountId(StringIdentifier.of(accountId))
								.withAddresses(
										AddressesIdentifier.builder().withScope(StringIdentifier.of(scope)).build())
								.build())
				.build();
	}

}
