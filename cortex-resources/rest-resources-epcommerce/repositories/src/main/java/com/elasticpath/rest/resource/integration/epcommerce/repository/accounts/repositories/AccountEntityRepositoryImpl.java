/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Account Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountEntityRepositoryImpl<E extends AccountEntity, I extends AccountIdentifier>
		implements Repository<AccountEntity, AccountIdentifier> {

	private ResourceOperationContext resourceOperationContext;

	private UserAccountAssociationService userAccountAssociationService;

	private CustomerService customerService;

	private CustomerProfileAttributeService customerProfileAttributeService;

	private CustomerProfileValueTransformer customerProfileValueTransformer;

	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Override
	public Single<AccountEntity> findOne(final AccountIdentifier identifier) {
		String accountId = identifier.getAccountId().getValue();
		return Single.just(convertCustomerToAccountEntity(identifier.getAccounts().getScope().getValue(), customerService.findByGuid(accountId)));
	}

	@Override
	public Observable<AccountIdentifier> findAll(final IdentifierPart<String> scope) {
		String userId = resourceOperationContext.getUserIdentifier();
		return Observable.fromIterable(userAccountAssociationService.findAssociationsForUser(userId).stream()
				.map(accountAssociation -> AccountIdentifier.builder()
						.withAccountId(StringIdentifier.of(accountAssociation.getAccountGuid()))
						.withAccounts(AccountsIdentifier.builder().withScope(scope).build())
						.build())
				.collect(Collectors.toList()));
	}

	/**
	 * Converts given Customer to AccountEntity.
	 *
	 * @param scope the scope
	 * @param customer customer to convert
	 * @return the converted AccountEntity
	 */
	protected AccountEntity convertCustomerToAccountEntity(final String scope, final Customer customer) {
		AccountEntity.Builder builder = AccountEntity.builder();
		Map<String, Optional<CustomerProfileValue>> attributes = customerProfileAttributeService
				.getAccountEditableAttributes(scope, customer);
		attributes.keySet().forEach(key -> addProperty(builder, key, attributes));
		return builder.build();
	}

	private void addProperty(final AccountEntity.Builder builder, final String key,
							 final Map<String, Optional<CustomerProfileValue>> attributeValueMap) {
		String value = null;
		Optional<CustomerProfileValue> optionalCustomerProfileValue = attributeValueMap.get(key);

		if (optionalCustomerProfileValue.isPresent()) {
			value = customerProfileValueTransformer.transformToString(optionalCustomerProfileValue.get());
		}
		builder.addingProperty(profileAttributeFieldTransformer.transformToFieldName(key), value);
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}

	@Reference
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	@Reference
	public void setCustomerProfileAttributeService(final CustomerProfileAttributeService customerProfileAttributeService) {
		this.customerProfileAttributeService = customerProfileAttributeService;
	}

	@Reference
	public void setCustomerProfileValueTransformer(final CustomerProfileValueTransformer customerProfileValueTransformer) {
		this.customerProfileValueTransformer = customerProfileValueTransformer;
	}

	@Reference
	public void setProfileAttributeFieldTransformer(final ProfileAttributeFieldTransformer profileAttributeFieldTransformer) {
		this.profileAttributeFieldTransformer = profileAttributeFieldTransformer;
	}
}
