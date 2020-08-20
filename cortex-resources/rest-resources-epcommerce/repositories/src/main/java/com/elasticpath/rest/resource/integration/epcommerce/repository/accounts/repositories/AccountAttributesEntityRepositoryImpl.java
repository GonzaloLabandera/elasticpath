/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.Map;
import java.util.Optional;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountAttributesEntity;
import com.elasticpath.rest.definition.accounts.AccountAttributesIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Account Attributes Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountAttributesEntityRepositoryImpl<E extends AccountAttributesEntity, I extends AccountAttributesIdentifier>
		implements Repository<AccountAttributesEntity, AccountAttributesIdentifier> {

	private CustomerService customerService;

	private CustomerProfileAttributeService customerProfileAttributeService;

	private CustomerProfileValueTransformer customerProfileValueTransformer;

	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Override
	public Single<AccountAttributesEntity> findOne(final AccountAttributesIdentifier identifier) {
		AccountAttributesEntity.Builder builder = AccountAttributesEntity.builder();
		Customer account = customerService.findByGuid(identifier.getAccount().getAccountId().getValue());
		Map<String, Optional<CustomerProfileValue>> attributes = customerProfileAttributeService
				.getAccountReadOnlyAttributes(identifier.getAccount().getAccounts().getScope().getValue(), account);
		attributes.keySet().forEach(key -> addProperty(builder, key, attributes));

		return Single.just(builder.build());
	}

	private void addProperty(final AccountAttributesEntity.Builder builder, final String key,
							 final Map<String, Optional<CustomerProfileValue>> attributeValueMap) {
		String value = null;
		Optional<CustomerProfileValue> optionalCustomerProfileValue = attributeValueMap.get(key);

		if (optionalCustomerProfileValue.isPresent()) {
			value = customerProfileValueTransformer.transformToString(optionalCustomerProfileValue.get());
		}

		builder.addingProperty(profileAttributeFieldTransformer.transformToFieldName(key), value);
	}

	@Reference
	public void setCustomerProfileAttributeService(final CustomerProfileAttributeService customerProfileAttributeService) {
		this.customerProfileAttributeService = customerProfileAttributeService;
	}

	@Reference
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
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
